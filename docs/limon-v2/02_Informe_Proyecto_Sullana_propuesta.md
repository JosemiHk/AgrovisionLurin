Informe de proyecto AgroVision Sullana

AgroVision Sullana

Versión 2 propuesta al asesor. Fecha de elaboración 12 de octubre de 2026. La aprobación académica, las etiquetas expertas y la validación local están pendientes.

Resumen del proyecto

AgroVision Sullana conserva inicio de sesión local y CRUD relacional de usuarios y parcelas en SQLite mediante Room. Captura una hoja, clasifica daño compatible con minador, registra inspecciones y exporta CSV sin internet. No diagnostica déficit hídrico ni calcula severidad desde la puntuación del modelo.

El informe propone una evaluación tecnológica delimitada a daño compatible con minador. La clasificación de una fotografía no confirma presencia del insecto, causalidad del síntoma o etapa inicial. La validación local se realizará contra referencia técnica independiente.

Problema y objetivos

La oportunidad y reproducibilidad del registro foliar se estudiarán en LIMONORT-PERU. Las causas del diagnóstico tardío son hipótesis a contrastar con observación y entrevista; no se afirman tasas de pérdida, demora o conectividad sin evidencia.

Objetivo general: desarrollar y evaluar la aplicación local. Objetivos específicos: protocolo experto, modelo reproducible, arquitectura durable y evaluación de exactitud, tiempo y usabilidad.

Arquitectura y almacenamiento

AgroVision Sullana conserva inicio de sesión local y CRUD relacional de usuarios y parcelas en SQLite mediante Room. Captura una hoja, clasifica daño compatible con minador, registra inspecciones y exporta CSV sin internet. No diagnostica déficit hídrico ni calcula severidad desde la puntuación del modelo.

Kotlin y Compose implementan presentación y estado observable; ViewModels coordinan casos de uso; repositorios separan reglas y persistencia. Room conserva SQLite relacional con consultas tipadas, transacciones y migraciones. TFLite ejecuta el modelo en el dispositivo. Esta separación conserva responsabilidades equivalentes a controlador, servicio y repositorio en aplicaciones Java, sin exigir servidor para un piloto offline.

El núcleo físico propuesto tiene tres tablas: usuarios, parcelas e inspecciones. Las inspecciones referencian usuario y parcela. Catálogo y modelo permanecen como manifiestos inmutables de assets y se identifican en cada resultado; no se representan como tablas SQL inexistentes. Claves primarias estables, claves foráneas e índices únicos evitan duplicados y huérfanos. Las bajas de registros referenciados se restringen o archivan; no borran inspecciones en cascada.

El CRUD de inspecciones edita notas y asociación de parcela, conservando clase, puntuación, estado de inferencia, modelo, tiempos y recomendaciones calculadas. Cambiar una foto implica nueva inspección. No presentar login local como verificación remota o protección ante un teléfono comprometido.

Datos y referencia agronómica

La copia externa inventariada contiene 17.586 JPG en 18 carpetas. Se seleccionan Citrus Leafminer (900) y Healthy (1.638) para el baseline. La fuente candidata es Mendeley Data 8d9fv6kpt3 versión 2, de Bangladesh; su atribución, versión y correspondencia con la copia deben confirmarse. No se atribuyen estos datos a LIMONORT ni se presupone limón sutil.

Las 16 clases restantes solo servirán para explorar confusiones por otras afecciones. Depurar archivos, agrupar duplicados exactos y similares antes de dividir aproximadamente 70/15/15. Mantener fotografías correlacionadas en un mismo grupo, fijar semilla y aplicar aumentación solo al entrenamiento.

Meta operativa local: 60 fotografías independientes con referencia experta y 5 usuarios para piloto exploratorio. Estas cantidades son metas de factibilidad, no suficiencia estadística ni participantes confirmados. Registrar planta y parcela cuando sea posible. No reutilizar campo para seleccionar umbral ni entrenar.

El técnico confirma minador y etapa inicial. La IA puede proponer etiquetas, nunca reemplazar la referencia independiente. Si no hay casos iniciales verificados, el estudio no concluye identificación temprana. Los resultados y permisos de campo permanecen pendientes.

Depuración ejecutada del conjunto externo

El manifiesto generado el 12 de octubre conserva 1.837 imágenes de las dos clases seleccionadas: 1.286 entrenamiento, 276 validación y 275 test. Se excluyen 701 imágenes seleccionadas por grupos de similitud que cruzan carpetas. La agrupación pHash es conservadora y requiere revisión visual de conflictos; no acredita independencia por planta o sesión.

El conjunto test externo incluye 99 imágenes MINADOR_COMPATIBLE y 176 SIN_SINTOMAS_VISIBLES. Fue consultado en una evaluación inicial exploratoria; no puede presentarse como test nunca visto del protocolo actual ni usarse para ajustar umbrales. La evaluación local independiente sigue pendiente. Fuente de recuentos: ml/artifacts/dataset_report.json; semilla 20261012. La proximidad visual puede agrupar imágenes distintas; conservar manifiesto y criterio para reproducir exclusiones.

Metodología y resultados

Separar evaluación externa, evaluación local y piloto de usuarios. Congelar modelo y umbral antes de consultar test. Presentar conteos, errores, abstenciones y métricas por conjunto; no mezclar accuracy de laboratorio con desempeño local.

Resultados pendientes: no hay cifras experimentales de campo, satisfacción, mejora productiva ni aprobaciones en este documento. Completar resultados, discusión y conclusiones únicamente con evidencia identificada por versión.

Avance técnico preliminar

Se generó un baseline MobileNetV2 FLOAT32 y se incorporó su asset TFLite con manifiesto y fixtures de paridad al proyecto. El entrenamiento utilizó datos externos depurados y una cabeza clasificadora sobre extracción de características. Esta evidencia acredita generación del modelo, no prueba Android en un teléfono ni validez para LIMONORT.

El umbral de abstención inicial es provisional y requiere validación independiente. Una evaluación argmax exploratoria anterior no debe presentarse como desempeño final del protocolo con abstención. Los recibos experimentales se conservan separados de los resultados finales; no se publica una cifra de exactitud local inexistente.

Antecedentes y sustentación

Revisar al menos cinco antecedentes con fuente primaria, tarea, cultivo, conjunto, partición y limitación. Los antecedentes anteriores de otro cultivo pueden permanecer como comparación contextual expresamente identificada; no constituyen validación del modelo actual.

Fundamentar MobileNetV2 y TFLite como opciones de implementación; comparar inferencia local con servidor en términos de dependencia de conexión, privacidad y mantenimiento. La elección de tecnologías actuales no prueba calidad por sí misma: demostrar migraciones, integridad, pruebas y separación de responsabilidades.



Dimensión | Indicador | Instrumento

Exactitud | Accuracy ≥85% y precision y recall ≥80% como metas propuestas | Matriz de confusión por clase, versión y conjunto; resultados pendientes

Oportunidad | Tiempo desde captura confirmada hasta resultado visible | 30 ejecuciones; mediana, p95, máximo, dispositivo y condición de inicio

Usabilidad | Completar login, parcela, captura, consulta y exportación | Piloto exploratorio de 5 usuarios elegibles; tareas, errores y valoración

Fiabilidad | Recuperar inspección y foto tras cierre de proceso | Pruebas de persistencia, migración e identidad A/B