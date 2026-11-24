import hashlib
import json
from pathlib import Path
import sys
import tempfile
import unittest

import numpy as np
from PIL import Image

sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from prepare_dataset import BKTree, assign_groups, prepare
from train_baseline import metrics, load_image, select_threshold


class PipelineTests(unittest.TestCase):
    def test_bk_tree_matches_exhaustive_hamming_search(self):
        rng=np.random.default_rng(7)
        values=[int(v) for v in rng.integers(0,2**63,100,dtype=np.int64)]
        tree=BKTree()
        for i,value in enumerate(values): tree.add(value,i)
        for value in values[:10]:
            self.assertEqual(set(tree.query(value,20)),{i for i,v in enumerate(values) if (value^v).bit_count()<=20})

    def test_reproducible_group_split(self):
        groups=[(str(i),list(range(i%7+1))) for i in range(100)]
        first=assign_groups(groups,[.7,.15,.15],42)
        self.assertEqual(first,assign_groups(groups,[.7,.15,.15],42))
        self.assertEqual(set(first.values()),{"train","validation","test"})

    def test_corrupt_and_cross_class_duplicates_excluded_without_mutation(self):
        with tempfile.TemporaryDirectory(dir=Path(__file__).resolve().parents[1]) as tmp:
            root=Path(tmp)/"source"; output=Path(tmp)/"output"
            for label in ["Citrus Leafminer","Healthy"]: (root/label).mkdir(parents=True)
            img=Image.fromarray(np.random.default_rng(8).integers(0,255,(64,64,3),dtype=np.uint8))
            img.save(root/"Healthy/a.jpg")
            (root/"Citrus Leafminer/b.jpg").write_bytes((root/"Healthy/a.jpg").read_bytes())
            (root/"Healthy/corrupt.jpg").write_bytes(b"not an image")
            before={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in root.rglob("*.jpg")}
            config=json.loads((Path(__file__).resolve().parents[1]/"config.json").read_text())
            rows=prepare(root,output,config)
            self.assertEqual({r["split"] for r in rows},{"excluded_conflict","corrupt"})
            self.assertEqual(before,{p:hashlib.sha256(p.read_bytes()).hexdigest() for p in root.rglob("*.jpg")})

    def test_abstention_not_removed_from_recall_or_accuracy(self):
        report=metrics([0,0,1],np.array([[.9,.1],[.55,.45],[.1,.9]]),.8)
        self.assertEqual(report["n"],3)
        self.assertEqual(report["abstentions"],1)
        self.assertEqual(report["per_class"][0]["recall"],.5)
        self.assertAlmostEqual(report["accuracy_full_denominator"],2/3)

    def test_binary_threshold_can_abstain(self):
        probabilities=np.array([[.9,.1],[.6,.4],[.1,.9],[.1,.9]])
        threshold=select_threshold([0,0,1,1],probabilities)
        self.assertGreaterEqual(threshold,.7)
        self.assertEqual(metrics([0,0,1,1],probabilities,threshold)["abstentions"],1)

    def test_normalization_and_rgb_order(self):
        with tempfile.TemporaryDirectory(dir=Path(__file__).resolve().parents[1]) as tmp:
            path=Path(tmp)/"red.png"
            Image.new("RGB",(10,10),(255,0,0)).save(path)
            arr=load_image(path)
            self.assertEqual(arr.shape,(224,224,3))
            np.testing.assert_array_equal(arr[0,0],[1,-1,-1])


if __name__=="__main__": unittest.main()
