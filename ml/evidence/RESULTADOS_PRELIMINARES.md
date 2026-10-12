# Evidencia del baseline externo — protocolo 2

## Datos y alcance real

Se encontraron 17.586 imágenes en 18 carpetas. Clases seleccionadas originalmente:
900 Citrus Leafminer y 1.638 Healthy (2.538). Se excluyeron conservadoramente 239
minador y 462 Healthy por grupos con etiquetas de carpeta contradictorias. Quedan
1.837: entrenamiento 1.286 (463/823), validación 276 (99/177), test externo 275
(99/176). Las demás imágenes se conservan en el manifiesto challenge/exclusiones.
No se modificó la fuente. No hubo errores de decodificación en este inventario.

El agrupamiento SHA256/pHash <=4 produjo 3.190 grupos con más de una imagen, máximo
832. Estos son **candidatos de similitud**, no duplicados confirmados ni plantas
identificadas. Una cadena transitiva puede agrupar extremos disímiles. Se entregan
100 pares directos con rutas y distancias para revisión humana; la exclusión de
701 imágenes seleccionadas puede sesgar el conjunto. No se ha hecho esa revisión
experta. IDs de planta/sesión ausentes limitan independencia más allá de hashes.

La ficha pública declara Bangladesh, 17.609 imágenes y CC BY 4.0. Fuente candidata:
[Arifen et al., versión 2, DOI 10.17632/8d9fv6kpt3.2](https://data.mendeley.com/datasets/8d9fv6kpt3/2).
Se verificó la ficha pública, no la procedencia exacta de esta copia. La diferencia
es de 23 imágenes; no atribuir estos datos a LIMONORT ni a limón sutil.

## Modelo real y contrato

MobileNetV2 con pesos ImageNet oficiales, base congelada y cabezal entrenado durante
8 épocas; originales y una aumentación por imagen de entrenamiento. Semilla
20261012, pesos de clase y máximo 2 hilos. Exportación FLOAT32, 8.873.848 bytes.

- Entrada [1,224,224,3], RGB /127.5−1, resize bilinear.
- Salida [1,2], orden MINADOR_COMPATIBLE, SIN_SINTOMAS_VISIBLES.
- SHA256: `324d1fefc9675564606024691fee1445624ae186d303c3a9518874b532fbe507`.
- Máxima diferencia Keras/TFLite en probabilidad: 0.00000691414.
- Versión vigente: `sullana-minador-baseline-1-protocol2`.
- Umbral vigente 0.70, elegido exclusivamente usando validación.

## Resultados de validación vigente

276 imágenes; 7 abstenciones; cobertura 97,46%; exactitud sobre el denominador
completo 97,46%. Matriz filas minador/sin síntomas, columnas minador/sin síntomas/
no concluyente: [[99,0,0],[0,170,7]]. Minador: precision/recall/F1 1,0; sin síntomas:
precision 1,0, recall 0,96045, F1 0,97983. Son resultados del conjunto usado para
seleccionar el umbral, **no una estimación final independiente**.

El protocolo inicial eligió 0.50, que no permite abstenerse en softmax binario.
Su test externo se consultó una vez antes de detectar esa deficiencia. Se conservan
el informe y recibo históricos como exploratorios; no se presentan como evaluación
final del protocolo corregido. La corrección utilizó solamente validación y no
volvió a consultar el test. La evaluación local independiente está pendiente.

## Verificación y pendientes

Seis pruebas automáticas pasaron: BK-tree vs búsqueda exhaustiva, partición
reproducible, corrupción/exclusión cruzada sin modificar fuentes, denominadores
con abstención, umbral binario capaz de abstenerse y RGB/normalización. Auditoría
de manifiesto confirmó que ningún grupo cruza los tres splits.

Fixtures RGB uniformes sirven para comprobar paridad numérica Android; no acreditan
diagnóstico y el score no es probabilidad calibrada. El rechazo fuera del dominio
no está garantizado. Faltan revisión de candidatos de similitud, procedencia,
validación agronómica local independiente, daño inicial, pruebas Android reales
y medición de extremo a extremo. No se inventaron esos resultados.

Se entregan además cuatro JPEG de validación (dos por clase), tensores esperados
FLOAT32 y probabilidades para medir paridad de fotografías. Estas pruebas no
consultan el test externo. Pillow y Android pueden reducir imágenes con filtros
distintos; EXIF, decodificación y resize aún requieren comparación en teléfono.
