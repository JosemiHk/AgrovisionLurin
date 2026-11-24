"""Produce the reviewable SQL reference from the compiled Room v2 schema."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
schema = json.loads((root / 'app/schemas/pe.edu.upn.agrovisionlurin.data.local.AppDatabase/2.json').read_text(encoding='utf-8'))['database']
parts = [
    '-- AgroVision Sullana SQLite schema generated from compiled Room version 2.',
    '-- Reference for academic review. This is not the migration for an existing phone.',
    '-- Users must be provisioned in the application; no default credentials are seeded.',
    'PRAGMA foreign_keys = ON;',
    'BEGIN TRANSACTION;',
]
priority = {'users': 0, 'plots': 1, 'inspections': 2}
for entity in sorted(schema['entities'], key=lambda e: priority[e['tableName']]):
    parts.append(entity['createSql'].replace('${TABLE_NAME}', entity['tableName']) + ';')
    parts.extend(index['createSql'].replace('${TABLE_NAME}', entity['tableName']) + ';' for index in entity['indices'])
parts.append('COMMIT;')
target = root / 'database/agrovision_sullana_schema.sql'
target.write_text('\n\n'.join(parts) + '\n', encoding='utf-8')
print('Generated SQL for ' + ', '.join(entity['tableName'] for entity in schema['entities']))
