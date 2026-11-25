# Evidencia de validación — Rama `review/pantallas-grandes` (2026-11-25)

## 1. Validación UI en emulador `Small_Tablet` (`1920×1200` @ `320 dpi` = `960×600 dp`)
- **Modo avión activado**, permiso `CAMERA` concedido, APK `app/build/outputs/apk/debug/app-debug.apk`.
- **Pantallas verificadas en ejecución:**
  - `LoginScreen` (horizontal): tarjeta centrada a `460 dp`, degradado proporcional al contenedor, scroll con IME y creación de sesión local SQLite sin red.
  - `DashboardScreen` (horizontal): contenido centrado a `840 dp`, métricas y tarjetas con proporciones estables.
  - `GuidedCameraScreen` (horizontal y vertical): guía foliar responsiva sin solapamiento con `Cerrar`, `Flash`, instrucciones, selector de parcela (`P-01`), `Galería` y `Disparo` (columna derecha en horizontal; barra inferior acotada en vertical).
  - `DiagnosticResultScreen` (horizontal 2 columnas y vertical 1 columna): miniatura ampliada (`148 dp`) en tablet horizontal, recomendaciones y acciones accesibles; rotación horizontal → vertical con diálogo **Editar nota** abierto preservó diálogo y borrador (`SubloteNorte3`).
  - `HistorySyncScreen` (`840 dp`) y `SettingsScreen` (`760 dp`): centrados, filtros y badge `1` operativos.
  - **Persistencia offline:** tras `am force-stop` y reapertura en frío en modo avión, la sesión y la inspección `P-01` permanecieron disponibles desde Room.
  - **Estabilidad (`logcat`):** `0` crashes (`FATAL EXCEPTION: 0`).

## 2. Entrenamiento y evaluación comparativa TFLite (`ml/train_baseline.py`)
- **Particiones (`ml/evidence/manifest.csv`):** `train` = 1 286 (463 minador, 823 sin síntomas), `validation` = 276 (99 minador, 177 sin síntomas), 0 fugas de grupo entre `train`, `validation`, `test`, `challenge` y `excluded_conflict`; `test` no evaluado (`test_evaluated: false`).
- **Entrenamiento:** 8/8 épocas (`val_accuracy = 0.9964`, `val_loss = 0.0286`), reutilizando pesos locales en `ml/artifacts/weights`.
- **Comparación sobre las 276 fotos de `validation` (umbral `0.70`):**
  - Modelo actual APK y candidato recién entrenado: **99/99** aciertos de minador (Recall `100%`), **0/177** falsos positivos en sin síntomas (Precisión `100%`), **7** abstenciones (`NO_CONCLUYENTE`), cobertura **97.46%** (`269/276`).
  - Matriz de confusión (`[MINADOR_COMPATIBLE, SIN_SINTOMAS_VISIBLES, NO_CONCLUYENTE]`): `[[99, 0, 0], [0, 170, 7]]`.
  - SHA-256 idéntico bit a bit: `324d1fefc9675564606024691fee1445624ae186d303c3a9518874b532fbe507`.
- **Paridad Keras/TFLite y contrato Android:**
  - Delta máximo directo Keras vs TFLite (276 fotos): `5.25e-6` (`< 0.002`).
  - Contrato validado: `RGB`, `FLOAT32`, `[1,224,224,3]`, normalización `[-1,1]`, salida `FLOAT32 [1,2]`, etiquetas compatibles.
- **Decisión para la feria:** conservar el modelo actual del APK (empate exacto / reproducibilidad determinista). La precisión en campo aún requiere validación independiente con fotos reales de limón sutil (`local_validation_complete: false`).
