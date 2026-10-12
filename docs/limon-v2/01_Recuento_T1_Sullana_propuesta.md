Recuento T1 revisado

AgroVision Sullana

Versión 2 propuesta al asesor. Fecha de elaboración 12 de octubre de 2026. La aprobación académica, las etiquetas expertas y la validación local están pendientes.

Título de investigación

Aplicación móvil para apoyar la identificación de daño foliar compatible con minador en limón sutil en la Cooperativa Agraria LIMONORT-PERU Sullana 2026

Problema y propósito

Pregunta propuesta: ¿qué desempeño presenta una aplicación móvil local para apoyar la identificación de daño foliar compatible con minador en limón sutil de LIMONORT-PERU, Sullana, durante 2026?

Objetivo general: desarrollar y evaluar el desempeño técnico y operativo de la aplicación en clasificación foliar, oportunidad de respuesta y uso exploratorio. No atribuir mejora causal de productividad o reducción de pérdidas sin diseño y medición específicos.

Objetivos específicos: definir referencia agronómica y protocolo; construir pipeline reproducible; implementar inferencia y CRUD durable; medir exactitud, tiempos y operación con usuarios.

Variables y diseño

X: aplicación móvil local, operacionalizada por funciones verificables de login, CRUD, captura, inferencia y exportación. Y: desempeño del apoyo a identificación de daño compatible con minador, medido por clasificación, tiempo y operación.

Diseño propuesto: desarrollo tecnológico con evaluación de desempeño, sin inferir causalidad. Las definiciones conceptuales, fuentes académicas y ajuste del título requieren revisión del asesor. La Mobile App Rating Scale del T1 original no reemplaza la matriz de confusión ni acredita validez agronómica.

Datos y referencia agronómica

La copia externa inventariada contiene 17.586 JPG en 18 carpetas. Se seleccionan Citrus Leafminer (900) y Healthy (1.638) para el baseline. La fuente candidata es Mendeley Data 8d9fv6kpt3 versión 2, de Bangladesh; su atribución, versión y correspondencia con la copia deben confirmarse. No se atribuyen estos datos a LIMONORT ni se presupone limón sutil.

Las 16 clases restantes solo servirán para explorar confusiones por otras afecciones. Depurar archivos, agrupar duplicados exactos y similares antes de dividir aproximadamente 70/15/15. Mantener fotografías correlacionadas en un mismo grupo, fijar semilla y aplicar aumentación solo al entrenamiento.

Meta operativa local: 60 fotografías independientes con referencia experta y 5 usuarios para piloto exploratorio. Estas cantidades son metas de factibilidad, no suficiencia estadística ni participantes confirmados. Registrar planta y parcela cuando sea posible. No reutilizar campo para seleccionar umbral ni entrenar.

El técnico confirma minador y etapa inicial. La IA puede proponer etiquetas, nunca reemplazar la referencia independiente. Si no hay casos iniciales verificados, el estudio no concluye identificación temprana. Los resultados y permisos de campo permanecen pendientes.

Depuración ejecutada del conjunto externo

El manifiesto generado el 12 de octubre conserva 1.837 imágenes de las dos clases seleccionadas: 1.286 entrenamiento, 276 validación y 275 test. Se excluyen 701 imágenes seleccionadas por grupos de similitud que cruzan carpetas. La agrupación pHash es conservadora y requiere revisión visual de conflictos; no acredita independencia por planta o sesión.

El conjunto test externo incluye 99 imágenes MINADOR_COMPATIBLE y 176 SIN_SINTOMAS_VISIBLES. Fue consultado en una evaluación inicial exploratoria; no puede presentarse como test nunca visto del protocolo actual ni usarse para ajustar umbrales. La evaluación local independiente sigue pendiente. Fuente de recuentos: ml/artifacts/dataset_report.json; semilla 20261012. La proximidad visual puede agrupar imágenes distintas; conservar manifiesto y criterio para reproducir exclusiones.



Dimensión | Indicador | Instrumento

Exactitud | Accuracy ≥85% y precision y recall ≥80% como metas propuestas | Matriz de confusión por clase, versión y conjunto; resultados pendientes

Oportunidad | Tiempo desde captura confirmada hasta resultado visible | 30 ejecuciones; mediana, p95, máximo, dispositivo y condición de inicio

Usabilidad | Completar login, parcela, captura, consulta y exportación | Piloto exploratorio de 5 usuarios elegibles; tareas, errores y valoración

Fiabilidad | Recuperar inspección y foto tras cierre de proceso | Pruebas de persistencia, migración e identidad A/B