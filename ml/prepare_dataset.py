"""Read-only inventory; group exact/perceptual duplicates BEFORE splitting.

No images are modified or copied. Relative paths make the manifest portable.
Perceptual grouping is conservative screening, not proof of independent plants.
"""
import argparse
import csv
import hashlib
import json
import random
from collections import Counter, defaultdict
from pathlib import Path

import numpy as np
from PIL import Image, ImageOps


def phash(image):
    values = np.asarray(image.convert("L").resize((32, 32), Image.Resampling.LANCZOS), dtype=float)
    n = np.arange(32)
    basis = np.cos(np.pi / 32 * (n + .5)[None, :] * n[:, None])
    low = (basis @ values @ basis.T)[:8, :8].flatten()
    bits = low > np.median(low[1:])
    bits[0] = False
    return sum(int(bit) << i for i, bit in enumerate(bits))


class UnionFind:
    def __init__(self, n): self.parents = list(range(n))
    def find(self, x):
        while self.parents[x] != x:
            self.parents[x] = self.parents[self.parents[x]]
            x = self.parents[x]
        return x
    def union(self, x, y):
        a, b = self.find(x), self.find(y)
        self.parents[max(a, b)] = min(a, b)


class BKTree:
    def __init__(self): self.root = None
    def query(self, value, radius):
        if self.root is None: return []
        stack, found = [self.root], []
        while stack:
            node = stack.pop()
            distance = (node[0] ^ value).bit_count()
            if distance <= radius: found.append(node[1])
            stack.extend(child for d, child in node[2].items() if distance-radius <= d <= distance+radius)
        return found
    def add(self, value, index):
        if self.root is None:
            self.root = [value, index, {}]
            return
        node = self.root
        while True:
            distance = (node[0] ^ value).bit_count()
            if distance == 0: return
            if distance not in node[2]:
                node[2][distance] = [value, index, {}]
                return
            node = node[2][distance]


def assign_groups(groups, ratios, seed):
    """Approximate stratification by group size, without splitting any group."""
    rng = random.Random(seed)
    groups = list(groups)
    rng.shuffle(groups)
    groups.sort(key=lambda pair: -len(pair[1]))
    total = sum(len(indices) for _, indices in groups)
    targets = [total * ratio for ratio in ratios]
    counts = [0, 0, 0]
    result = {}
    for group, indices in groups:
        # Largest remaining target first; tie order is train, validation, test.
        choice = max(range(3), key=lambda s: targets[s] - counts[s])
        result[group] = ["train", "validation", "test"][choice]
        counts[choice] += len(indices)
    return result


def prepare(root, output, config):
    root, output = Path(root), Path(output)
    if not root.is_dir(): raise ValueError(f"Dataset directory missing: {root}")
    missing = set(config["classes"]) - {p.name for p in root.iterdir() if p.is_dir()}
    if missing: raise ValueError(f"Required class folders missing: {sorted(missing)}")
    files = sorted(p for p in root.rglob("*") if p.is_file() and p.suffix.lower() in {".jpg", ".jpeg", ".png"})
    rows = []
    for p in files:
        row = {"path": p.relative_to(root).as_posix(), "source_class": p.relative_to(root).parts[0], "sha256": hashlib.sha256(p.read_bytes()).hexdigest(), "width": "", "height": "", "phash": "", "error": "", "group": "", "split": "corrupt", "label": ""}
        try:
            with Image.open(p) as raw:
                raw.load()
                im = ImageOps.exif_transpose(raw).convert("RGB")
                row.update(width=im.width, height=im.height, phash=f"{phash(im):016x}")
        except Exception as exc:
            row["error"] = type(exc).__name__ + ": " + str(exc)
        row["label"] = config["classes"].get(row["source_class"], "")
        rows.append(row)
    union, tree, hashes = UnionFind(len(rows)), BKTree(), {}
    for i, row in enumerate(rows):
        if row["error"]: continue
        if row["sha256"] in hashes: union.union(i, hashes[row["sha256"]])
        hashes[row["sha256"]] = i
        value = int(row["phash"], 16)
        for j in tree.query(value, config["phash_distance"]): union.union(i, j)
        tree.add(value, i)
    grouped = defaultdict(list)
    for i, row in enumerate(rows):
        if not row["error"]: grouped[union.find(i)].append(i)
    eligible = defaultdict(list)
    conflicts = []
    for g, indices in grouped.items():
        group = "g_" + rows[g]["sha256"][:16]
        source_classes = {rows[i]["source_class"] for i in indices}
        selected = [i for i in indices if rows[i]["label"]]
        conflict = bool(selected) and len(source_classes) > 1
        if conflict: conflicts.append({"group": group, "classes": sorted(source_classes), "count": len(indices)})
        for i in indices:
            rows[i]["group"] = group
            rows[i]["split"] = "excluded_conflict" if conflict else "challenge"
        if selected and not conflict: eligible[rows[selected[0]]["label"]].append((group, selected))
    splits = {}
    for label, groups in eligible.items(): splits.update(assign_groups(groups, config["split_ratios"], config["seed"]))
    for row in rows:
        if row["group"] in splits: row["split"] = splits[row["group"]]
    output.mkdir(parents=True, exist_ok=True)
    manifest = output / "manifest.csv"
    with manifest.open("w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0]) if rows else ["path"])
        writer.writeheader(); writer.writerows(rows)
    summary = {"source_url": config["source_url"], "provenance_confirmed": False, "local_limonort": False, "sutil_variety_confirmed": False, "early_stage_confirmed": False, "files_found": len(rows), "published_count": config["published_count"], "difference_from_publication": len(rows)-config["published_count"], "classes": dict(Counter(r["source_class"] for r in rows)), "split_counts": dict(Counter(r["split"] for r in rows)), "selected_class_split_counts": dict(Counter(r["label"]+":"+r["split"] for r in rows if r["label"])), "valid_duplicate_groups": sum(len(v)>1 for v in grouped.values()), "max_group_size": max(map(len, grouped.values()), default=0), "cross_class_conflicts": conflicts, "seed": config["seed"], "phash_distance": config["phash_distance"], "manifest_sha256": hashlib.sha256(manifest.read_bytes()).hexdigest(), "limitations": ["pHash groups do not establish plant/session independence; these identifiers were not supplied", "Unverified copy provenance; publication totals differ", "External dataset is not local agronomic validation"]}
    (output / "dataset_report.json").write_text(json.dumps(summary, indent=2, ensure_ascii=False), encoding="utf-8")
    print(json.dumps({k: summary[k] for k in ["files_found", "split_counts", "valid_duplicate_groups", "max_group_size"]}))
    return rows


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--dataset", required=True)
    parser.add_argument("--output", default="ml/artifacts")
    parser.add_argument("--config", default="ml/config.json")
    args = parser.parse_args()
    prepare(args.dataset, args.output, json.loads(Path(args.config).read_text(encoding="utf-8")))
