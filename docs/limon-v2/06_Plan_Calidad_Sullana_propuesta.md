Plan de gestión de la calidad

AgroVision Sullana

Versión 2 propuesta al asesor. Fecha de elaboración 12 de octubre de 2026. La aprobación académica, las etiquetas expertas y la validación local están pendientes.

Objetivo y medición

Verificar funcionamiento Android y evaluar desempeño ML por separado. No sustituir precisión por usabilidad ni convertir objetivos en resultados.



Pruebas del producto

Probar login válido e inválido; unicidad de usuarios y parcelas; edición y bajas con registros relacionados; migración; reinicio; lectura A/B; imagen corrupta y modelo ausente; contrato de tensores; exportación CSV y modo avión.

La edición de metadatos conserva clase, score, versión, tiempos y recomendaciones originales. El test de migración parte de una base previa realista y compara recuentos y significado de etiquetas. No usar una reconstrucción destructiva como estrategia de actualización.

Evaluación del modelo

Reportar accuracy, precision, recall y F1 por clase, matriz de confusión, cobertura de resultados concluyentes y tasa de abstención. Informar evaluación completa con abstenciones y métricas sobre resultados concluyentes por separado. Seleccionar umbral en validación; test y campo quedan reservados.

Comparar Python y TFLite Android sobre fotografías fijas, con tolerancia documentada; verificar resize, orientación, normalización, dtype y orden de clases. Congelar hash y configuración antes del piloto.

Estado del protocolo

El test externo ya se consultó en una corrida exploratoria. El protocolo actual conserva ese recibo y no presenta el conjunto como nunca visto. El umbral 0,7 fue seleccionado con validación y permanece pendiente de aplicabilidad local. No ajustar sobre test externo ni sobre campo; recoger una referencia local independiente para la evaluación final.

Validación humana y cierre

El experto evalúa síntomas y etapa con ficha independiente antes de comparar con la app. Registrar desacuerdos y casos ambiguos. Los cinco usuarios son piloto exploratorio, no muestra representativa por defecto.

Salida: cero errores críticos que fabriquen resultados o pierdan registros; pruebas vinculadas a requisitos; resultados científicos reportados aunque no alcancen metas. La aceptación académica no se presume.

Dimensión | Meta o indicador | Evidencia

Exactitud | Accuracy ≥85% y precision y recall ≥80% como metas propuestas | Matriz de confusión por clase, versión y conjunto; resultados pendientes

Oportunidad | Tiempo desde captura confirmada hasta resultado visible | 30 ejecuciones; mediana, p95, máximo, dispositivo y condición de inicio

Usabilidad | Completar login, parcela, captura, consulta y exportación | Piloto exploratorio de 5 usuarios elegibles; tareas, errores y valoración

Fiabilidad | Recuperar inspección y foto tras cierre de proceso | Pruebas de persistencia, migración e identidad A/B