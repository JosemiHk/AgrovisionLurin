"""Reference offline inference and deterministic cross-runtime fixtures."""
import argparse
import hashlib
import json
from pathlib import Path
import numpy as np
from train_baseline import load_image


def load_model(assets, manifest_file="model_manifest.json"):
    import tensorflow as tf
    assets=Path(assets)
    manifest=json.loads((assets/manifest_file).read_text())
    blob=(assets/manifest["model_file"]).read_bytes()
    if hashlib.sha256(blob).hexdigest()!=manifest["model_sha256"]: raise ValueError("Model SHA256 mismatch")
    interpreter=tf.lite.Interpreter(model_content=blob,num_threads=2)
    interpreter.allocate_tensors()
    inp,out=interpreter.get_input_details()[0],interpreter.get_output_details()[0]
    if inp["dtype"]!=np.float32 or out["dtype"]!=np.float32: raise ValueError("FLOAT32 contract required")
    if inp["shape"].tolist()!=manifest["input_shape"] or out["shape"].tolist()!=manifest["output_shape"]: raise ValueError("Tensor shape mismatch")
    return interpreter,inp,out,manifest


def main(args):
    interpreter,inp,out,manifest=load_model(args.assets)
    def predict(image):
        interpreter.set_tensor(inp["index"],image); interpreter.invoke()
        probabilities=interpreter.get_tensor(out["index"])[0]
        if not np.isfinite(probabilities).all() or (probabilities<0).any() or (probabilities>1).any() or not np.isclose(probabilities.sum(),1,atol=.002): raise ValueError("Invalid probability output")
        return probabilities
    if args.write_fixtures:
        fixtures=[]
        for rgb in [(0,0,0),(255,255,255),(0,255,0),(255,0,0)]:
            fixture=np.broadcast_to(np.array(rgb,dtype=np.float32)/127.5-1,(1,224,224,3)).copy()
            fixtures.append({"rgb":rgb,"probabilities":predict(fixture).tolist()})
        fixture_path=Path(args.fixture_assets)/"parity_fixtures.json"
        fixture_path.parent.mkdir(parents=True,exist_ok=True)
        report=json.loads(fixture_path.read_text()) if fixture_path.exists() else {}
        if report.get("model_sha256") not in {None,manifest["model_sha256"]}: report={}
        report.update(model_sha256=manifest["model_sha256"],tolerance=.002,fixtures=fixtures)
        fixture_path.write_text(json.dumps(report,indent=2),encoding="utf-8")
    if args.image:
        image = load_image(args.image)[None]
        gate, gate_input, gate_output, contract = load_model(args.assets, "leaf_gate.json")
        version = manifest["model_version"] + "+" + contract["model_version"]
        if np.max(np.ptp(np.rint((image + 1) * 127.5), axis=(1, 2))) < 8:
            print(json.dumps({"status": "ERROR", "class_code": "", "reason": "Imagen sin detalles suficientes"}))
            return
        gate.set_tensor(gate_input["index"], image)
        gate.invoke()
        lemon_score, leaf_score = gate.get_tensor(gate_output["index"])[0]
        if not (0 <= leaf_score <= 1 and 0 <= lemon_score <= 1):
            raise ValueError("Invalid gate output")
        if leaf_score < contract["leaf_threshold"]:
            print(json.dumps({"status": "NO_CONCLUYENTE", "class_code": "", "model_version": version,
                              "reason": "No se pudo identificar una hoja de limón"}))
            return
        if lemon_score < contract["acceptance_threshold"] and not args.confirm_lemon:
            print(json.dumps({"status": "NO_CONCLUYENTE", "class_code": "", "model_version": version,
                              "reason": "¿La hoja proviene de un limonero?", "can_confirm_origin": True}))
            return
        if args.confirm_lemon:
            version += "+origen-confirmado"
        p=predict(image); index=int(p.argmax())
        accepted=p[index]>=manifest["abstention_threshold"]
        print(json.dumps({"status":"VALIDO" if accepted else "NO_CONCLUYENTE","class_code":manifest["labels"][index] if accepted else "","score":float(p[index]),"model_version":version,"probabilities":p.tolist()}))


if __name__=="__main__":
    p=argparse.ArgumentParser();p.add_argument("--image");p.add_argument("--assets",default="app/src/main/assets/models");p.add_argument("--write-fixtures",action="store_true")
    p.add_argument("--fixture-assets",default="app/src/androidTest/assets/models")
    p.add_argument("--confirm-lemon", action="store_true", help="Confirm known tree origin for a recognized leaf")
    args=p.parse_args()
    if not args.image and not args.write_fixtures: p.error("--image or --write-fixtures required")
    main(args)
