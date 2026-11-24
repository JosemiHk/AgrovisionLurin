from pathlib import Path
from zipfile import ZipFile
from lxml import etree
from openpyxl import load_workbook
import json, hashlib

out=Path(__file__).parent
src=Path('C:/Users/josem/OneDrive - Universidad Privada del Norte/UPN CICLO 2026-2/CAPSTONE PROJECT/Documentos')
ns={'w':'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
docs=[]
for p in sorted(out.glob('*.docx')):
    with ZipFile(p) as z:
        xml=etree.fromstring(z.read('word/document.xml'))
        text=' '.join(xml.xpath('//w:t/text()',namespaces=ns))
        assert 'propuesta al asesor' in text
        assert 'pendientes' in text
        assert 'Sullana' in text
        docs.append({'file':p.name,'characters':len(text),'tables':len(xml.xpath('//w:tbl',namespaces=ns)),'zip_valid':z.testzip() is None,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
assert len(docs)==9
original=load_workbook(src/'Cronograma_Hitos_y_Detallado_Agrovision_Lurin_Corregido.xlsx',data_only=False)
revised=load_workbook(out/'Cronograma_Sullana_propuesta.xlsx',data_only=False)
differences=[]
for sh in original:
    target=revised[sh.title]
    for row in sh:
        for c in row:
            if c.value!=target[c.coordinate].value:
                differences.append((sh.title,c.coordinate,str(c.value),str(target[c.coordinate].value)))
assert not differences,differences[:5]
cached=load_workbook(out/'Cronograma_Sullana_propuesta.xlsx',data_only=True)
schedule=cached['CPM Sullana']
assert schedule['F4'].value==84
assert cached['Control Sullana']['B10'].value==112
assert schedule['N23'].value.day==28
assert all(cached['Calendario Sullana'].cell(r,7).value>=0 for r in range(7,56))
checks={'documents':docs,'docx_visual_validation':'NOT_COMPLETED: bundled LibreOffice soffice.exe unavailable; no page PNGs generated','xlsx_historical_values_and_formulas_unchanged':not differences,'xlsx_formula_cache_checked':True,'xlsx_native_excel_execution':'Not performed','sources':[{'file':p.name,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in sorted(src.glob('*')) if p.suffix in ['.docx','.xlsx']]}
(out/'qa'/'artifact_checks.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'docx_count':len(docs),'historical_differences':len(differences),'final_task_finish':str(schedule['N23'].value),'docx_visual_validation':checks['docx_visual_validation']},ensure_ascii=False))
