# Baseline externo de minador: AgroVision Sullana

## Filtro de fotografías en Android

La captura y la importación pasan primero por `lemon_leaf_gate.tflite`. Este modelo
local estima por separado si hay una hoja y si parece de limonero. Cuando falta
evidencia de hoja, la app detiene el diagnóstico. Una hoja reconocible de especie
incierta queda pendiente de confirmar el árbol de origen. Si el usuario confirma
que la hoja proviene de un limonero, la app vuelve a comprobar la presencia de hoja
y evalúa los síntomas. El registro conserva la indicación `+origen-confirmado`.
Las fotos aceptadas automáticamente continúan al clasificador de síntomas. Las inspecciones guardan ambas
versiones. El filtro usa la misma entrada RGB normalizada de 224 × 224.

El filtro v2 usa un cabezal logístico sobre MobileNetV2 de ancho 1,0 con pesos
de ImageNet. Durante el entrenamiento se incluyen vistas completas y recortes
rotados de ambas clases, con mayor peso para las hojas etiquetadas con minador.
Las muestras incluyen originales de hojas sanas y dañadas, hojas de
naranjo, hojas de otras plantas y objetos. Las fuentes y rutas originales están
identificadas en `evidence/leaf_gate_images.csv`, junto con SHA256 y partición.

- Das y colaboradores, [BDLemonLeaf v6](https://data.mendeley.com/datasets/643f5bbc2t/6), carpeta Raw Image.
- [Lemon Leaf Dataset](https://data.mendeley.com/datasets/smz8ffbthy), incluidas hojas con minador, y [Comprehensive Lemon Leaf Disease Dataset](https://data.mendeley.com/datasets/44nrn4593f/1), carpeta Original Dataset.
- [Smartphone Image Dataset for Aegle Marmelos, Hog Plum and Lemon Plant Leaf v1](https://data.mendeley.com/datasets/54r883j5zr/1), archivos Original Dataset de las tres especies.
- [PlantVillage](https://github.com/spMohanty/PlantVillage-Dataset/tree/7f7ecc7e1eaca78107e3affe7cb5abd9427e139a/raw/color), incluidas hojas de naranjo etiquetadas con greening.
- [Imagenette](https://github.com/fastai/imagenette), fotos de diez clases de objetos y animales.
- [Caltech 101](https://data.caltech.edu/records/mzrjq-6wc02), fotos de objetos y animales.
- [COCO128](https://docs.ultralytics.com/datasets/detect/coco128/), escenas reservadas por completo para la evaluación.

Para repetir el entrenamiento, coloca las muestras bajo las rutas de la columna
`path` del inventario. Desde la raíz del proyecto, en el entorno Python del
entrenamiento, ejecuta lo siguiente. Usa carpetas de salida separadas para
comparar con el modelo distribuido.

```powershell
python -m pip install scikit-learn==1.9.1
python ml/train_leaf_gate.py --dataset ml/data/leaf_gate --assets ml/outputs/gate/assets --evidence ml/outputs/gate/evidence
```

El script admite nuevas fotos bajo `lemon`, `orange`, `other_leaf` y `object`, con
una subcarpeta por fuente. Elimina fotos de aspecto casi idéntico mediante pHash
y protege las particiones de prueba y validación del diagnóstico anterior. Elige
el umbral con la validación y mide la aceptación por grupo en prueba usando el
TFLite exportado. Los recuentos están en `evidence/leaf_gate_report.json`. Las
etiquetas proceden de los conjuntos públicos. La identificación de variedades
locales, otros cítricos y fotos de campo en Perú requiere evaluación independiente.
Las imágenes conservadas de v1 mantienen su partición original. Los cinco ejemplos
comunicados por el usuario se excluyen mediante pHash. En fotografías con transparencia,
Android y la referencia Python componen sobre blanco antes de reducir a RGB.
La separación se hizo por imágenes con control de similitud. Una misma planta
podría aparecer en más de una partición. El recuento de diagnósticos antes y
después del primer filtro está en `evidence/leaf_gate_before_after.json`.

## Clasificador de síntomas

Este pipeline prepara datos sin modificar las fotografías originales. El modelo
clasifica una hoja completa como `MINADOR_COMPATIBLE` o `SIN_SINTOMAS_VISIBLES`.
No identifica todos los agentes causales, no mide severidad y no acredita detección
temprana. Las otras 16 carpetas se registran como challenge, no se entrenan.

## Reproducir en PowerShell desde la raíz

Python 3.12 es compatible con TensorFlow 2.16.1. El entorno `ml/.venv` está aislado
y no se versiona. Dependencias directas fijadas en `requirements.txt`; la copia
completa del entorno se registra en `artifacts/environment_lock.txt`.

```powershell
$dataset = 'C:/Users/josem/Downloads/Large-Scale Lemon Leaf Disease and Pest Image Data/Large-Scale Lemon Leaf Disease and Pest Image Data'
& ml/.venv/Scripts/python.exe ml/prepare_dataset.py --dataset $dataset
& ml/.venv/Scripts/python.exe ml/audit_manifest.py
& ml/.venv/Scripts/python.exe -m unittest discover -s ml/tests -v
& ml/.venv/Scripts/python.exe ml/train_baseline.py --dataset $dataset
```

La última orden requiere descargar pesos ImageNet si no existen en
`ml/artifacts/weights/models`. Entrena un cabezal sobre MobileNetV2 congelado,
usando originales y una aumentación reproducible de cada imagen de entrenamiento.
Máximo 2 hilos CPU, 8 épocas y early stopping. No ajusta la base convolucional.

## Separación y trazabilidad

`manifest.csv` contiene ruta relativa, SHA256, pHash, dimensiones, grupo y split.
Se verifican decodificación, duplicados exactos y distancia pHash <=4 usando BK-tree.
La unión transitiva precede al split; grupos con carpetas contradictorias quedan
excluidos. Los grupos por clase se reparten aproximadamente 70/15/15 con semilla
20261012. Ningún grupo válido cruza entrenamiento/validación/test. Revisar manualmente
los grupos grandes y los conflictos: pHash no reemplaza identidad de planta, sesión
o predio. La ausencia de esos IDs limita la independencia real del test.

`dataset_report.json` compara cantidad local y publicación. La fuente candidata es
https://data.mendeley.com/datasets/8d9fv6kpt3/2; la procedencia exacta de esta copia
permanece sin confirmar. No atribuirla a LIMONORT, limón sutil o estados iniciales.

## Contrato Android

`app/src/main/assets/models/lemon_leaf_mobilenetv2_float32.tflite` y
`model_manifest.json` se generan solo desde entrenamiento real. Entrada FLOAT32
[1,224,224,3], RGB, resize bilinear a 224x224, valores `RGB/127.5-1`; salida
FLOAT32 [1,2] softmax, orden definido en `labels`. No cuantización. El manifiesto
incluye hash del modelo, hash del dataset, versión y umbral elegido en validación.

El umbral, con mínimo 0.70, maximiza macro-F1 contabilizando abstenciones en el denominador y exige
cobertura de al menos 70% en validación. Esto NO es calibración probabilística ni
garantiza rechazo de imágenes fuera del dominio. El test se consulta una vez tras
congelar el contrato; `test_evaluation_receipt.json` impide repetición accidental
en el mismo directorio. No borrar ese recibo para volver a ajustar sobre el test.

**Corrección de protocolo 2:** el entrenamiento inicial seleccionó 0.50, que en
softmax binario no permite abstenerse. Se corrigió esa política usando exclusivamente
las predicciones ya guardadas de validación: umbral 0.70. No se reconsultó el test.
El informe y recibo originales se preservan con sufijo `initial_exploratory`; esas
métricas no acreditan el protocolo final. El recibo actual impide reutilizar ese
test para nuevos ajustes. Falta evaluación local independiente no vista.

`training_report.json` contiene métricas reales, historia y paridad Keras/TFLite.
La matriz incorpora `NO_CONCLUYENTE`; todas las fotografías participan en el
denominador. Resultados del test externo no sustituyen evaluación local independiente.

`predict.py --image RUTA` ejecuta inferencia de referencia. `--write-fixtures`
genera cuatro entradas uniformes reproducibles para comparar ejecución Android
con tolerancia 0.002 en probabilidades. Esas entradas son pruebas numéricas y
no evidencias agronómicas ni garantía de rechazo fuera del dominio.

`generate_natural_fixtures.py --dataset RUTA` añade dos fotografías de validación
por clase, sus tensores de entrada FLOAT32 little-endian NHWC y salidas esperadas.
Los JPEG y tensores están en `app/src/androidTest/assets/models/parity_images`, con atribución; no se incluyen en el APK del usuario. El JSON de fixtures está en la misma carpeta `models` de pruebas.
El JSON
separa fixtures uniformes de `natural_fixtures`. Comparar primero runtime con el
mismo tensor y luego decodificación/resize de JPEG. Python usa Pillow BILINEAR
con filtro de reducción; Android ahora usa un filtro separable equivalente, con
coeficientes normalizados a 22 bits y redondeo por eje. `verify_resize.py` compara
esa matemática sobre RGB con Pillow en ocho casos, sin diferencia de píxeles.
Decodificación JPEG y orientación Android todavía requieren prueba de dispositivo.
La tolerancia propuesta de entrada
2/127.5 para entrada; runtime con tensor idéntico 0.002 y comparación de fotografía en la prueba Android 0.02 para salida requieren verificación en dispositivo; no son resultados
de paridad ya logrado en Android.

## Pendientes humanos

Confirmar licencia/procedencia de la copia; verificar taxonomía y cultivo; revisar
daño inicial con experto; recolectar muestra local independiente y consentimientos;
comparar salida Python/Android en el dispositivo y medir latencia de extremo a
extremo. No se han generado métricas de campo, aprobaciones ni etiquetas expertas.
