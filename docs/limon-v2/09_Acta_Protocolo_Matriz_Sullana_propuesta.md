Acta de cambio y protocolo de evaluación

AgroVision Sullana

Versión 2 propuesta al asesor. Fecha de elaboración 12 de octubre de 2026. La aprobación académica, las etiquetas expertas y la validación local están pendientes.

Solicitud de cambio

Aplicación móvil para apoyar la identificación de daño foliar compatible con minador en limón sutil en la Cooperativa Agraria LIMONORT-PERU Sullana 2026

AgroVision Sullana conserva inicio de sesión local y CRUD relacional de usuarios y parcelas en SQLite mediante Room. Captura una hoja, clasifica daño compatible con minador, registra inspecciones y exporta CSV sin internet. No diagnostica déficit hídrico ni calcula severidad desde la puntuación del modelo.

Motivo: transición del cultivo y lugar; disponibilidad de 16 h semanales; exigencia de login y CRUD SQLite local. Estado: propuesta técnica aceptada para implementación por el estudiante; aprobación académica formal pendiente. No contiene firmas ni resultados de campo.



Datos y referencia agronómica

La copia externa inventariada contiene 17.586 JPG en 18 carpetas. Se seleccionan Citrus Leafminer (900) y Healthy (1.638) para el baseline. La fuente candidata es Mendeley Data 8d9fv6kpt3 versión 2, de Bangladesh; su atribución, versión y correspondencia con la copia deben confirmarse. No se atribuyen estos datos a LIMONORT ni se presupone limón sutil.

Las 16 clases restantes solo servirán para explorar confusiones por otras afecciones. Depurar archivos, agrupar duplicados exactos y similares antes de dividir aproximadamente 70/15/15. Mantener fotografías correlacionadas en un mismo grupo, fijar semilla y aplicar aumentación solo al entrenamiento.

Meta operativa local: 60 fotografías independientes con referencia experta y 5 usuarios para piloto exploratorio. Estas cantidades son metas de factibilidad, no suficiencia estadística ni participantes confirmados. Registrar planta y parcela cuando sea posible. No reutilizar campo para seleccionar umbral ni entrenar.

El técnico confirma minador y etapa inicial. La IA puede proponer etiquetas, nunca reemplazar la referencia independiente. Si no hay casos iniciales verificados, el estudio no concluye identificación temprana. Los resultados y permisos de campo permanecen pendientes.

Depuración ejecutada del conjunto externo

El manifiesto generado el 12 de octubre conserva 1.837 imágenes de las dos clases seleccionadas: 1.286 entrenamiento, 276 validación y 275 test. Se excluyen 701 imágenes seleccionadas por grupos de similitud que cruzan carpetas. La agrupación pHash es conservadora y requiere revisión visual de conflictos; no acredita independencia por planta o sesión.

El conjunto test externo incluye 99 imágenes MINADOR_COMPATIBLE y 176 SIN_SINTOMAS_VISIBLES. Fue consultado en una evaluación inicial exploratoria; no puede presentarse como test nunca visto del protocolo actual ni usarse para ajustar umbrales. La evaluación local independiente sigue pendiente. Fuente de recuentos: ml/artifacts/dataset_report.json; semilla 20261012. La proximidad visual puede agrupar imágenes distintas; conservar manifiesto y criterio para reproducir exclusiones.

Matriz de consistencia

Problema: desempeño del apoyo móvil. Objetivo general: desarrollar y evaluar. X: aplicación implementada y verificada. Y: desempeño medido. Técnica: experimentación técnica y observación de tareas. Instrumentos: manifiesto, referencia experta, matriz de confusión, cronometraje y ficha de usuario. Unidades: fotografía, ejecución y usuario, analizadas por separado.

Ficha de captura y etiqueta

Campos por completar: código de imagen; parcela; planta; fecha; cultivo confirmado; órgano; iluminación observada; dispositivo; permiso; técnico; clase de referencia; etapa inicial sí/no/no determinable; criterio y observaciones. Etiquetar antes de visualizar salida del modelo.

No concluyente no equivale a sano. No usar imágenes ambiguas como clase segura sin criterio documentado. Mantener archivo original, hash y partición.

Ficha de tiempos y usuario

Por ejecución: ID, modelo/hash, teléfono, condición inicial, inicio, resultado visible, duración, estado y error. Registrar 30 ejecuciones sin descartar lentas. Por usuario: código, tareas login/parcela/captura/historial/CSV, éxito, ayuda, errores, valoración y observaciones. La meta es exploratoria.

Ishikawa propuesto

Problema central a confirmar: dificultad para registrar y revisar oportunamente daño foliar compatible con minador. Categorías de hipótesis: método de inspección, conocimiento de síntomas, disponibilidad de registro, calidad fotográfica y condiciones de campo. Cada causa debe tener observación o entrevista identificada. No incluir riego ni afirmar pérdidas no medidas.

Objeto | Decisión

Catálogo | Dos clases de daño compatible; sin diagnóstico hídrico

Persistencia | Usuarios, parcelas, inspecciones y referencia versionada

Login | Local con credenciales reales, sin demo universal

Exportación | CSV; imagen privada

Método | Evaluación de desempeño; no causalidad

Datos | Externos separados de campo y participantes