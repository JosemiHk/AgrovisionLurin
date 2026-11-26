"""Verify the delivered debug APK contains the current model/contract, not test data."""
import hashlib
import json
from pathlib import Path
from xml.etree import ElementTree
from zipfile import ZipFile

root = Path(__file__).resolve().parents[1]
apk = root / 'app/build/outputs/apk/debug/app-debug.apk'
assets = root / 'app/src/main/assets/models'
manifest = json.loads((assets / 'model_manifest.json').read_text(encoding='utf-8'))
with ZipFile(apk) as archive:
    names = archive.namelist()
    packaged = json.loads(archive.read('assets/models/model_manifest.json'))
    assert packaged == manifest, 'APK manifest differs from current source'
    binary = archive.read('assets/models/' + manifest['model_file'])
    assert hashlib.sha256(binary).hexdigest() == manifest['model_sha256']
    assert binary == (assets / manifest['model_file']).read_bytes()
    gate = json.loads(archive.read('assets/models/leaf_gate.json'))
    assert gate == json.loads((assets / 'leaf_gate.json').read_text(encoding='utf-8'))
    gate_binary = archive.read('assets/models/' + gate['model_file'])
    assert hashlib.sha256(gate_binary).hexdigest() == gate['model_sha256']
    assert gate['outputs'] == ['HOJA_DE_LIMON', 'HOJA'] and 0.5 <= gate['acceptance_threshold'] <= 1
    assert gate['output_shape'] == [1, 2] and 0.5 <= gate['leaf_threshold'] <= 1
    assert not any('parity_images' in name or 'parity_fixtures' in name for name in names)
    assert manifest['abstention_threshold'] >= 0.7
    assert manifest['labels'] == ['MINADOR_COMPATIBLE', 'SIN_SINTOMAS_VISIBLES']
suite_reports = (root / 'app/build/outputs/androidTest-results/connected/debug').glob('TEST-*.xml')
suites = [ElementTree.parse(p).getroot() for p in suite_reports if p.stat().st_mtime >= apk.stat().st_mtime]
result = {'apk': apk.relative_to(root).as_posix(), 'apk_sha256': hashlib.sha256(apk.read_bytes()).hexdigest(), 'apk_bytes': apk.stat().st_size,
          'model_sha256': manifest['model_sha256'], 'model_bytes': len(binary), 'model_version': manifest['model_version'],
          'gate_sha256': gate['model_sha256'], 'gate_version': gate['model_version'],
          'manifest_matches_source': True, 'test_fixtures_in_user_apk': False,
          'android_device_tests_executed': bool(suites),
          'android_test_suites': [{key: suite.get(key) for key in ('name', 'tests', 'failures', 'errors', 'skipped')} for suite in suites],
          'local_agronomic_validation_complete': False}
(root / 'audit/apk_verification.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps(result, indent=2))
