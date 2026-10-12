import fs from 'node:fs/promises';
import {FileBlob, SpreadsheetFile} from '@oai/artifact-tool';
const outputDir='E:/TESIS/AGROVISION-LURIN-1/docs/limon-v2';
const source='C:/Users/josem/OneDrive - Universidad Privada del Norte/UPN CICLO 2026-2/CAPSTONE PROJECT/Documentos/Cronograma_Hitos_y_Detallado_Agrovision_Lurin_Corregido.xlsx';
const wb=await SpreadsheetFile.importXlsx(await FileBlob.load(source));
await fs.mkdir(outputDir+'/qa',{recursive:true});
console.log((await wb.inspect({kind:'sheet',include:'id,name',maxChars:2000})).ndjson);
for(const s of ['Cronograma de Hitos','Cronograma Detallado']) {
  const view=await wb.render({sheetName:s,range:s==='Cronograma de Hitos'?'B2:I10':'B3:J12',scale:1,format:'png'});
  await fs.writeFile(outputDir+'/qa/source_'+s.replaceAll(' ','_')+'.png',new Uint8Array(await view.arrayBuffer()));
}
// Keep both original sheets, values, dates and historical terminology untouched.
const cp=wb.worksheets.add('CPM Sullana');
const cal=wb.worksheets.add('Calendario Sullana');
const notes=wb.worksheets.add('Control Sullana');
notes.getRange('A1:F1').merge(); notes.getRange('A1').values=[['Programación revisada AgroVision Sullana']];
const controls=[
['Estado','Propuesta de ejecución; no aprobada por asesor'],
['Inicio',new Date('2026-10-12T00:00:00Z')],
['Cierre previsto',new Date('2026-11-30T00:00:00Z')],
['Responsable con capacidad','José Miguel; sin horas adicionales de otro integrante'],
['Compromiso semanal h',12],['Reserva semanal h',4],
['Semanas',7],['Capacidad total h',null],['Compromiso total h',null],['Reserva total h',null],
['Modelo de calendario','Lunes a jueves 1,5 h; viernes 2 h; sábado 4 h; domingo 4 h de reserva'],
['CPM lógico','Precedencias fin a inicio; unidad horas de esfuerzo; no representa por sí solo disponibilidad'],
['Nivelación','Una persona; inicios no anteriores al fin de la tarea previa en el orden listado'],
['Histórico','Cronograma de Hitos y Cronograma Detallado se conservan como línea base histórica íntegra'],
['Login y CRUD','Obligatorios; SQLite local aceptado por indicación del estudiante'],
['Resultados','Ninguna aprobación, fotografía local o métrica de campo se declara ejecutada'],
['Actualización','Editar esfuerzo en CPM; revisar precedencias y disponibilidad; registrar estado y evidencia'],
['Fuente','Cronograma original de Documentos; plan acordado y disponibilidad indicada por estudiante']
];
notes.getRange('A3:B20').values=controls;
notes.getRange('B10').formulas=[['=(B7+B8)*B9']];
notes.getRange('B11').formulas=[['=B7*B9']];
notes.getRange('B12').formulas=[['=B8*B9']];
notes.getRange('B4:B5').setNumberFormat('dd/mm/yyyy');
const tasks=[
['1','Alcance acta y coordinación',4,[],'S1','Ficha y cambio propuestos'],
['2','Retirar simulación y errores',4,[0],'S1','Modelo ausente produce error'],
['3','SRS matriz e informe',4,[0],'S1','IDs y alcance coherentes'],
['4','Room migración y recuperación',6,[1],'S2','Sin pérdida; identidad A/B'],
['5','Login usuarios y parcelas CRUD',6,[3],'S2','Credenciales, relaciones y unicidad'],
['6','Manifiesto depuración y split',4,[2],'S3','Grupos disjuntos y procedencia'],
['7','Baseline MobileNetV2 FLOAT32',6,[5],'S3','Modelo real con contrato'],
['8','Integración y paridad',2,[4,6],'S3','Python y Android comparados'],
['9','CSV y recomendaciones',4,[7],'S4','Exportación coincide con ID'],
['10','Pruebas offline CRUD y migración',4,[8],'S4','Sin defectos críticos'],
['11','APK RC y documentación',4,[9],'S4','Incremento para piloto'],
['12','Campo y referencia experta',8,[10],'S5','Fotos independientes; etiquetas confirmadas'],
['13','Piloto exploratorio usuarios',4,[10],'S5','Tareas observadas y consentimiento'],
['14','Métricas y discusión',6,[11,12],'S6','Resultados completos sin inventar'],
['15','Correcciones y regresión',6,[13],'S6','Defectos resueltos y evidencia'],
['16','Ocho documentos y manuales',6,[14],'S7','Alcance y resultados coherentes'],
['17','Release video y ensayo',6,[15],'S7','Archivos y demo verificables']
];
const first=7,last=first+tasks.length-1;
cp.getRange('A1:N1').merge(); cp.getRange('A1').values=[['CPM lógico y programación nivelada de AgroVision Sullana']];
cp.getRange('A2:N2').merge(); cp.getRange('A2').values=[['Horas estimadas; una persona; el calendario no presupone ejecución ni validación académica']];
cp.getRange('A4').values=[['Duración lógica h']];cp.getRange('B4').formulas=[[`=MAX(F${first}:F${last})`]];
cp.getRange('D4').values=[['Esfuerzo comprometido h']];cp.getRange('F4').formulas=[[`=SUM(C${first}:C${last})`]];
cp.getRange('A6:N6').values=[['ID','Actividad','Esfuerzo h','Predecesoras','ES h','EF h','LS h','LF h','Holgura h','CPM','Inicio recurso h','Fin recurso h','Inicio fecha','Fin fecha']];
cp.getRange(`A${first}:N${last}`).values=tasks.map(t=>[t[0],t[1],t[2],t[3].map(p=>tasks[p][0]).join(',')||'—',null,null,null,null,null,null,null,null,null,null]);
cp.getRange('O6:R6').values=[['Sprint','Aceptación','Estado','Evidencia']];
cp.getRange(`O${first}:R${last}`).values=tasks.map(t=>[t[4],t[5],'Pendiente','']);
const calendar=[];
for(let i=0;i<49;i++){
 const date=new Date(Date.UTC(2026,9,12+i)); const dow=date.getUTCDay();
 const hours=dow===0?0:dow===6?4:dow===5?2:1.5;
 calendar.push([date,hours,dow===0?4:0,null,null,null,null]);
}
const cf=7,cl=cf+calendar.length-1;
cal.getRange('A1:G1').merge();cal.getRange('A1').values=[['Calendario de capacidad de un responsable']];
cal.getRange('A2:G2').merge();cal.getRange('A2').values=[['Domingo es reserva; las horas estimadas se asignan a un solo bloque de esfuerzo acumulado por tarea']];
cal.getRange('A6:G6').values=[['Fecha','Compromiso h','Reserva h','Inicio acumulado h','Fin acumulado h','Carga h','Saldo h']];
cal.getRange(`A${cf}:G${cl}`).values=calendar;
for(let i=0;i<calendar.length;i++){
 const r=cf+i;
 cal.getRange(`D${r}:E${r}`).formulas=[[i?`=E${r-1}`:'=0',`=D${r}+B${r}`]];
 cal.getRange(`F${r}:G${r}`).formulas=[['='+tasks.map((t,j)=>`MAX(0,MIN('CPM Sullana'!L${first+j},E${r})-MAX('CPM Sullana'!K${first+j},D${r}))`).join('+'),`=B${r}-F${r}`]];
}
// Lookup table uses only working days: no duplicate zero-capacity boundaries.
const workdays=calendar.map((r,i)=>({values:r,row:cf+i})).filter(r=>r.values[1]>0);
cal.getRange('I6:K6').values=[['Fecha operativa','Inicio h','Fin h']];
cal.getRange(`I7:K${6+workdays.length}`).values=workdays.map(r=>[r.values[0],null,null]);
for(let i=0;i<workdays.length;i++) cal.getRange(`J${7+i}:K${7+i}`).formulas=[[`=D${workdays[i].row}`,`=E${workdays[i].row}`]];
for(let i=0;i<tasks.length;i++) {
 const r=first+i,t=tasks[i]; const successors=tasks.map((x,j)=>x[3].includes(i)?first+j:null).filter(x=>x!==null);
 const es=t[3].length?'=MAX('+t[3].map(j=>`F${first+j}`).join(',')+')':'=0';
 const lf=successors.length?'=MIN('+successors.map(j=>`G${j}`).join(',')+')':'=$B$4';
 cp.getRange(`E${r}:L${r}`).formulas=[[es,`=E${r}+C${r}`,`=H${r}-C${r}`,lf,`=G${r}-E${r}`,`=IF(I${r}=0,"Sí","No")`,i?`=MAX(E${r},L${r-1})`:`=E${r}`,`=K${r}+C${r}`]];
 cp.getRange(`M${r}:N${r}`).formulas=[[
  `=IF(K${r}>=SUM('Calendario Sullana'!B7:B55),"Sin capacidad",INDEX('Calendario Sullana'!I7:I48,MATCH(K${r},'Calendario Sullana'!J7:J48,1)))`,
  `=IF(L${r}>SUM('Calendario Sullana'!B7:B55),"Sin capacidad",INDEX('Calendario Sullana'!I7:I48,MATCH(L${r}-0.000001,'Calendario Sullana'!J7:J48,1)))`
 ]];
}
cp.getRange('M7:N23').setNumberFormat('dd/mm/yyyy'); cal.getRange('A7:A55').setNumberFormat('dd/mm/yyyy');cal.getRange('I7:I48').setNumberFormat('dd/mm/yyyy');
notes.getRange('A22').values=[['Entrega final 30/11']];notes.getRange('B22').values=[['1,5 a 2 h adicionales para revisar acceso y entrega; fecha oficial por confirmar']];
for(const sh of [cp,cal,notes]) {
 sh.showGridLines=false;sh.freezePanes.freezeRows(6);
 const used=sh.getUsedRange(); used.format.font.name='Arial';used.format.font.size=11;used.format.rowHeight=32;used.format.verticalAlignment='center';used.format.wrapText=true;
 sh.getRange('A1:R1').format.font.bold=true;sh.getRange('A1:R1').format.font.size=18;
}
cp.getRange('A6:R6').format.fill='#DCE6F1';cp.getRange('A6:R6').format.font.bold=true;cp.getRange('A6:R23').format.borders={preset:'all',style:'thin',color:'#D9D9D9'};
cp.getRange('A7:R23').format.rowHeight=54;cp.getRange('A:A').format.columnWidth=6;cp.getRange('B:B').format.columnWidth=36;cp.getRange('C:L').format.columnWidth=13;cp.getRange('M:N').format.columnWidth=15;cp.getRange('O:O').format.columnWidth=8;cp.getRange('P:P').format.columnWidth=40;cp.getRange('Q:R').format.columnWidth=18;
cal.getRange('A6:K6').format.fill='#DCE6F1';cal.getRange('A6:K6').format.font.bold=true;cal.getRange('A:A').format.columnWidth=17;cal.getRange('B:G').format.columnWidth=19;cal.getRange('I:K').format.columnWidth=18;
notes.getRange('A:A').format.columnWidth=29;notes.getRange('B:B').format.columnWidth=105;notes.getRange('A3:B22').format.rowHeight=42;
wb.recalculate();
console.log((await wb.inspect({kind:'table',range:'CPM Sullana!A4:N23',include:'values,formulas',tableMaxRows:20,tableMaxCols:14,maxChars:3500})).ndjson);
console.log((await wb.inspect({kind:'match',searchTerm:'#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A|#NUM!|#NULL!',options:{useRegex:true,maxResults:30},maxChars:2500})).ndjson);
for(const [s,r] of [['CPM Sullana','A6:D15'],['CPM Sullana','E6:N15'],['CPM Sullana','A16:D23'],['CPM Sullana','E16:N23'],['CPM Sullana','O6:R23'],['Calendario Sullana','A6:G17'],['Control Sullana','A1:F12'],['Control Sullana','A13:B22']]) {
 const view=await wb.render({sheetName:s,range:r,scale:1.5,format:'png'});
 await fs.writeFile(outputDir+'/qa/'+s.replaceAll(' ','_')+'_'+r.replaceAll(':','_')+'.png',new Uint8Array(await view.arrayBuffer()));
}
const report={
 effort:cp.getRange('F4').values, logicalDuration:cp.getRange('B4').values,
 finalDates:cp.getRange('M23:N23').values,
 committed:cal.getRange('B7:B55').values.reduce((n,r)=>n+r[0],0),
 reserve:cal.getRange('C7:C55').values.reduce((n,r)=>n+r[0],0),
 deficits:cal.getRange('G7:G55').values.filter(r=>r[0]<-1e-9),
 scheduled:cp.getRange('K7:N23').values,
};
if(report.deficits.length)throw new Error('Calendar overbooked');
// Perturb one input and confirm dependent schedule recalculates, then restore.
const before=cp.getRange('L23').values[0][0];cp.getRange('C7').values=[[5]];wb.recalculate();const changed=cp.getRange('L23').values[0][0];cp.getRange('C7').values=[[4]];wb.recalculate();
report.recalculationTest={before,changed,restored:cp.getRange('L23').values[0][0]};
if(changed!==before+1||report.recalculationTest.restored!==before)throw new Error('Recalculation failed');
await fs.writeFile(outputDir+'/qa/schedule_checks.json',JSON.stringify(report,null,2));
await(await SpreadsheetFile.exportXlsx(wb)).save(outputDir+'/Cronograma_Sullana_propuesta.xlsx');
console.log(JSON.stringify(report));
