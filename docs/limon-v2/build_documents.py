"""Rebuild proposal copies with bundled python-docx. Originals are never modified."""
from pathlib import Path
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT
import json

OUT = Path(__file__).parent
TITLE = 'Aplicación móvil para apoyar la identificación de daño foliar compatible con minador en limón sutil en la Cooperativa Agraria LIMONORT-PERU Sullana 2026'
STATUS = 'Versión 2 propuesta al asesor. Fecha de elaboración 12 de octubre de 2026. La aprobación académica, las etiquetas expertas y la validación local están pendientes.'
COMMON = 'AgroVision Sullana conserva inicio de sesión local y CRUD relacional de usuarios y parcelas en SQLite mediante Room. Captura una hoja, clasifica daño compatible con minador, registra inspecciones y exporta CSV sin internet. No diagnostica déficit hídrico ni calcula severidad desde la puntuación del modelo.'
REQ = [
('RF01','Modificado','Inicio y cierre de sesión local con credenciales verificadas; sin acceso universal ni contraseñas en claro.','CP-AUTH-01 credenciales válidas e inválidas; sesión cerrada impide entrar.'),
('RF02','Modificado','Crear, consultar, editar y retirar parcelas por código único y nombre; sin GPS ni inferencia de edad fenológica.','CP-PARC-01 CRUD; duplicados rechazados; integridad al retirar parcela usada.'),
('RF03','Modificado','Capturar o importar una hoja, conservar copia privada y preparar entrada RGB 224 por 224. Error explícito para archivo no legible.','CP-IMG-01 cámara, galería, archivo corrupto y orientación.'),
('RF04','Modificado','Clasificar hoja completa en MINADOR_COMPATIBLE o SIN_SINTOMAS_VISIBLES con modelo TFLite real; admitir no concluyente.','CP-ML-01 contrato y paridad; matriz de confusión independiente.'),
('RF05','Retirado','Déficit hídrico excluido por falta de medición validada con imagen RGB. ID conservado como trazabilidad histórica.','Sin implementación activa.'),
('RF06','Modificado','Presentar acciones de observar, registrar y consultar al técnico; guardar texto original por inspección.','CP-REC-01 catálogo correcto; sin prescripción de pesticidas ni riego.'),
('RF07','Modificado','Ejecutar inferencia local y medir tiempo real; meta inicial menor a 3,5 s en dispositivo identificado.','CP-PERF-01 30 ejecuciones; mediana, p95 y máximo.'),
('RF08','Modificado','Historial por usuario y parcela; recuperar por ID; editar metadatos y eliminar con confirmación sin alterar predicción congelada.','CP-DATA-01 abrir A después de B; edición no cambia clase, score o modelo.'),
('RF09','Modificado','Persistir borrador, imagen y resultado en Room; migrar datos anteriores como LEGADO. Sin sincronización remota.','CP-ROOM-01 reinicio, migración y modo avión.'),
('RF10','Modificado','Exportar inspección a CSV compartible con identificadores, fechas, parcela, resultado, modelo y recomendaciones.','CP-CSV-01 escaping y correspondencia exacta con la inspección.'),
('RF11','Nuevo','Crear, consultar, editar y retirar usuarios locales con identidad única; proteger usuario de sesión y referencias existentes.','CP-USR-01 CRUD; contraseña nueva verificable y registros relacionados preservados.'),
]
METRICS = [
('Exactitud','Accuracy ≥85% y precision y recall ≥80% como metas propuestas','Matriz de confusión por clase, versión y conjunto; resultados pendientes'),
('Oportunidad','Tiempo desde captura confirmada hasta resultado visible','30 ejecuciones; mediana, p95, máximo, dispositivo y condición de inicio'),
('Usabilidad','Completar login, parcela, captura, consulta y exportación','Piloto exploratorio de 5 usuarios elegibles; tareas, errores y valoración'),
('Fiabilidad','Recuperar inspección y foto tras cierre de proceso','Pruebas de persistencia, migración e identidad A/B'),
]
SPRINTS = [
('S1','12–18 oct.','12','Alcance y honestidad','Acta y SRS; fallback y sync falso retirados'),
('S2','19–25 oct.','12','Room login y CRUD','Migración, usuarios y parcelas; protocolo de datos'),
('S3','26 oct.–1 nov.','12','Datos y modelo real','Manifiesto agrupado, baseline y contrato FLOAT32'),
('S4','2–8 nov.','12','Integración y RC','APK offline, historial por ID y CSV'),
('S5','9–15 nov.','12','Piloto y campo','Etiquetas expertas y fichas de tareas sin inventar resultados'),
('S6','16–22 nov.','12','Evaluación y resultados','Métricas reales, defectos y discusión'),
('S7','23–29 nov.','12','Cierre y sustentación','Documentos coherentes, APK y ensayo'),
]

def doc(name, heading):
    d=Document(); sec=d.sections[0]
    sec.page_width=Inches(8.5); sec.page_height=Inches(11)
    sec.top_margin=sec.bottom_margin=Inches(.75)
    sec.left_margin=sec.right_margin=Inches(.75)
    for n in ['Normal','Title','Heading 1','Heading 2']:
        s=d.styles[n]; s.font.name='Arial'; s.font.color.rgb=RGBColor(0,0,0)
        s.font.size=Pt(11 if n=='Normal' else 20 if n=='Title' else 14 if n=='Heading 1' else 12)
        s.paragraph_format.space_after=Pt(7)
    d.styles['Normal'].paragraph_format.line_spacing=1.12
    d.add_paragraph(heading,style='Title'); d.add_paragraph('AgroVision Sullana'); d.add_paragraph(STATUS)
    d.core_properties.title=heading; d.core_properties.subject=TITLE
    return d

def section(d,h,*paras):
    d.add_heading(h,1)
    for p in paras: d.add_paragraph(p)

def table(d, headers, rows, widths=None):
    t=d.add_table(rows=1,cols=len(headers)); t.autofit=False
    widths=widths or [7/len(headers)]*len(headers)
    for c,w in zip(t.columns,widths): c.width=Inches(w)
    for c,txt in zip(t.rows[0].cells,headers): c.text=txt
    for row in rows:
        for c,txt in zip(t.add_row().cells,row): c.text=str(txt)
    for i,row in enumerate(t.rows):
        if i==0:
            rep=OxmlElement('w:tblHeader'); row._tr.get_or_add_trPr().append(rep)
        for j,c in enumerate(row.cells):
            c.width=Inches(widths[j]); c.vertical_alignment=WD_CELL_VERTICAL_ALIGNMENT.CENTER
            pr=c._tc.get_or_add_tcPr()
            shade=OxmlElement('w:shd'); shade.set(qn('w:fill'),'E6EEF4' if i==0 else 'F5F7F9' if i%2==0 else 'FFFFFF'); pr.append(shade)
            margin=OxmlElement('w:tcMar')
            for side in ['top','left','bottom','right']:
                el=OxmlElement('w:'+side); el.set(qn('w:w'),'90'); el.set(qn('w:type'),'dxa'); margin.append(el)
            pr.append(margin)
            borders=OxmlElement('w:tcBorders')
            for side in ['top','left','bottom','right']:
                el=OxmlElement('w:'+side); el.set(qn('w:val'),'single'); el.set(qn('w:sz'),'4'); el.set(qn('w:color'),'D9D9D9'); borders.append(el)
            pr.append(borders)
            for p in c.paragraphs:
                p.paragraph_format.space_after=Pt(3)
                for r in p.runs: r.font.size=Pt(10); r.bold=(i==0)
    d.add_paragraph()

def save(d, name):
    d.save(OUT/name)
    lines=[p.text for p in d.paragraphs]
    for t in d.tables:
        lines.extend(' | '.join(c.text for c in r.cells) for r in t.rows)
    (OUT/(Path(name).stem+'.md')).write_text('\n\n'.join(lines),encoding='utf-8')

def dataset(d):
    section(d,'Datos y referencia agronómica',
        'La copia externa inventariada contiene 17.586 JPG en 18 carpetas. Se seleccionan Citrus Leafminer (900) y Healthy (1.638) para el baseline. La fuente candidata es Mendeley Data 8d9fv6kpt3 versión 2, de Bangladesh; su atribución, versión y correspondencia con la copia deben confirmarse. No se atribuyen estos datos a LIMONORT ni se presupone limón sutil.',
        'Las 16 clases restantes solo servirán para explorar confusiones por otras afecciones. Depurar archivos, agrupar duplicados exactos y similares antes de dividir aproximadamente 70/15/15. Mantener fotografías correlacionadas en un mismo grupo, fijar semilla y aplicar aumentación solo al entrenamiento.',
        'Meta operativa local: 60 fotografías independientes con referencia experta y 5 usuarios para piloto exploratorio. Estas cantidades son metas de factibilidad, no suficiencia estadística ni participantes confirmados. Registrar planta y parcela cuando sea posible. No reutilizar campo para seleccionar umbral ni entrenar.',
        'El técnico confirma minador y etapa inicial. La IA puede proponer etiquetas, nunca reemplazar la referencia independiente. Si no hay casos iniciales verificados, el estudio no concluye identificación temprana. Los resultados y permisos de campo permanecen pendientes.')
    report=OUT.parent.parent/'ml'/'artifacts'/'dataset_report.json'
    if report.exists():
        section(d,'Depuración ejecutada del conjunto externo',
            'El manifiesto generado el 12 de octubre conserva 1.837 imágenes de las dos clases seleccionadas: 1.286 entrenamiento, 276 validación y 275 test. Se excluyen 701 imágenes seleccionadas por grupos de similitud que cruzan carpetas. La agrupación pHash es conservadora y requiere revisión visual de conflictos; no acredita independencia por planta o sesión.',
            'El conjunto test externo incluye 99 imágenes MINADOR_COMPATIBLE y 176 SIN_SINTOMAS_VISIBLES. Fue consultado en una evaluación inicial exploratoria; no puede presentarse como test nunca visto del protocolo actual ni usarse para ajustar umbrales. La evaluación local independiente sigue pendiente. Fuente de recuentos: ml/artifacts/dataset_report.json; semilla 20261012. La proximidad visual puede agrupar imágenes distintas; conservar manifiesto y criterio para reproducir exclusiones.')

def architecture(d):
    section(d,'Arquitectura y almacenamiento',COMMON,
        'Kotlin y Compose implementan presentación y estado observable; ViewModels coordinan casos de uso; repositorios separan reglas y persistencia. Room conserva SQLite relacional con consultas tipadas, transacciones y migraciones. TFLite ejecuta el modelo en el dispositivo. Esta separación conserva responsabilidades equivalentes a controlador, servicio y repositorio en aplicaciones Java, sin exigir servidor para un piloto offline.',
        'El núcleo físico propuesto tiene tres tablas: usuarios, parcelas e inspecciones. Las inspecciones referencian usuario y parcela. Catálogo y modelo permanecen como manifiestos inmutables de assets y se identifican en cada resultado; no se representan como tablas SQL inexistentes. Claves primarias estables, claves foráneas e índices únicos evitan duplicados y huérfanos. Las bajas de registros referenciados se restringen o archivan; no borran inspecciones en cascada.',
        'El CRUD de inspecciones edita notas y asociación de parcela, conservando clase, puntuación, estado de inferencia, modelo, tiempos y recomendaciones calculadas. Cambiar una foto implica nueva inspección. No presentar login local como verificación remota o protección ante un teléfono comprometido.')

d=doc('','Recuento T1 revisado')
section(d,'Título de investigación',TITLE)
section(d,'Problema y propósito',
    'Pregunta propuesta: ¿qué desempeño presenta una aplicación móvil local para apoyar la identificación de daño foliar compatible con minador en limón sutil de LIMONORT-PERU, Sullana, durante 2026?',
    'Objetivo general: desarrollar y evaluar el desempeño técnico y operativo de la aplicación en clasificación foliar, oportunidad de respuesta y uso exploratorio. No atribuir mejora causal de productividad o reducción de pérdidas sin diseño y medición específicos.',
    'Objetivos específicos: definir referencia agronómica y protocolo; construir pipeline reproducible; implementar inferencia y CRUD durable; medir exactitud, tiempos y operación con usuarios.')
section(d,'Variables y diseño',
    'X: aplicación móvil local, operacionalizada por funciones verificables de login, CRUD, captura, inferencia y exportación. Y: desempeño del apoyo a identificación de daño compatible con minador, medido por clasificación, tiempo y operación.',
    'Diseño propuesto: desarrollo tecnológico con evaluación de desempeño, sin inferir causalidad. Las definiciones conceptuales, fuentes académicas y ajuste del título requieren revisión del asesor. La Mobile App Rating Scale del T1 original no reemplaza la matriz de confusión ni acredita validez agronómica.')
dataset(d); table(d,['Dimensión','Indicador','Instrumento'],METRICS,[1.15,2.45,3.4])
save(d,'01_Recuento_T1_Sullana_propuesta.docx')

d=doc('','Informe de proyecto AgroVision Sullana')
section(d,'Resumen del proyecto',COMMON,'El informe propone una evaluación tecnológica delimitada a daño compatible con minador. La clasificación de una fotografía no confirma presencia del insecto, causalidad del síntoma o etapa inicial. La validación local se realizará contra referencia técnica independiente.')
section(d,'Problema y objetivos','La oportunidad y reproducibilidad del registro foliar se estudiarán en LIMONORT-PERU. Las causas del diagnóstico tardío son hipótesis a contrastar con observación y entrevista; no se afirman tasas de pérdida, demora o conectividad sin evidencia.', 'Objetivo general: desarrollar y evaluar la aplicación local. Objetivos específicos: protocolo experto, modelo reproducible, arquitectura durable y evaluación de exactitud, tiempo y usabilidad.')
architecture(d); dataset(d)
section(d,'Metodología y resultados','Separar evaluación externa, evaluación local y piloto de usuarios. Congelar modelo y umbral antes de consultar test. Presentar conteos, errores, abstenciones y métricas por conjunto; no mezclar accuracy de laboratorio con desempeño local.', 'Resultados pendientes: no hay cifras experimentales de campo, satisfacción, mejora productiva ni aprobaciones en este documento. Completar resultados, discusión y conclusiones únicamente con evidencia identificada por versión.')
section(d,'Avance técnico preliminar',
    'Se generó un baseline MobileNetV2 FLOAT32 y se incorporó su asset TFLite con manifiesto y fixtures de paridad al proyecto. El entrenamiento utilizó datos externos depurados y una cabeza clasificadora sobre extracción de características. Esta evidencia acredita generación del modelo, no prueba Android en un teléfono ni validez para LIMONORT.',
    'El umbral de abstención inicial es provisional y requiere validación independiente. Una evaluación argmax exploratoria anterior no debe presentarse como desempeño final del protocolo con abstención. Los recibos experimentales se conservan separados de los resultados finales; no se publica una cifra de exactitud local inexistente.')
section(d,'Antecedentes y sustentación','Revisar al menos cinco antecedentes con fuente primaria, tarea, cultivo, conjunto, partición y limitación. Los antecedentes anteriores de otro cultivo pueden permanecer como comparación contextual expresamente identificada; no constituyen validación del modelo actual.', 'Fundamentar MobileNetV2 y TFLite como opciones de implementación; comparar inferencia local con servidor en términos de dependencia de conexión, privacidad y mantenimiento. La elección de tecnologías actuales no prueba calidad por sí misma: demostrar migraciones, integridad, pruebas y separación de responsabilidades.')
table(d,['Dimensión','Indicador','Instrumento'],METRICS,[1.15,2.45,3.4]); save(d,'02_Informe_Proyecto_Sullana_propuesta.docx')

d=doc('','Especificación de requisitos y trazabilidad')
section(d,'Propósito y línea base',COMMON,'Se conservan IDs RF01 a RF10 del documento original; RF11 incorpora CRUD de usuarios. Todos son requisitos de la propuesta, no declaración de implementación terminada. El retiro de RF05 preserva su trazabilidad histórica.')
for rid, status, desc, test in REQ:
    section(d,rid+' '+status,desc,'Aceptación: '+test)
section(d,'Restricciones de calidad','Se conservan los identificadores RNF originales y su dimensión. Las metas son propuestas pendientes de ratificación. Ninguna cifra de la siguiente tabla representa un resultado ya medido.')
table(d,['ID y estado','Dimensión','Revisión propuesta'],[
('RNF01 modificado','Exactitud','Accuracy ≥85%; precision y recall ≥80% por clase en test externo y campo separados. No extrapolar a toda enfermedad.'),
('RNF02 modificado','Reglas','Cobertura del catálogo de dos clases y estados error/no concluyente; no combinar déficit hídrico.'),
('RNF03 modificado','Tiempo','Meta <3,5 s desde captura confirmada hasta resultado visible; 30 ejecuciones en teléfono identificado, frío/caliente registrados.'),
('RNF04 modificado','Recursos','Usar FLOAT32; medir tamaño real y memoria en dispositivo. Límites históricos 45 MB/250 MB por ratificar, no prometer INT8.'),
('RNF05 modificado','Offline','Funciones locales sin red; recuperación de registros probada. Retirar garantía universal de pérdida cero.'),
('RNF06 retirado','Recuperación remota','Sin servidor ni transferencia. La recuperación local se verifica en RNF05 y RF09.'),
('RNF07 modificado','Usabilidad','Piloto exploratorio de 5 usuarios; registrar tareas y errores. SUS y meta histórica 85 solo si instrumento y criterio ratificados.'),
('RNF08 modificado','Prevención de errores','Imagen/modelo inválido causa error explícito; no afirmar filtro Laplacian validado ni detección automática universal de mala calidad.'),
('RNF09 modificado','Seguridad','Credenciales locales derivadas con sal individual y permisos de administración; TLS no aplica sin comunicación remota.'),
('RNF10 modificado','Compatibilidad','Validar API mínima declarada en Gradle y teléfono piloto. Límites previos 2 GB/8 MP no implican soporte universal.')
],[1.15,1.15,4.7])
section(d,'Restricciones adicionales','RNF-V2-01 integridad relacional: transacciones, FK y únicos. RNF-V2-02 reproducibilidad: hash, contrato y versión. RNF-V2-03 mantenibilidad: lógica fuera de composables y migración probada. Son nuevos identificadores y no sustituyen silenciosamente requisitos anteriores.')
table(d,['Requisito','HU y prueba','Evidencia actual'],[(r[0],'HU'+r[0][2:]+' '+r[3].split(' ')[0],'Pendiente de prueba y registro') for r in REQ if r[0]!='RF05'],[1,2.8,3.2])
section(d,'Cambios respecto al alcance anterior','Se retiran como histórico las promesas de nube, YOLO, cajas, déficit hídrico, GPS y PDF. Login, SQLite y CRUD se mantienen por requisito docente. Las etiquetas anteriores no se reinterpretan como limón; los registros migrados quedan LEGADO. Firma y aprobación del asesor: pendientes.')
save(d,'03_SRS_RTM_Sullana_propuesta.docx')

d=doc('','Estructura de desglose del trabajo')
section(d,'Línea base de entregables','La EDT agrupa entregables verificables. Se planifican 84 horas comprometidas y 28 horas de reserva en siete semanas; no son horas ya ejecutadas. La entrega del 30 de noviembre dispone de 1,5 a 2 horas adicionales.')
table(d,['EDT','Paquete','Aceptación'],[
('1','Alcance y requisitos','Ficha, acta, SRS y matriz sujetos a revisión'),('2','Datos y referencia','Manifiesto, grupos, split y protocolo experto'),('3','Persistencia y CRUD','Usuarios, parcelas y consultas; migración sin pérdida'),('4','Modelo e inferencia','MobileNetV2 FLOAT32 real con contrato y paridad'),('5','Aplicación integrada','Login, captura, historial durable y CSV offline'),('6','Evaluación','Campo independiente, tiempos y piloto registrados'),('7','Cierre','Resultados reales, manuales, APK y sustentación')],[.5,2.05,4.45])
table(d,['Sprint','Fechas','Horas','Foco','Entregable'],SPRINTS,[.55,1.1,.5,1.5,3.35])
section(d,'Exclusiones y protección de evidencia','No se incorporan servidor, autenticación remota, sincronización, déficit hídrico, severidad inferida, segmentación o nuevos modelos. La supresión de una función debe reflejarse en requisitos y pruebas. El CRUD no debe permitir editar resultados del modelo para producir una demo favorable.')
save(d,'04_EDT_Sullana_propuesta.docx')

d=doc('','Plan de gestión del proyecto')
section(d,'Objetivo y responsabilidades',COMMON,'José Miguel administra la ejecución y revisión de cambios. No se presupone capacidad adicional de otro integrante. El docente revisa alcance y criterios; el experto valida etiquetas; agricultores participan si existe autorización y consentimiento.')
table(d,['Sprint','Fechas','Horas','Foco','Entregable'],SPRINTS,[.55,1.1,.5,1.5,3.35])
section(d,'Control de trabajo','Capacidad: lunes a jueves 1,5 h por día, viernes 2 h, sábado 4 h y domingo hasta 4 h. Comprometer 12 h semanales y reservar 4 h. Cada tarea incluye revisión del trabajo de agentes y pruebas. No asignar dos tareas simultáneas al mismo responsable.', 'El cronograma calcula CPM por precedencias fin a inicio y presenta calendario con disponibilidad. La ruta lógica y el calendario con recursos limitados se identifican separadamente. Si una tarea consume reserva, recortar alcance postergable antes que validez o pruebas. Congelar funciones el 8 de noviembre.')
table(d,['Riesgo','Señal','Respuesta'],[('Referencia experta ausente','Etiquetas sin confirmación al 25/10','Gestionar técnico externo; no reemplazar por IA'),('Integración atrasada','Sin modelo Android al 01/11','Usar reserva; mantener baseline único'),('Métrica insuficiente','Test por debajo de meta','Informar resultados; no ajustar sobre test'),('Campo sin etapa inicial','No hay casos iniciales confirmados','Limitar título y conclusión'),('CRUD aumenta carga','Reserva de S2 consumida','Reducir adornos y mantener integridad/login')],[1.65,2.2,3.15])
section(d,'Gobierno de cambios','La acta registra decisión, requisitos afectados, costo y aceptación académica pendiente. Cada sprint termina con incremento demostrado y RTM actualizado. No marcar aprobación docente ni ejecución de campo sin evidencia. Conservar la línea base histórica de agosto y septiembre sin reescribir sus fechas.')
save(d,'05_Plan_Proyecto_Sullana_propuesta.docx')

d=doc('','Plan de gestión de la calidad')
section(d,'Objetivo y medición','Verificar funcionamiento Android y evaluar desempeño ML por separado. No sustituir precisión por usabilidad ni convertir objetivos en resultados.')
table(d,['Dimensión','Meta o indicador','Evidencia'],METRICS,[1.15,2.45,3.4])
section(d,'Pruebas del producto','Probar login válido e inválido; unicidad de usuarios y parcelas; edición y bajas con registros relacionados; migración; reinicio; lectura A/B; imagen corrupta y modelo ausente; contrato de tensores; exportación CSV y modo avión.', 'La edición de metadatos conserva clase, score, versión, tiempos y recomendaciones originales. El test de migración parte de una base previa realista y compara recuentos y significado de etiquetas. No usar una reconstrucción destructiva como estrategia de actualización.')
section(d,'Evaluación del modelo','Reportar accuracy, precision, recall y F1 por clase, matriz de confusión, cobertura de resultados concluyentes y tasa de abstención. Informar evaluación completa con abstenciones y métricas sobre resultados concluyentes por separado. Seleccionar umbral en validación; test y campo quedan reservados.', 'Comparar Python y TFLite Android sobre fotografías fijas, con tolerancia documentada; verificar resize, orientación, normalización, dtype y orden de clases. Congelar hash y configuración antes del piloto.')
section(d,'Estado del protocolo', 'El test externo ya se consultó en una corrida exploratoria. El protocolo actual conserva ese recibo y no presenta el conjunto como nunca visto. El umbral 0,7 fue seleccionado con validación y permanece pendiente de aplicabilidad local. No ajustar sobre test externo ni sobre campo; recoger una referencia local independiente para la evaluación final.')
section(d,'Validación humana y cierre','El experto evalúa síntomas y etapa con ficha independiente antes de comparar con la app. Registrar desacuerdos y casos ambiguos. Los cinco usuarios son piloto exploratorio, no muestra representativa por defecto.', 'Salida: cero errores críticos que fabriquen resultados o pierdan registros; pruebas vinculadas a requisitos; resultados científicos reportados aunque no alcancen metas. La aceptación académica no se presume.')
save(d,'06_Plan_Calidad_Sullana_propuesta.docx')

d=doc('','Plan de gestión de los costos')
section(d,'Base de estimación','La capacidad disponible limita el presupuesto de trabajo: 7 semanas por 16 h equivalen a 112 h, distribuidas en 84 h comprometidas y 28 h de reserva. La entrega final añade 1,5 a 2 h. Son estimaciones de capacidad, no horas facturadas ni ejecutadas.', 'Se sustituye la estimación histórica por puntos de función por un presupuesto de esfuerzo compatible con la dedicación real. No se arrastra la tarifa histórica de S/ 25 por hora como costo aprobado.')
table(d,['Concepto','Unidad','Cantidad propuesta','Tarifa y costo'],[('Trabajo propio','Hora','84 + 28 reserva','Valoración opcional pendiente'),('Entrega final','Hora','1,5 a 2','Valoración opcional pendiente'),('Herramientas de IA','Mes / suscripción','Según uso autorizado','Costo incremental pendiente'),('Entrenamiento','Hora de cómputo','Según ejecución medida','Local; GPU externa solo con presupuesto'),('Movilidad a campo','Visita','Según protocolo y agenda','Cotización pendiente'),('Materiales','Unidad','Según necesidad real','Cotización pendiente')],[1.7,1.1,1.65,2.55])
section(d,'Control financiero','Separar desembolso incremental, recursos ya disponibles y valoración del tiempo propio. No confundir suscripciones ya pagadas con nuevo gasto. Costo de recurso = cantidad real por tarifa respaldada; total monetario no calculable hasta completar tarifas y unidades.', 'Registrar fecha, concepto, moneda, tipo de cambio cuando corresponda y comprobante. Revisar por sprint. Los costos Azure quedan retirados del alcance activo; no se inventa ahorro o viabilidad económica sin comparación consistente.')
section(d,'Reserva y decisiones','La reserva es tiempo disponible, no autorización para gasto adicional. Si se requiere nube para entrenar, estimar costo antes de ejecutarla. Comparar presupuesto con consumo registrado; no declarar desviación favorable mientras falten costos.')
save(d,'07_Plan_Costos_Sullana_propuesta.docx')

d=doc('','Registro de interesados')
section(d,'Propósito y alcance','Identificar decisiones y participación necesarias para LIMONORT-PERU. Los roles no implican permisos, presencia o aprobación ya obtenidos. No trasladar nombres de productores del alcance anterior.')
table(d,['Interesado o rol','Necesidad y responsabilidad','Estado'],[('José Miguel Perea','Implementación, revisión de agentes y coordinación','Responsable de capacidad planificada'),('Docente y asesor','Revisar alcance, método y criterios; login y CRUD requeridos','SQLite local aceptado según indicación del estudiante; aprobación formal pendiente'),('Técnico agronómico','Confirmar especie, síntoma y etapa; resolver ambiguos','Disponibilidad indicada; nombre y agenda por confirmar'),('Representante LIMONORT','Autorizar acceso y coordinación de parcelas','Contacto y permiso por confirmar'),('Usuarios piloto','Realizar tareas y aportar valoración con consentimiento','Meta de cinco; reclutamiento pendiente'),('Otros integrantes del trabajo original','Revisar contribución y capacidad efectiva','No se presuponen horas ni compromisos')],[1.55,3.75,1.7])
section(d,'Participación y privacidad','Usar códigos de participante y planta en los registros de evaluación. No exportar contraseñas ni datos personales innecesarios. Informar finalidad, uso de fotografías y posibilidad de retiro. El experto conserva referencia independiente de la predicción durante etiquetado.', 'Revisión con asesor antes de congelar método; coordinación agronómica antes de campo; devolución de resultados agregados cuando exista autorización. Las actas deben registrar fecha real y respuesta, sin firmas automáticas.')
save(d,'08_Registro_Interesados_Sullana_propuesta.docx')

d=doc('','Acta de cambio y protocolo de evaluación')
section(d,'Solicitud de cambio',TITLE,COMMON,'Motivo: transición del cultivo y lugar; disponibilidad de 16 h semanales; exigencia de login y CRUD SQLite local. Estado: propuesta técnica aceptada para implementación por el estudiante; aprobación académica formal pendiente. No contiene firmas ni resultados de campo.')
table(d,['Objeto','Decisión'],[('Catálogo','Dos clases de daño compatible; sin diagnóstico hídrico'),('Persistencia','Usuarios, parcelas, inspecciones y referencia versionada'),('Login','Local con credenciales reales, sin demo universal'),('Exportación','CSV; imagen privada'),('Método','Evaluación de desempeño; no causalidad'),('Datos','Externos separados de campo y participantes')],[1.35,5.65])
dataset(d)
section(d,'Matriz de consistencia','Problema: desempeño del apoyo móvil. Objetivo general: desarrollar y evaluar. X: aplicación implementada y verificada. Y: desempeño medido. Técnica: experimentación técnica y observación de tareas. Instrumentos: manifiesto, referencia experta, matriz de confusión, cronometraje y ficha de usuario. Unidades: fotografía, ejecución y usuario, analizadas por separado.')
section(d,'Ficha de captura y etiqueta','Campos por completar: código de imagen; parcela; planta; fecha; cultivo confirmado; órgano; iluminación observada; dispositivo; permiso; técnico; clase de referencia; etapa inicial sí/no/no determinable; criterio y observaciones. Etiquetar antes de visualizar salida del modelo.', 'No concluyente no equivale a sano. No usar imágenes ambiguas como clase segura sin criterio documentado. Mantener archivo original, hash y partición.')
section(d,'Ficha de tiempos y usuario','Por ejecución: ID, modelo/hash, teléfono, condición inicial, inicio, resultado visible, duración, estado y error. Registrar 30 ejecuciones sin descartar lentas. Por usuario: código, tareas login/parcela/captura/historial/CSV, éxito, ayuda, errores, valoración y observaciones. La meta es exploratoria.')
section(d,'Ishikawa propuesto','Problema central a confirmar: dificultad para registrar y revisar oportunamente daño foliar compatible con minador. Categorías de hipótesis: método de inspección, conocimiento de síntomas, disponibilidad de registro, calidad fotográfica y condiciones de campo. Cada causa debe tener observación o entrevista identificada. No incluir riego ni afirmar pérdidas no medidas.')
save(d,'09_Acta_Protocolo_Matriz_Sullana_propuesta.docx')

(OUT/'REQUISITOS_MVP.json').write_text(json.dumps({'version':'2-propuesta','title':TITLE,'requirements':[dict(id=r[0],status=r[1],description=r[2],acceptance=r[3]) for r in REQ]},ensure_ascii=False,indent=2),encoding='utf-8')
print('Created 9 proposal DOCX and readable sources')
