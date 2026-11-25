$ErrorActionPreference = "Stop"
Set-Location "E:\TESIS\AGROVISION-LURIN-1"

$outDir = "ml\artifacts\minador-candidate"
$modelDir = "$outDir\model"
$fixturesDir = "$outDir\fixtures"
$logFile = "$outDir\run.log"
$py = "ml\.venv\Scripts\python.exe"
$dataset = "ml\Large-Scale Lemon Leaf Disease and Pest Image Data\Large-Scale Lemon Leaf Disease and Pest Image Data"
$manifest = "ml\evidence\manifest.csv"
$config = "ml\config.json"

New-Item -ItemType Directory -Force -Path $outDir, $modelDir, $fixturesDir | Out-Null
Start-Transcript -Path $logFile -Force

try {
    Write-Host "=== [1/4] Verificación de rama, rutas, pesos locales y manifiesto ==="
    git branch --show-current
    foreach ($req in @($py, $dataset, $manifest, $config, "ml\train_baseline.py", "ml\artifacts\weights", "app\src\main\assets\models\lemon_leaf_mobilenetv2_float32.tflite")) {
        if (-not (Test-Path $req)) { throw "Falta ruta requerida: $req" }
    }

    $env:KERAS_HOME = (Resolve-Path "ml\artifacts\weights").Path
    Write-Host "KERAS_HOME=$($env:KERAS_HOME)"

    $checkScript = @'
import csv, json
from collections import Counter, defaultdict
from pathlib import Path

dataset = Path(r"ml/Large-Scale Lemon Leaf Disease and Pest Image Data/Large-Scale Lemon Leaf Disease and Pest Image Data")
manifest = Path(r"ml/evidence/manifest.csv")
rows = list(csv.DictReader(manifest.open(encoding="utf-8")))

split_counts = defaultdict(Counter)
split_groups = defaultdict(set)
missing = []

for r in rows:
    sp = r["split"]
    lb = r["label"] or "(sin_etiqueta)"
    split_counts[sp][lb] += 1
    if r["group"]:
        split_groups[sp].add(r["group"])
    if sp in ("train", "validation"):
        if not (dataset / r["path"]).exists():
            missing.append(r["path"])

splits = list(split_groups.keys())
cross_leaks = {}
for i in range(len(splits)):
    for j in range(i + 1, len(splits)):
        a, b = splits[i], splits[j]
        inter = split_groups[a] & split_groups[b]
        if inter:
            cross_leaks[f"{a}__vs__{b}"] = len(inter)

summary = {
    "total_rows": len(rows),
    "split_counts": {k: dict(v) for k, v in split_counts.items()},
    "group_counts": {k: len(v) for k, v in split_groups.items()},
    "cross_split_group_leaks": cross_leaks,
    "missing_train_val_files": len(missing),
    "missing_samples": missing[:10],
}
Path(r"ml/artifacts/minador-candidate/manifest_check.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
print(json.dumps(summary, indent=2))
assert not cross_leaks, f"Fuga de grupos entre particiones: {cross_leaks}"
assert not missing, f"Faltan {len(missing)} archivos en train/validation"
assert sum(split_counts["train"].values()) == 1286, f"Train != 1286: {split_counts['train']}"
assert sum(split_counts["validation"].values()) == 276, f"Validation != 276: {split_counts['validation']}"
'@
    $checkScript | & $py -
    if ($LASTEXITCODE -ne 0) { throw "Falló la verificación del manifiesto (exit $LASTEXITCODE)" }

    Write-Host "=== [2/4] Entrenamiento real y exportación TFLite del candidato (sin --evaluate-test) ==="
    & $py "ml\train_baseline.py" `
        --dataset $dataset `
        --manifest $manifest `
        --config $config `
        --output $outDir `
        --assets $modelDir `
        --fixture-assets $fixturesDir
    if ($LASTEXITCODE -ne 0) { throw "Falló ml/train_baseline.py (exit $LASTEXITCODE)" }

    Write-Host "=== [3/4] Comparación TFLite actual vs candidato en 276 fotos de validation + paridad y contrato Android ==="
    $evalScript = @'
import csv, json, hashlib
from pathlib import Path
import numpy as np
from PIL import Image, ImageOps
import tensorflow as tf

dataset = Path(r"ml/Large-Scale Lemon Leaf Disease and Pest Image Data/Large-Scale Lemon Leaf Disease and Pest Image Data")
manifest_rows = [r for r in csv.DictReader(Path(r"ml/evidence/manifest.csv").open(encoding="utf-8")) if r["split"] == "validation"]
cur_model_path = Path(r"app/src/main/assets/models/lemon_leaf_mobilenetv2_float32.tflite")
cur_manifest = json.loads(Path(r"app/src/main/assets/models/model_manifest.json").read_text(encoding="utf-8"))
cand_model_path = Path(r"ml/artifacts/minador-candidate/model/lemon_leaf_mobilenetv2_float32.tflite")
cand_manifest = json.loads(Path(r"ml/artifacts/minador-candidate/model/model_manifest.json").read_text(encoding="utf-8"))
train_report = json.loads(Path(r"ml/artifacts/minador-candidate/training_report.json").read_text(encoding="utf-8"))
labels = cur_manifest["labels"]

def load_image(path, size=224):
    with Image.open(path) as img:
        img = ImageOps.exif_transpose(img).convert("RGB")
        return np.asarray(img.resize((size, size), Image.Resampling.BILINEAR), dtype=np.float32) / 127.5 - 1.0

images = [load_image(dataset / r["path"])[None, ...] for r in manifest_rows]
y_true = np.array([labels.index(r["label"]) for r in manifest_rows], dtype=int)

def eval_tflite(model_path):
    interp = tf.lite.Interpreter(model_path=str(model_path), num_threads=2)
    interp.allocate_tensors()
    inp = interp.get_input_details()[0]
    out = interp.get_output_details()[0]
    probs = []
    for img in images:
        interp.set_tensor(inp["index"], img)
        interp.invoke()
        probs.append(interp.get_tensor(out["index"])[0].copy())
    contract = {
        "input_shape": inp["shape"].tolist(),
        "input_dtype": str(inp["dtype"]),
        "output_shape": out["shape"].tolist(),
        "output_dtype": str(out["dtype"]),
        "sha256": hashlib.sha256(model_path.read_bytes()).hexdigest(),
    }
    return np.stack(probs), contract

def compute_metrics(y, probs, threshold):
    pred = probs.argmax(axis=1)
    accepted = probs.max(axis=1) >= threshold
    m = np.zeros((2, 3), dtype=int)
    for t, p, ok in zip(y, pred, accepted):
        m[t, p if ok else 2] += 1
    tp_min = int(m[0, 0])
    fp_min = int(m[1, 0])
    prec_min = float(tp_min / (tp_min + fp_min)) if (tp_min + fp_min) else 0.0
    rec_min = float(tp_min / m[0].sum()) if m[0].sum() else 0.0
    return {
        "threshold": float(threshold),
        "n": int(len(y)),
        "minador_tp_of_99": tp_min,
        "minador_fp_of_177": fp_min,
        "minador_precision": prec_min,
        "minador_recall": rec_min,
        "abstentions": int((~accepted).sum()),
        "coverage": float(accepted.mean()),
        "confusion_columns": ["MINADOR_COMPATIBLE", "SIN_SINTOMAS_VISIBLES", "NO_CONCLUYENTE"],
        "confusion_rows": ["MINADOR_COMPATIBLE", "SIN_SINTOMAS_VISIBLES"],
        "confusion_matrix": m.tolist(),
    }

cur_probs, cur_contract = eval_tflite(cur_model_path)
cand_probs, cand_contract = eval_tflite(cand_model_path)

# Verificación directa de paridad Keras vs TFLite del candidato sobre las 276 imágenes de validación
keras_model = tf.keras.models.load_model(r"ml/artifacts/minador-candidate/baseline.keras", compile=False)
keras_probs = np.vstack([keras_model(img, training=False).numpy() for img in images])
keras_tflite_max_delta = float(np.abs(cand_probs - keras_probs).max())

cand_selected_thr = float(cand_manifest["abstention_threshold"])
comparison = {
    "current_at_0_70": compute_metrics(y_true, cur_probs, 0.70),
    "candidate_at_0_70": compute_metrics(y_true, cand_probs, 0.70),
    "candidate_selected_threshold": cand_selected_thr,
    "candidate_at_selected_threshold": compute_metrics(y_true, cand_probs, cand_selected_thr) if abs(cand_selected_thr - 0.70) > 1e-6 else None,
    "current_contract": cur_contract,
    "candidate_contract": cand_contract,
    "candidate_manifest_contract_ok": (
        cand_contract["input_shape"] == [1, 224, 224, 3]
        and "float32" in cand_contract["input_dtype"]
        and cand_contract["output_shape"] == [1, 2]
        and "float32" in cand_contract["output_dtype"]
        and cand_manifest["input_color"] == "RGB"
        and cand_manifest["normalization"] == "minus_one_to_one"
        and cand_manifest["labels"] == cur_manifest["labels"]
    ),
    "keras_tflite_max_delta_direct": keras_tflite_max_delta,
    "keras_tflite_max_delta_report": train_report.get("keras_tflite_max_probability_delta"),
    "epochs_completed": len(train_report.get("history", {}).get("loss", [])),
    "candidate_same_weights_as_current": cur_contract["sha256"] == cand_contract["sha256"],
}
Path(r"ml/artifacts/minador-candidate/comparison_report.json").write_text(json.dumps(comparison, indent=2), encoding="utf-8")
print(json.dumps(comparison, indent=2))
'@
    $evalScript | & $py -
    if ($LASTEXITCODE -ne 0) { throw "Falló la evaluación comparativa (exit $LASTEXITCODE)" }

    Write-Host "=== [4/4] Comprobación de disponibilidad de Pixel Tablet (emulator-5554) ==="
    $adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
    if (Test-Path $adb) {
        & $adb devices -l
    } else {
        Write-Host "adb no encontrado en SDK por defecto."
    }

    Write-Host "=== LOTE COMPLETADO CORRECTAMENTE ==="
} catch {
    Write-Host "ERROR EN EL LOTE: $($_.Exception.Message)"
    exit 1
} finally {
    Stop-Transcript
}

