"""Reference offline inference and deterministic cross-runtime fixtures."""
import argparse
import hashlib
import json
from pathlib import Path
import numpy as np
from train_baseline import load_image


def load_model(assets):
    import tensorflow as tf
    assets=Path(assets)
    manifest=json.loads((assets/"model_manifest.json").read_text())
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
        p=predict(load_image(args.image)[None,...]); index=int(p.argmax())
        accepted=p[index]>=manifest["abstention_threshold"]
        print(json.dumps({"status":"VALIDO" if accepted else "NO_CONCLUYENTE","class_code":manifest["labels"][index] if accepted else "","score":float(p[index]),"model_version":manifest["model_version"],"probabilities":p.tolist()}))


if __name__=="__main__":
    p=argparse.ArgumentParser();p.add_argument("--image");p.add_argument("--assets",default="app/src/main/assets/models");p.add_argument("--write-fixtures",action="store_true")
    p.add_argument("--fixture-assets",default="app/src/androidTest/assets/models")
    args=p.parse_args()
    if not args.image and not args.write_fixtures: p.error("--image or --write-fixtures required")
    main(args)
