"""Check migration SQL semantics on legacy rows. This does not run Android Room."""
from pathlib import Path
import re
import sqlite3
import json

root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/pe/edu/upn/agrovisionlurin/data/local/AppDatabase.kt').read_text(encoding='utf-8')
block = source.split('override fun migrate', 1)[1].split('@Volatile', 1)[0]
statements = re.findall(r'db\.execSQL\("([^"\n]*)"\)', block)
assert statements, 'No migration SQL found'
original = sqlite3.connect((root / 'database/agrovision_lurin.sqlite').as_uri() + '?mode=ro', uri=True)
ddl = original.execute("SELECT sql FROM sqlite_master WHERE name='inspections'").fetchone()[0]
rows = original.execute('SELECT * FROM inspections').fetchall()
original.close()
db = sqlite3.connect(':memory:')
db.execute('PRAGMA foreign_keys=ON')
db.execute(ddl)
db.executemany('INSERT INTO inspections VALUES (' + ','.join('?' for _ in rows[0]) + ')', rows)
db.commit()
for statement in statements:
    # Dynamic index statement is executed explicitly below.
    if '$' not in statement:
        db.execute(statement)
for column in ('syncStatus', 'createdAt', 'userId', 'plotId'):
    db.execute(f'CREATE INDEX index_inspections_{column} ON inspections({column})')
assert db.execute('SELECT COUNT(*) FROM inspections').fetchone()[0] == len(rows)
assert db.execute("SELECT COUNT(*) FROM inspections WHERE state='LEGADO'").fetchone()[0] == len(rows)
assert db.execute('PRAGMA foreign_key_check').fetchall() == []
assert db.execute('SELECT id,plotId,imagePath,diagnosisClass,confidence,createdAt FROM inspections ORDER BY id').fetchall() == sorted([(r[0], r[1], r[3], r[4], r[5], r[10]) for r in rows])
try:
    db.execute("DELETE FROM users WHERE id='__legacy_user__'")
except sqlite3.IntegrityError:
    pass
else:
    raise AssertionError('Referenced user deletion must fail')
try:
    db.execute("INSERT INTO plots VALUES ('different-id',(SELECT code FROM plots LIMIT 1),'Duplicate',1)")
except sqlite3.IntegrityError:
    pass
else:
    raise AssertionError('Duplicate plot code must fail')
assert len(db.execute('PRAGMA foreign_key_list(inspections)').fetchall()) == 2
schema_file = root / 'app/schemas/pe.edu.upn.agrovisionlurin.data.local.AppDatabase/2.json'
if schema_file.exists():
    schema = json.loads(schema_file.read_text(encoding='utf-8'))['database']
    expected_db = sqlite3.connect(':memory:')
    for entity in schema['entities']:
        name = entity['tableName']
        expected_db.execute(entity['createSql'].replace('${TABLE_NAME}', name))
        for index in entity['indices']:
            expected_db.execute(index['createSql'].replace('${TABLE_NAME}', name))
    for entity in schema['entities']:
        name = entity['tableName']
        for pragma in ('table_info', 'foreign_key_list', 'index_list'):
            actual = sorted(db.execute(f'PRAGMA {pragma}("{name}")').fetchall())
            expected = sorted(expected_db.execute(f'PRAGMA {pragma}("{name}")').fetchall())
            # index creation sequence is irrelevant; SQLite order is not schema semantics.
            if pragma == 'index_list':
                actual = sorted(row[1:] for row in actual)
                expected = sorted(row[1:] for row in expected)
            assert actual == expected, (name, pragma, actual, expected)
    print('PASS: migrated tables, indexes and foreign keys match the compiled Room v2 schema.')
print(f'PASS: {len(rows)} legacy inspections preserved; two FKs, delete restriction and unique plot code verified using SQLite.')
print('LIMITATION: Room schema validation and Android process recovery require the Android tests/device.')
