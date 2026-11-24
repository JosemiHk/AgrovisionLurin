"""Independently verify Android resampling algorithm against Pillow 11.3.

This verifies filter/rounding over RGB arrays, not Android's JPEG decoder.
"""
import json
from pathlib import Path
import numpy as np
from PIL import Image


def kernels(source,target):
    ratio=source/target;radius=max(1.,ratio)
    result=[]
    for index in range(target):
        center=(index+.5)*ratio
        first=max(0,int(center-radius+.5));end=min(source,int(center+radius+.5))
        weights=np.array([max(0.,1.-abs((offset+first-center+.5)/radius)) for offset in range(end-first)])
        weights=(weights/weights.sum()*(1<<22)+.5).astype(np.int64)
        result.append((first,weights))
    return result


def resize_rgb(image,width=224,height=224):
    source=np.asarray(image,dtype=np.int64)
    intermediate=np.empty((source.shape[0],width,3),dtype=np.int64)
    for x,(first,weights) in enumerate(kernels(source.shape[1],width)):
        intermediate[:,x,:]=np.clip((np.sum(source[:,first:first+len(weights),:]*weights[None,:,None],axis=1)+(1<<21))>>22,0,255)
    result=np.empty((height,width,3),dtype=np.uint8)
    for y,(first,weights) in enumerate(kernels(source.shape[0],height)):
        result[y,:,:]=np.clip((np.sum(intermediate[first:first+len(weights),:,:]*weights[:,None,None],axis=0)+(1<<21))>>22,0,255)
    return result


def main():
    rng=np.random.default_rng(20261012);cases=[]
    for width,height in [(17,13),(224,224),(503,331),(1000,1000)]:
        cases.append((f"synthetic_{width}x{height}",Image.fromarray(rng.integers(0,256,(height,width,3),dtype=np.uint8))))
    for path in sorted(Path("app/src/androidTest/assets/models/parity_images").glob("*.jpg")):
        cases.append((path.name,Image.open(path).convert("RGB")))
    records=[]
    for name,image in cases:
        reference=np.asarray(image.resize((224,224),Image.Resampling.BILINEAR),dtype=np.int16)
        actual=resize_rgb(image).astype(np.int16)
        difference=np.abs(reference-actual)
        record={"name":name,"max_channel_difference":int(difference.max()),"different_channels":int((difference>0).sum())}
        records.append(record)
        if difference.max()!=0: raise AssertionError(record)
    report={"algorithm_matches_pillow_rgb_exactly":True,"cases":records,"android_jpeg_decoder_parity": "requires_device_test", "model_or_threshold_changed":False}
    Path("ml/evidence/resize_algorithm_verification.json").write_text(json.dumps(report,indent=2),encoding="utf-8")
    print(json.dumps(report))


if __name__=="__main__": main()
