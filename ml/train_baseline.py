"""Frozen ImageNet MobileNetV2 + trained head; FLOAT32 export, validation threshold.

Test is evaluated only with --evaluate-test, AFTER manifest/model freeze. A receipt
prevents accidental reevaluation of the same held-out manifest in this output dir.
"""
import argparse
import csv
import hashlib
import json
import os
from pathlib import Path
import time

os.environ.setdefault("TF_CPP_MIN_LOG_LEVEL", "2")
os.environ.setdefault("KERAS_HOME", str(Path("ml/artifacts/weights").resolve()))
import numpy as np
from PIL import Image, ImageOps


def load_image(path, size=224):
    with Image.open(path) as image:
        image = ImageOps.exif_transpose(image)
        if "A" in image.getbands() or "transparency" in image.info:
            rgba = image.convert("RGBA")
            image = Image.alpha_composite(Image.new("RGBA", rgba.size, "white"), rgba)
        image = image.convert("RGB")
        return np.asarray(image.resize((size, size), Image.Resampling.BILINEAR), dtype=np.float32) / 127.5 - 1.0


def metrics(labels, probabilities, threshold):
    labels = np.asarray(labels)
    prediction = probabilities.argmax(axis=1)
    accepted = probabilities.max(axis=1) >= threshold
    matrix = np.zeros((2, 3), dtype=int)
    for truth, pred, ok in zip(labels, prediction, accepted): matrix[truth, pred if ok else 2] += 1
    per_class = []
    for i in range(2):
        tp = matrix[i, i]
        precision = float(tp / matrix[:, i].sum()) if matrix[:, i].sum() else 0.0
        recall = float(tp / matrix[i].sum()) if matrix[i].sum() else 0.0
        per_class.append({"precision": precision, "recall": recall, "f1": 2*precision*recall/(precision+recall) if precision+recall else 0.0, "support": int(matrix[i].sum())})
    return {"n": len(labels), "threshold": threshold, "coverage": float(accepted.mean()), "abstentions": int((~accepted).sum()), "accuracy_full_denominator": float(((prediction==labels)&accepted).mean()), "accuracy_without_abstention": float((prediction==labels).mean()), "confusion_columns": ["MINADOR_COMPATIBLE", "SIN_SINTOMAS_VISIBLES", "NO_CONCLUYENTE"], "confusion_matrix": matrix.tolist(), "per_class": per_class}


def select_threshold(labels, probabilities, minimum=.7):
    candidates = []
    for threshold in np.arange(minimum, .951, .05):
        report = metrics(labels, probabilities, float(threshold))
        if report["coverage"] >= .7: candidates.append(report)
    if not candidates: raise ValueError("No uncertainty threshold meets minimum validation coverage; review protocol without using held-out test")
    # Favor F1 on full denominators: abstaining on true positives lowers recall.
    return max(candidates, key=lambda r: (sum(c["f1"] for c in r["per_class"])/2, r["coverage"], -r["threshold"]))["threshold"]


def main(args):
    import tensorflow as tf
    from tensorflow.python.framework.convert_to_constants import convert_variables_to_constants_v2
    config = json.loads(Path(args.config).read_text(encoding="utf-8"))
    tf.config.threading.set_inter_op_parallelism_threads(config["threads"])
    tf.config.threading.set_intra_op_parallelism_threads(config["threads"])
    tf.keras.utils.set_random_seed(config["seed"])
    tf.config.experimental.enable_op_determinism()
    output, assets = Path(args.output), Path(args.assets)
    output.mkdir(parents=True, exist_ok=True)
    receipt = output / "test_evaluation_receipt.json"
    if args.evaluate_test and receipt.exists(): raise RuntimeError("Held-out test already evaluated here. Do not tune/retrain against its results.")
    rows = list(csv.DictReader(Path(args.manifest).open(encoding="utf-8")))
    sets = {s: [r for r in rows if r["split"] == s] for s in ["train", "validation", "test"]}
    if any(not records for records in sets.values()): raise ValueError("All three split sets must be nonempty")
    group_sets = [{r["group"] for r in sets[s]} for s in sets]
    assert not any(group_sets[i]&group_sets[j] for i in range(3) for j in range(i)), "Group leakage"
    base = tf.keras.applications.MobileNetV2(input_shape=(224,224,3), include_top=False, weights="imagenet", pooling="avg")
    base.trainable = False
    augmentation = tf.keras.Sequential([tf.keras.layers.RandomFlip("horizontal"), tf.keras.layers.RandomRotation(.05), tf.keras.layers.RandomContrast(.1)])
    head = tf.keras.Sequential([tf.keras.Input((1280,)), tf.keras.layers.Dropout(.2), tf.keras.layers.Dense(2, activation="softmax")])
    head.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=.001), loss="sparse_categorical_crossentropy", metrics=["accuracy"])
    def features(records, augment=False):
        all_features, all_labels = [], []
        for start in range(0, len(records), config["batch_size"]):
            batch = records[start:start+config["batch_size"]]
            images = np.stack([load_image(Path(args.dataset)/r["path"]) for r in batch])
            if augment: images = np.clip(augmentation(images, training=True).numpy(), -1, 1)
            all_features.append(base(images, training=False).numpy())
            all_labels.extend(config["labels"].index(r["label"]) for r in batch)
        return np.concatenate(all_features), np.array(all_labels)
    print("Extracting training features (original and one seeded augmentation per image)", flush=True)
    x, y = features(sets["train"])
    xa, ya = features(sets["train"], augment=True)
    x, y = np.concatenate([x,xa]), np.concatenate([y,ya])
    print("Extracting validation features", flush=True)
    xv, yv = features(sets["validation"])
    counts = np.bincount(y, minlength=2)
    weights = {i: len(y)/(2*int(count)) for i,count in enumerate(counts)}
    history = head.fit(x, y, validation_data=(xv,yv), batch_size=32, epochs=config["epochs"], class_weight=weights, verbose=2, callbacks=[tf.keras.callbacks.EarlyStopping(monitor="val_loss", patience=2, restore_best_weights=True)])
    input_tensor = tf.keras.Input((224,224,3), batch_size=1, dtype=tf.float32, name="rgb_minus_one_to_one")
    model = tf.keras.Model(input_tensor, head(base(input_tensor, training=False), training=False))
    model.save(output / "baseline.keras")
    concrete = tf.function(lambda inputs: model(inputs, training=False), input_signature=[tf.TensorSpec([1,224,224,3],tf.float32)]).get_concrete_function()
    frozen = convert_variables_to_constants_v2(concrete)
    converter = tf.lite.TFLiteConverter.from_concrete_functions([frozen])
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]
    blob = converter.convert()
    interpreter = tf.lite.Interpreter(model_content=blob, num_threads=config["threads"])
    interpreter.allocate_tensors()
    inp, out = interpreter.get_input_details()[0], interpreter.get_output_details()[0]
    assert inp["shape"].tolist()==[1,224,224,3] and inp["dtype"]==np.float32
    assert out["shape"].tolist()==[1,2] and out["dtype"]==np.float32
    def predict(records):
        result=[]
        for row in records:
            image=load_image(Path(args.dataset)/row["path"])[None,...]
            interpreter.set_tensor(inp["index"],image)
            interpreter.invoke()
            result.append(interpreter.get_tensor(out["index"])[0].copy())
        return np.stack(result)
    pv = predict(sets["validation"])
    keras_pv = head(xv, training=False).numpy()
    delta = float(np.abs(pv-keras_pv).max())
    if delta > .002: raise RuntimeError(f"TFLite/Keras parity mismatch: {delta}")
    threshold=select_threshold(yv,pv,config["minimum_abstention_threshold"])
    manifest={"model_file":"lemon_leaf_mobilenetv2_float32.tflite", "model_version": config["model_version"], "model_sha256":hashlib.sha256(blob).hexdigest(), "labels":config["labels"], "input_shape":[1,224,224,3], "input_dtype":"FLOAT32", "output_shape":[1,2], "output_dtype":"FLOAT32", "input_color":"RGB", "normalization":"minus_one_to_one", "resize":"bilinear", "abstention_threshold":threshold, "input_quantization":{"scale":0.0,"zero_point":0}, "output_quantization":{"scale":0.0,"zero_point":0}, "training_manifest_sha256":hashlib.sha256(Path(args.manifest).read_bytes()).hexdigest(), "seed":config["seed"], "source":"external_unverified_copy", "local_validation_complete":False, "early_detection_validated":False}
    manifest.update(threshold_protocol_version=config["threshold_protocol_version"], abstention_threshold_status="validation_selected_pending_local_validation")
    assets.mkdir(parents=True,exist_ok=True)
    (assets/manifest["model_file"]).write_bytes(blob)
    (assets/"model_manifest.json").write_text(json.dumps(manifest,indent=2),encoding="utf-8")
    parity_fixtures=[]
    for rgb in [(0,0,0),(255,255,255),(0,255,0),(255,0,0)]:
        fixture=np.broadcast_to(np.array(rgb,dtype=np.float32)/127.5-1,(1,224,224,3)).copy()
        interpreter.set_tensor(inp["index"],fixture); interpreter.invoke()
        parity_fixtures.append({"rgb":rgb,"probabilities":interpreter.get_tensor(out["index"])[0].tolist()})
    fixture_assets=Path(args.fixture_assets)
    fixture_assets.mkdir(parents=True,exist_ok=True)
    (fixture_assets/"parity_fixtures.json").write_text(json.dumps({"model_sha256":manifest["model_sha256"],"tolerance":.002,"fixtures":parity_fixtures},indent=2),encoding="utf-8")
    report={"validation":metrics(yv,pv,threshold), "keras_tflite_max_probability_delta":delta, "history":history.history, "class_weights":weights, "model_manifest":manifest, "tensorflow_version":tf.__version__, "test_evaluated":False}
    with (output/"validation_predictions.csv").open("w",newline="",encoding="utf-8") as f:
        writer=csv.writer(f); writer.writerow(["path","truth","p_minador","p_sin_sintomas"])
        writer.writerows([r["path"],r["label"],*p.tolist()] for r,p in zip(sets["validation"],pv))
    if args.evaluate_test:
        # Freeze all decisions above before looking at this set.
        pt=predict(sets["test"])
        yt=[config["labels"].index(r["label"]) for r in sets["test"]]
        test=metrics(yt,pt,threshold)
        receipt.write_text(json.dumps({"frozen_model_sha256":manifest["model_sha256"],"manifest_sha256":manifest["training_manifest_sha256"],"threshold":threshold,"test":test},indent=2),encoding="utf-8")
        report.update(test=test,test_evaluated=True)
    (output/"training_report.json").write_text(json.dumps(report,indent=2),encoding="utf-8")
    print(json.dumps({"model_sha256":manifest["model_sha256"],"threshold":threshold,"validation":report["validation"],"test":report.get("test")}),flush=True)


if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--dataset",required=True)
    parser.add_argument("--manifest",default="ml/artifacts/manifest.csv")
    parser.add_argument("--config",default="ml/config.json")
    parser.add_argument("--output",default="ml/artifacts")
    parser.add_argument("--assets",default="app/src/main/assets/models")
    parser.add_argument("--fixture-assets",default="app/src/androidTest/assets/models")
    parser.add_argument("--evaluate-test",action="store_true")
    main(parser.parse_args())
