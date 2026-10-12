"""Verify the delivered debug APK contains the current model/contract, not test data."""
import hashlib
import json
from pathlib import Path
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
    assert not any('parity_images' in name or 'parity_fixtures' in name for name in names)
    assert manifest['abstention_threshold'] >= 0.7
    assert manifest['labels'] == ['MINADOR_COMPATIBLE', 'SIN_SINTOMAS_VISIBLES']
result = {'apk': str(apk), 'apk_sha256': hashlib.sha256(apk.read_bytes()).hexdigest(), 'apk_bytes': apk.stat().st_size,
          'model_sha256': manifest['model_sha256'], 'model_bytes': len(binary), 'model_version': manifest['model_version'],
          'manifest_matches_source': True, 'test_fixtures_in_user_apk': False,
          'android_device_tests_executed': (root / 'audit/ui_review/android_tests.txt').exists() and 'OK (8 tests)' in (root / 'audit/ui_review/android_tests.txt').read_text(encoding='utf-8-sig'), 'local_agronomic_validation_complete': False}
(root / 'audit/apk_verification.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps(result, indent=2))
