"""Audit split leakage and produce candidate conflicts for human review."""
import argparse
import csv
import json
from collections import Counter, defaultdict
from pathlib import Path
from prepare_dataset import BKTree


def audit(manifest, output, radius=4):
    rows=list(csv.DictReader(Path(manifest).open(encoding="utf-8")))
    groups=defaultdict(list)
    for row in rows:
        if row["group"]: groups[row["group"]].append(row)
    for identifier,group in groups.items():
        splits={r["split"] for r in group if r["split"] in {"train","validation","test"}}
        if len(splits)>1: raise ValueError(f"Group leaks across splits: {identifier}")
    tree=BKTree(); pairs=[]
    for i,row in enumerate(rows):
        if not row["phash"]: continue
        value=int(row["phash"],16)
        for j in tree.query(value,radius):
            other=rows[j]
            if row["source_class"] != other["source_class"] and (row["label"] or other["label"]) and len(pairs)<100:
                pairs.append({"a":other["path"],"b":row["path"],"hamming_distance":(value^int(other["phash"],16)).bit_count(),"exact_sha256":row["sha256"]==other["sha256"],"group":row["group"]})
        tree.add(value,i)
    report={"group_split_leakage":False,"selected_original_count":sum(bool(r["label"]) for r in rows),"selected_after_exclusion":sum(bool(r["label"]) and r["split"] in {"train","validation","test"} for r in rows),"selected_excluded_by_class":dict(Counter(r["source_class"] for r in rows if r["label"] and r["split"] not in {"train","validation","test"})),"largest_groups":[{"group":g,"n":len(records),"classes":dict(Counter(r["source_class"] for r in records)),"splits":dict(Counter(r["split"] for r in records))} for g,records in sorted(groups.items(),key=lambda pair:-len(pair[1]))[:20]],"candidate_direct_cross_class_pairs":pairs,"interpretation":"Pairs are pHash screening candidates, not expert-confirmed duplicates. Transitive chains may put dissimilar endpoints in one conservative group. Review exclusions manually; no plant/session identity is inferred."}
    Path(output).write_text(json.dumps(report,indent=2,ensure_ascii=False),encoding="utf-8")
    print(json.dumps({k:report[k] for k in ["group_split_leakage","selected_original_count","selected_after_exclusion","selected_excluded_by_class"]}))


if __name__=="__main__":
    p=argparse.ArgumentParser();p.add_argument("--manifest",default="ml/artifacts/manifest.csv");p.add_argument("--output",default="ml/artifacts/duplicate_review.json");p.add_argument("--radius",type=int,default=4)
    a=p.parse_args();audit(a.manifest,a.output,a.radius)
