"""One-time correction of binary threshold policy, NEVER reads test scores.

Preserves the initial report and receipt. Test observations under the original
policy remain exploratory; independent local evaluation is still outstanding.
"""
import csv
import json
from pathlib import Path
from train_baseline import metrics, select_threshold
import numpy as np


def main():
    folder=Path("ml/artifacts")
    config=json.loads(Path("ml/config.json").read_text())
    records=list(csv.DictReader((folder/"validation_predictions.csv").open()))
    labels=[config["labels"].index(r["truth"]) for r in records]
    probabilities=np.array([[float(r["p_minador"]),float(r["p_sin_sintomas"])] for r in records])
    threshold=select_threshold(labels,probabilities,config["minimum_abstention_threshold"])
    report_path=folder/"training_report.json"
    original_path=folder/"training_report_initial_exploratory.json"
    receipt_path=folder/"test_evaluation_receipt.json"
    archived_receipt=folder/"test_evaluation_receipt_initial_exploratory.json"
    if original_path.exists(): raise RuntimeError("Protocol correction already applied; do not overwrite archive")
    original_path.write_bytes(report_path.read_bytes())
    archived_receipt.write_bytes(receipt_path.read_bytes())
    manifest_path=Path("app/src/main/assets/models/model_manifest.json")
    manifest=json.loads(manifest_path.read_text())
    manifest.update(abstention_threshold=threshold,threshold_protocol_version=2,abstention_threshold_status="validation_selected_pending_local_validation",model_version="sullana-minador-baseline-1-protocol2")
    manifest_path.write_text(json.dumps(manifest,indent=2),encoding="utf-8")
    # Remove final-test presentation, not the historical evidence.
    report=json.loads(report_path.read_text())
    report.pop("test",None)
    report.update(validation=metrics(labels,probabilities,threshold),model_manifest=manifest,test_evaluated=False,external_test_seen_under_initial_protocol=True,final_independent_local_test_pending=True,protocol_revision_reason="Binary softmax max score >=0.5; initial policy could not abstain. Minimum0.7 selected only from validation, not external test performance.")
    report_path.write_text(json.dumps(report,indent=2),encoding="utf-8")
    receipt_path.write_text(json.dumps({"external_test_already_seen":True,"initial_exploratory_receipt":archived_receipt.name,"current_protocol_version":2,"final_unseen_local_test_pending":True,"reuse_for_tuning_forbidden":True},indent=2),encoding="utf-8")
    print(json.dumps({"threshold":threshold,"validation":report["validation"]}))


if __name__=="__main__": main()
