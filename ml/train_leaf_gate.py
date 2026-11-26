"""Train the offline photo eligibility filter. See ml/README.md for the input layout."""
import argparse
import csv
import hashlib
import io
import json
import random
from collections import defaultdict
from pathlib import Path

import numpy as np
from PIL import Image, ImageOps

from prepare_dataset import BKTree, phash
from train_baseline import load_image


SEED = 426
CATEGORIES = ("lemon", "orange", "other_leaf", "object")


def split_images(root, baseline_manifest):
    # Protect the existing diagnostic validation and test from reuse by the filter.
    previous = BKTree()
    with baseline_manifest.open(encoding="utf-8") as stream:
        for i, row in enumerate(csv.DictReader(stream)):
            if row["split"] in ("validation", "test"):
                previous.add(int(row["phash"], 16), i)
    seen = BKTree()
    # Keep the five reported regressions outside every development partition.
    for h in (0x2cc366ccce8c3972, 0x1b1929b9d3e30e16, 0x610d025c2ffe7e48,
              0x2c0d9d3a73f4845a, 0x6c2c75224d6cdcb2):
        previous.add(h, -1)
    rows = []
    original_sources = set()
    with (Path(__file__).parent / "evidence/leaf_gate_v1_images.csv").open(encoding="utf-8") as stream:
        for record in csv.DictReader(stream):
            path = Path(record["path"])
            file = root / path
            if hashlib.sha256(file.read_bytes()).hexdigest() != record["sha256"]:
                raise ValueError(f"Image changed: {file}")
            with Image.open(file) as image:
                fingerprint = phash(ImageOps.exif_transpose(image))
            original_sources.add(path.parts[:2])
            if previous.query(fingerprint, 6):
                continue
            seen.add(fingerprint, file)
            rows.append(tuple(record[key] for key in ("path", "category", "split", "sha256")))
    groups = defaultdict(list)
    for category in CATEGORIES:
        for file in sorted((root / category).rglob("*")):
            if file.suffix.lower() not in (".png", ".jpg", ".jpeg"):
                continue
            if file.relative_to(root).parts[:2] in original_sources:
                continue
            with Image.open(file) as image:
                fingerprint = phash(ImageOps.exif_transpose(image))
            if seen.query(fingerprint, 6) or previous.query(fingerprint, 6):
                continue
            seen.add(fingerprint, file)
            path = file.relative_to(root)
            groups[category, path.parts[1]].append(path.as_posix())
    for (category, source), paths in sorted(groups.items()):
        random.Random(SEED).shuffle(paths)
        for i, path in enumerate(paths):
            split = "test"
            if source != "COCO128":  # Entire COCO collection reserved as an unseen source.
                if i < int(.7 * len(paths)):
                    split = "train"
                elif i < int(.85 * len(paths)):
                    split = "validation"
            rows.append((path, category, split, hashlib.sha256((root / path).read_bytes()).hexdigest()))
    return rows


def train(root, assets, evidence, baseline_manifest):
    import tensorflow as tf
    from sklearn.linear_model import LogisticRegression
    from tensorflow.python.framework.convert_to_constants import convert_variables_to_constants_v2

    tf.keras.utils.set_random_seed(SEED)
    tf.config.threading.set_intra_op_parallelism_threads(3)
    tf.config.threading.set_inter_op_parallelism_threads(1)
    rows = split_images(root, baseline_manifest)
    for partition in ("train", "validation", "test"):
        missing = set(CATEGORIES) - {category for _, category, split, _ in rows if split == partition}
        if missing:
            raise ValueError(f"Missing samples for {sorted(missing)} in {partition}")
    base = tf.keras.applications.MobileNetV2(input_shape=(224, 224, 3), alpha=1.,
                                              include_top=False, pooling="avg", weights="imagenet")
    base.trainable = False
    features, labels, weights, leaves = [], [], [], []
    rng = np.random.default_rng(SEED)
    embed = tf.function(lambda x: base(x, training=False), reduce_retracing=True)
    for start in range(0, len(rows), 24):
        batch = [r for r in rows[start:start + 24] if r[2] == "train"]
        if batch:
            images = np.stack([load_image(root / r[0]) for r in batch])
            # Include close-ups where injury, rotation and framing change the leaf outline.
            crops = []
            for image in images:
                width = int(rng.integers(132, 205))
                y, x = rng.integers(0, 225 - width, size=2)
                crop = np.rot90(image[y:y+width, x:x+width], int(rng.integers(4)))
                crops.append(tf.image.resize(crop, (224, 224)))
            for view in (images, tf.stack(crops)):
                features.append(embed(view).numpy())
                labels.extend(int(r[1] == "lemon") for r in batch)
                leaves.extend(int(r[1] != "object") for r in batch)
                weights.extend(3.0 if "leafminer" in r[0].lower() else 1.0 for r in batch)
    embeddings = np.concatenate(features)
    head = LogisticRegression(C=.1, class_weight="balanced", max_iter=500).fit(embeddings, labels, sample_weight=weights)
    leaf = LogisticRegression(C=.1, class_weight="balanced", max_iter=500).fit(embeddings, leaves)
    score = tf.keras.layers.Dense(2, activation="sigmoid")
    model = tf.keras.Model(base.input, score(base.output))
    score.set_weights([np.vstack([head.coef_, leaf.coef_]).T.astype("float32"),
                       np.concatenate([head.intercept_, leaf.intercept_]).astype("float32")])
    concrete = tf.function(lambda x: model(x, training=False),
                           input_signature=[tf.TensorSpec([1, 224, 224, 3], tf.float32)]).get_concrete_function()
    converter = tf.lite.TFLiteConverter.from_concrete_functions([convert_variables_to_constants_v2(concrete)])
    blob = converter.convert()
    interpreter = tf.lite.Interpreter(model_content=blob, num_threads=2)
    interpreter.allocate_tensors()
    inp, out = interpreter.get_input_details()[0], interpreter.get_output_details()[0]

    def scores(split):
        result = defaultdict(list)
        for path, category, partition, _ in rows:
            if partition == split:
                interpreter.set_tensor(inp["index"], load_image(root / path)[None])
                interpreter.invoke()
                result[category].append(interpreter.get_tensor(out["index"])[0])
        return {category: np.array(result[category]) for category in CATEGORIES}

    validation = scores("validation")
    # Highest coverage subject to <=2% false accepts in EACH negative group.
    leaf_threshold = next(t / 1000 for t in range(900, 1001)
                          if not np.any(validation["object"][:, 1] >= t / 1000))
    def accepted(v, cutoff):
        return (v[:, 0] >= cutoff) & (v[:, 1] >= leaf_threshold)
    threshold = next(t / 1000 for t in range(500, 1001)
                     if all(np.mean(accepted(validation[c], t / 1000)) <= .02 for c in CATEGORIES[1:]))
    if np.mean(accepted(validation["lemon"], threshold)) < .70:
        raise ValueError("Insufficient coverage for lemon leaves at the selected threshold")
    manifest = dict(model_file="lemon_leaf_gate.tflite", model_version="leaf-gate-v2",
                    model_sha256=hashlib.sha256(blob).hexdigest(), acceptance_threshold=threshold,
                    outputs=["HOJA_DE_LIMON", "HOJA"], leaf_threshold=leaf_threshold,
                    input_shape=[1, 224, 224, 3], output_shape=[1, 2],
                    normalization="minus_one_to_one", input_color="RGB", resize="bilinear")
    stream = io.StringIO(newline="")
    writer = csv.writer(stream, lineterminator="\n")
    writer.writerow(["path", "category", "split", "sha256"])
    writer.writerows(rows)
    inventory = stream.getvalue().encode()
    report = dict(manifest=manifest, inventory_sha256=hashlib.sha256(inventory).hexdigest(),
                  training_images=sum(r[2] == "train" for r in rows), training_views=len(labels), local_validation_complete=False)
    for name, values in [("validation", validation), ("test", scores("test"))]:
        report[name] = {c: {"total": len(v), "accepted_as_lemon": int(accepted(v, threshold).sum()),
                            "recognized_as_leaf": int((v[:, 1] >= leaf_threshold).sum())}
                        for c, v in values.items()}
    assets.mkdir(parents=True, exist_ok=True)
    evidence.mkdir(parents=True, exist_ok=True)
    (assets / manifest["model_file"]).write_bytes(blob)
    (assets / "leaf_gate.json").write_text(json.dumps(manifest, indent=2) + "\n")
    (evidence / "leaf_gate_images.csv").write_bytes(inventory)
    (evidence / "leaf_gate_report.json").write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dataset", required=True, type=Path)
    parser.add_argument("--assets", type=Path, default=Path("app/src/main/assets/models"))
    parser.add_argument("--evidence", type=Path, default=Path("ml/evidence"))
    parser.add_argument("--baseline-manifest", type=Path, default=Path("ml/evidence/manifest.csv"))
    args = parser.parse_args()
    train(args.dataset, args.assets, args.evidence, args.baseline_manifest)
