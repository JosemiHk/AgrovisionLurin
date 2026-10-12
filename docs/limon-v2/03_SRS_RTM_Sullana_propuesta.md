Especificación de requisitos y trazabilidad

AgroVision Sullana

Versión 2 propuesta al asesor. Fecha de elaboración 12 de octubre de 2026. La aprobación académica, las etiquetas expertas y la validación local están pendientes.

Propósito y línea base

AgroVision Sullana conserva inicio de sesión local y CRUD relacional de usuarios y parcelas en SQLite mediante Room. Captura una hoja, clasifica daño compatible con minador, registra inspecciones y exporta CSV sin internet. No diagnostica déficit hídrico ni calcula severidad desde la puntuación del modelo.

Se conservan IDs RF01 a RF10 del documento original; RF11 incorpora CRUD de usuarios. Todos son requisitos de la propuesta, no declaración de implementación terminada. El retiro de RF05 preserva su trazabilidad histórica.

RF01 Modificado

Inicio y cierre de sesión local con credenciales verificadas; sin acceso universal ni contraseñas en claro.

Aceptación: CP-AUTH-01 credenciales válidas e inválidas; sesión cerrada impide entrar.

RF02 Modificado

Crear, consultar, editar y retirar parcelas por código único y nombre; sin GPS ni inferencia de edad fenológica.

Aceptación: CP-PARC-01 CRUD; duplicados rechazados; integridad al retirar parcela usada.

RF03 Modificado

Capturar o importar una hoja, conservar copia privada y preparar entrada RGB 224 por 224. Error explícito para archivo no legible.

Aceptación: CP-IMG-01 cámara, galería, archivo corrupto y orientación.

RF04 Modificado

Clasificar hoja completa en MINADOR_COMPATIBLE o SIN_SINTOMAS_VISIBLES con modelo TFLite real; admitir no concluyente.

Aceptación: CP-ML-01 contrato y paridad; matriz de confusión independiente.

RF05 Retirado

Déficit hídrico excluido por falta de medición validada con imagen RGB. ID conservado como trazabilidad histórica.

Aceptación: Sin implementación activa.

RF06 Modificado

Presentar acciones de observar, registrar y consultar al técnico; guardar texto original por inspección.

Aceptación: CP-REC-01 catálogo correcto; sin prescripción de pesticidas ni riego.

RF07 Modificado

Ejecutar inferencia local y medir tiempo real; meta inicial menor a 3,5 s en dispositivo identificado.

Aceptación: CP-PERF-01 30 ejecuciones; mediana, p95 y máximo.

RF08 Modificado

Historial por usuario y parcela; recuperar por ID; editar metadatos y eliminar con confirmación sin alterar predicción congelada.

Aceptación: CP-DATA-01 abrir A después de B; edición no cambia clase, score o modelo.

RF09 Modificado

Persistir borrador, imagen y resultado en Room; migrar datos anteriores como LEGADO. Sin sincronización remota.

Aceptación: CP-ROOM-01 reinicio, migración y modo avión.

RF10 Modificado

Exportar inspección a CSV compartible con identificadores, fechas, parcela, resultado, modelo y recomendaciones.

Aceptación: CP-CSV-01 escaping y correspondencia exacta con la inspección.

RF11 Nuevo

Crear, consultar, editar y retirar usuarios locales con identidad única; proteger usuario de sesión y referencias existentes.

Aceptación: CP-USR-01 CRUD; contraseña nueva verificable y registros relacionados preservados.

Restricciones de calidad

Se conservan los identificadores RNF originales y su dimensión. Las metas son propuestas pendientes de ratificación. Ninguna cifra de la siguiente tabla representa un resultado ya medido.



Restricciones adicionales

RNF-V2-01 integridad relacional: transacciones, FK y únicos. RNF-V2-02 reproducibilidad: hash, contrato y versión. RNF-V2-03 mantenibilidad: lógica fuera de composables y migración probada. Son nuevos identificadores y no sustituyen silenciosamente requisitos anteriores.



Cambios respecto al alcance anterior

Se retiran como histórico las promesas de nube, YOLO, cajas, déficit hídrico, GPS y PDF. Login, SQLite y CRUD se mantienen por requisito docente. Las etiquetas anteriores no se reinterpretan como limón; los registros migrados quedan LEGADO. Firma y aprobación del asesor: pendientes.

ID y estado | Dimensión | Revisión propuesta

RNF01 modificado | Exactitud | Accuracy ≥85%; precision y recall ≥80% por clase en test externo y campo separados. No extrapolar a toda enfermedad.

RNF02 modificado | Reglas | Cobertura del catálogo de dos clases y estados error/no concluyente; no combinar déficit hídrico.

RNF03 modificado | Tiempo | Meta <3,5 s desde captura confirmada hasta resultado visible; 30 ejecuciones en teléfono identificado, frío/caliente registrados.

RNF04 modificado | Recursos | Usar FLOAT32; medir tamaño real y memoria en dispositivo. Límites históricos 45 MB/250 MB por ratificar, no prometer INT8.

RNF05 modificado | Offline | Funciones locales sin red; recuperación de registros probada. Retirar garantía universal de pérdida cero.

RNF06 retirado | Recuperación remota | Sin servidor ni transferencia. La recuperación local se verifica en RNF05 y RF09.

RNF07 modificado | Usabilidad | Piloto exploratorio de 5 usuarios; registrar tareas y errores. SUS y meta histórica 85 solo si instrumento y criterio ratificados.

RNF08 modificado | Prevención de errores | Imagen/modelo inválido causa error explícito; no afirmar filtro Laplacian validado ni detección automática universal de mala calidad.

RNF09 modificado | Seguridad | Credenciales locales derivadas con sal individual y permisos de administración; TLS no aplica sin comunicación remota.

RNF10 modificado | Compatibilidad | Validar API mínima declarada en Gradle y teléfono piloto. Límites previos 2 GB/8 MP no implican soporte universal.

Requisito | HU y prueba | Evidencia actual

RF01 | HU01 CP-AUTH-01 | Pendiente de prueba y registro

RF02 | HU02 CP-PARC-01 | Pendiente de prueba y registro

RF03 | HU03 CP-IMG-01 | Pendiente de prueba y registro

RF04 | HU04 CP-ML-01 | Pendiente de prueba y registro

RF06 | HU06 CP-REC-01 | Pendiente de prueba y registro

RF07 | HU07 CP-PERF-01 | Pendiente de prueba y registro

RF08 | HU08 CP-DATA-01 | Pendiente de prueba y registro

RF09 | HU09 CP-ROOM-01 | Pendiente de prueba y registro

RF10 | HU10 CP-CSV-01 | Pendiente de prueba y registro

RF11 | HU11 CP-USR-01 | Pendiente de prueba y registro