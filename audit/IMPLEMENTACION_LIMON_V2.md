# Implementación del MVP local en limon v2

Fecha: 12 de octubre de 2026. Se mantuvieron login y CRUD relacionales porque el profesor los requiere y el usuario confirmó que SQLite local es aceptado. No se implementó un servidor Spring ni se trasladó el diagnóstico a nube.

## Producto construido

- Entrada Android nueva: Compose → LocalMvpViewModel → LocalMvpRepository → DAO Room/SQLite. El modelo TFLite es un adaptador local, no un servicio web.
- Primer inicio configura administrador real; login local verifica contraseñas derivadas con PBKDF2 y sal individual. Usuarios administrativos y operarios se conservan en Room. No hay credenciales demo activas.
- Administrador crea, consulta, edita y elimina usuarios/parcelas no referenciados. No puede eliminar o desactivar su propia cuenta ni dejar el sistema sin administrador. Datos referenciados se conservan mediante restricción y posibilidad de desactivación.
- Usuarios operarios no reciben el listado de cuentas ni hashes/salts. Pueden registrar y consultar sus propias inspecciones; el administrador puede acceder al conjunto.
- Captura mediante cámara del dispositivo o importación de galería. Se registra borrador antes de abrir el proveedor externo. Error de imagen/modelo produce ERROR explícito; no diagnóstico ficticio.
- Historial y detalle se recuperan por ID. Editar una nota no modifica el resultado, puntuación o versión del modelo. Resultados completados quedan congelados; una nueva evaluación corresponde a una inspección nueva.
- CSV compartible con campos entrecomillados y neutralización de prefijos de fórmula. Guarda clase y metadatos de la inspección seleccionada, incluyendo recomendaciones originales.
- Room v2 con usuarios, parcelas e inspecciones; PK, FK, unicidad e índices. Migración conserva diagnósticos anteriores como LEGADO, sin convertir trips/agua en minador.
- Sin sincronización falsa, recomendaciones de cebolla/riego ni severidad deducida de confianza. El antiguo flag sync se conserva exclusivamente como dato legado.

Las pantallas anteriores se conservaron para respetar los cambios existentes del usuario, pero la entrada activa utiliza el MVP nuevo. No describir funcionalidades antiguas desconectadas como capacidades del producto actual.

## Modelo generado y límites

Modelo real MobileNetV2 FLOAT32, 8.873.848 bytes. Entrada [1,224,224,3], RGB, /127.5−1. Salida [1,2]: MINADOR_COMPATIBLE y SIN_SINTOMAS_VISIBLES. Manifiesto con versión, hash y umbral 0.70 elegido mediante validación.

Se inventariaron 17.586 imágenes externas. De 2.538 candidatas de las dos carpetas se excluyeron conservadoramente 701 por similitud cruzada entre carpetas; quedan 1.837 para train/validation/test. Esto exige revisión humana y no acredita duplicados biológicos confirmados. La fuente candidata corresponde a Bangladesh, no a LIMONORT. La variedad limón sutil y el daño inicial no están verificados.

Los resultados y cambios de protocolo se explican en [Resultados preliminares](E:/TESIS/AGROVISION-LURIN-1/ml/evidence/RESULTADOS_PRELIMINARES.md). El test externo ya consultado se conserva como exploratorio. No representa evaluación independiente final del protocolo vigente.

El umbral no es un detector universal de hojas ni una garantía de rechazo fuera del dominio. La app rechaza imágenes casi uniformes como calidad insuficiente, pero otras fotos ajenas pueden producir puntuaciones altas. Solicitar siempre una hoja del cultivo y referencia técnica. No confundir puntuación softmax con probabilidad calibrada.

## Evidencias y archivos

| Elemento | Archivo |
|---|---|
| Fundamento y DER para el profesor | [Arquitectura SQLite y CRUD](E:/TESIS/AGROVISION-LURIN-1/audit/FUNDAMENTO_ARQUITECTURA_SQLITE_CRUD.md) |
| DDL generado de las tablas reales | [Esquema SQL](E:/TESIS/AGROVISION-LURIN-1/database/agrovision_sullana_schema.sql) |
| Esquema Room v2 exportado | [Esquema compilado](E:/TESIS/AGROVISION-LURIN-1/app/schemas/pe.edu.upn.agrovisionlurin.data.local.AppDatabase/2.json) |
| APK de demostración local | [APK debug](E:/TESIS/AGROVISION-LURIN-1/app/build/outputs/apk/debug/app-debug.apk) |
| Propuestas académicas y cronograma | [Índice de documentos](E:/TESIS/AGROVISION-LURIN-1/docs/limon-v2/README.md) |
| Reproducción de datos y entrenamiento | [Guía ML](E:/TESIS/AGROVISION-LURIN-1/ml/README.md) |

## Verificación obtenida

- Compilación final después del ajuste de autorización: assembleDebug, testDebugUnitTest y assembleDebugAndroidTest terminaron correctamente. Dos pruebas JVM de contraseñas/CSV pasaron; el APK de pruebas instrumentadas se generó, sin ejecutarlo en dispositivo.
- Seis pruebas Python del pipeline pasaron tras los cambios finales: partición, similitud, corrupción, normalización y denominadores con abstención.
- El chequeo SQLite conservó cinco inspecciones de la base de ejemplo y confirmó dos FKs, unicidad y rechazo de borrado referenciado. Columnas/índices/FKs coinciden con el esquema Room compilado.
- Pruebas instrumentadas preparadas para migración, autenticación, permisos, CRUD, identidad A/B, congelación, imagen inválida, modelo ausente y paridad numérica. No ejecutadas: no había dispositivo Android conectado.
- Cronograma Excel propuesto recalculado y revisado por rangos, conservando la línea base histórica. Ocho propuestas Word y un documento de acta/protocolo se entregan como borradores, no como tesis final.

## Primera demostración manual

1. Instalar el APK en un teléfono de prueba. Si ya existe una instalación con mismo applicationId, conservarla y probar actualización; no desinstalar para ocultar fallos de migración. Si la firma es distinta, preservar/exportar datos antes de elegir una estrategia de instalación.
2. Configurar administrador si no hay usuarios de la versión nueva. Crear una parcela real con código único y una cuenta operaria.
3. Probar contraseña incorrecta, cambiar una cuenta, desactivarla y demostrar que no puede iniciar sesión. Verificar rol administrativo.
4. Iniciar como operario, elegir parcela y capturar/importar una hoja. Consultar su registro, nota y CSV. Activar modo avión y repetir.
5. Crear A y B; abrir A después de B y comparar foto, resultado y CSV. Cerrar el proceso y recuperar A desde historial.
6. Como administrador, intentar duplicar código de parcela y borrar parcela/usuario referenciado. Mostrar restricciones y desactivación.
7. Ejecutar pruebas instrumentadas y medir 30 capturas hasta resultado visible, identificando teléfono, arranque frío/caliente y versión del modelo. Estos tiempos aún no existen.

## Pendientes que requieren evidencia externa

Confirmación del asesor sobre título/diseño y nueva línea base; procedencia/licencia de la copia de datos; revisión agronómica de minador y etapas iniciales; muestras locales independientes; piloto y consentimientos; pruebas Android reales y latencia/memoria en teléfono.

Los DOCX no tienen revisión visual completa: falta LibreOffice soffice.exe en el runtime empaquetado usado por el renderizador. El control de documentos exige renderizar e inspeccionar páginas antes de considerar aprobado el formato. Su estructura fue comprobada, pero deben revisarse en Word antes de una entrega formal. El informe principal todavía requiere antecedentes verificados, resultados, discusión y conclusiones.

No se crearon commits ni se publicaron archivos. Los originales académicos de OneDrive permanecieron intactos. Todas las modificaciones de implementación se realizaron en la rama solicitada limon-v2, manteniendo el applicationId anterior.
