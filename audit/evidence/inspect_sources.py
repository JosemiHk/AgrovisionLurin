import json
import pathlib
import sqlite3
import openpyxl
from PIL import Image
import sys

sys.stdout.reconfigure(encoding='utf-8')

root = pathlib.Path('E:/TESIS/AGROVISION-LURIN-1')
src = pathlib.Path('C:/Users/josem/OneDrive - Universidad Privada del Norte/UPN CICLO 2026-2/CAPSTONE PROJECT/Documentos')
out = []
for path in [root/'database/agrovision_lurin.sqlite', root/'agrovision_database.sqlite']:
    with sqlite3.connect(path.as_uri()+'?mode=ro', uri=True) as conn:
        schema = list(conn.execute("select name,sql from sqlite_master where type in ('table','view')"))
        counts = {name: conn.execute('select count(*) from "'+name+'"').fetchone()[0] for name, _ in schema if name != 'sqlite_sequence'}
        out.append({'path':str(path),'schema':schema,'counts':counts,'integrity':conn.execute('pragma integrity_check').fetchall(),'fk_errors':conn.execute('pragma foreign_key_check').fetchall()})
(root/'audit/evidence/sqlite_inventory.json').write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps([{k:v for k,v in record.items() if k!='schema'} for record in out], ensure_ascii=False, indent=2))
try:
    wb = openpyxl.load_workbook(next(src.glob('*.xlsx')), data_only=False)
except PermissionError:
    wb = []
    print('Cronograma locked by another process; content not inspected.')
lines=[]
for ws in wb:
    lines.append('SHEET '+ws.title)
    for row in ws.iter_rows():
        values=['%s=%s'%(c.coordinate,c.value) for c in row if c.value is not None]
        if values: lines.append(' | '.join(values))
text='\n'.join(lines)
(root/'audit/evidence/cronograma.txt').write_text(text, encoding='utf-8')
print(text)
im=Image.open(src/'ishikawa corregido limonort (1).png').convert('RGBA')
print('Ishikawa mode',im.mode,'alpha extrema',im.getchannel('A').getextrema())
bg=Image.new('RGBA',im.size,'white')
bg.alpha_composite(im)
bg.convert('RGB').save(root/'audit/evidence/ishikawa_white_qa.png')
