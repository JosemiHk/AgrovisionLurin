# Paquete académico propuesto AgroVision Sullana

Versión 2 para revisión del asesor, elaborada el 12 de octubre de 2026. Conserva login y CRUD SQLite locales por requisito docente. No acredita aprobación institucional ni resultados de campo.

## Archivos para revisar

| Archivo | Contenido |
|---|---|
| 01_Recuento_T1_Sullana_propuesta.docx | Título, X/Y, objetivo, unidades y protocolo de evaluación |
| 02_Informe_Proyecto_Sullana_propuesta.docx | Secciones técnicas y metodológicas reformuladas; resultados pendientes |
| 03_SRS_RTM_Sullana_propuesta.docx | RF01–10 conservados, RF11 nuevo; RNF anteriores con estado y pruebas |
| 04_EDT_Sullana_propuesta.docx | Entregables, exclusiones y dedicación semanal |
| 05_Plan_Proyecto_Sullana_propuesta.docx | Responsables, sprints, riesgos y gestión de cambios |
| 06_Plan_Calidad_Sullana_propuesta.docx | Pruebas Android, referencia independiente y evaluación ML |
| 07_Plan_Costos_Sullana_propuesta.docx | Capacidad de trabajo y partidas pendientes de cotización |
| 08_Registro_Interesados_Sullana_propuesta.docx | Roles confirmables, permisos y participación |
| 09_Acta_Protocolo_Matriz_Sullana_propuesta.docx | Acta sin firma, ficha maestra, matriz y fichas de evaluación |
| Cronograma_Sullana_propuesta.xlsx | Línea base histórica íntegra y programación revisada con CPM y capacidad |

Los Word son propuestas de sustitución de las secciones afectadas, no memorias finales listas para presentar. El informe principal aún necesita antecedentes académicos verificados, resultados, discusión y conclusiones derivados del estudio. No se inventaron referencias académicas ni aprobaciones. Los archivos originales de Documentos permanecen intactos.

Las copias `.md` facilitan revisar contenido y diferencias; `REQUISITOS_MVP.json` permite enlazar pruebas a IDs estables. El fundamento técnico detallado está en `audit/FUNDAMENTO_ARQUITECTURA_SQLITE_CRUD.md`. La implementación debe verificarse antes de cambiar las descripciones de arquitectura propuesta a arquitectura implementada.

## Cronograma

El CPM lógico usa horas de esfuerzo y precedencias fin a inicio. Su duración lógica calculada es 66 h; la programación nivelada utiliza un único responsable y 84 h comprometidas. Las fechas se obtienen de la disponibilidad de lunes a sábado y terminan el 28 de noviembre. Las 28 h de domingo son reserva sin tareas comprometidas. La entrega del 30 de noviembre usa 1,5 a 2 h adicionales.

En `CPM Sullana`, editar esfuerzo en C y actualizar precedencias junto con fórmulas ES/LF si cambia el grafo. No basta editar el texto de predecesoras D: es la descripción del grafo, y E/H contienen las referencias de cálculo correspondientes. El orden de filas es el orden de trabajo de una persona. Registrar estado y evidencia en Q/R.

En `Calendario Sullana`, B/C son disponibilidad de compromiso/reserva. Los días operativos del lookup I/K corresponden a lunes a sábado: si se cambia ese conjunto o se agregan semanas, actualizar el lookup y rangos. No convertir reserva de domingo en compromiso sin replanificar. `Control Sullana` documenta la línea base; sus 12/4 horas son supuestos originales, no una distribución automática del calendario. Verificar ambas vistas después de cambiar disponibilidad.

Las hojas originales `Cronograma de Hitos` y `Cronograma Detallado` contienen terminología y estados históricos del alcance anterior; no son el cronograma activo. Sus valores y fórmulas se compararon con el original y permanecen iguales. El archivo fuente no se sobrescribió.

## Verificación y pendientes

- Excel: recálculo con artifact-tool, escaneo de errores, prueba de cambio de esfuerzo y restauración, comparación de histórico, cache exportada y revisión visual de rangos relevantes. No se ejecutó en Microsoft Excel nativo.
- Word: nueve archivos DOCX válidos estructuralmente y contenido revisado. **La revisión visual de páginas no está completada:** el renderizador empaquetado falla porque no existe LibreOffice `soffice.exe` en el runtime. No se presentan como maquetación aprobada. Revisar paginación y tablas en Word antes de entregar.
- Las metas accuracy ≥85%, precision y recall ≥80%, <3,5 s y cantidades de campo/usuarios son objetivos propuestos; no resultados ni suficiencia estadística.
- Confirmar título y diseño con asesor, experto y agenda, permisos, fuente/version del dataset y tarifas antes de aprobar la nueva línea base.
- Confirmar etapa inicial mediante criterio experto antes de reincorporar “temprana” al título o conclusiones.
- El conjunto externo depurado tiene 1.837 imágenes seleccionadas, 1.286/276/275 en train/validation/test, con agrupación conservadora por similitud. No representa validación local ni independencia comprobada por planta.
- El test externo fue consultado en una evaluación exploratoria anterior. No presentarlo como test nunca visto del protocolo actual ni reutilizarlo para ajustar umbral. El umbral 0,7 del manifiesto actual procede de validación y queda pendiente de aplicabilidad local. La referencia local independiente será la evidencia final pendiente.

## Orden de revisión

1. Ficha y acta, título y metodología con asesor.
2. Recuento T1 y matriz de consistencia.
3. SRS/RTM y fundamento de arquitectura SQLite/CRUD.
4. Protocolo experto y referencia local.
5. EDT, planes de proyecto/calidad/costos e interesados.
6. Cronograma CPM nivelado y fechas oficiales.
7. Completar evidencias y resultados reales; después cerrar memoria y manuales.
