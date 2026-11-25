package pe.edu.upn.agrovisionlurin.presentation.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upn.agrovisionlurin.presentation.local.LocalMvpViewModel
import pe.edu.upn.agrovisionlurin.presentation.theme.PrimaryGreen
import pe.edu.upn.agrovisionlurin.presentation.theme.Typography
import java.io.File

@Composable
fun GuidedCameraScreen(
    onBack: () -> Unit,
    onCaptureComplete: (String) -> Unit,
    session: LocalMvpViewModel = viewModel()
) {
    val isAnalyzing by session.busy.collectAsStateWithLifecycle()
    val plots by session.plots.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedPlot by session.selectedPlot.collectAsStateWithLifecycle()
    val ready by session.ready.collectAsStateWithLifecycle()
    val user by session.user.collectAsStateWithLifecycle()
    val message by session.message.collectAsStateWithLifecycle()
    var cameraError by remember { mutableStateOf("") }
    var selectingPlot by remember { mutableStateOf(false) }
    var captureInProgress by remember { mutableStateOf(false) }
    val captureEnabled = ready && user != null && plots.any { it.id == selectedPlot && it.active } && !isAnalyzing && !captureInProgress
    BackHandler {
        if(!isAnalyzing && !captureInProgress) { session.discardPendingCapture();onBack() }
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    // Limpieza de ciclo de vida para evitar fugas de hardware y memoria
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraControl?.enableTorch(false)
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                Log.e("GuidedCameraScreen", "Error al desvincular CameraProvider", e)
            }
        }
    }

    // Selector de galería File-First sin decodificar bitmap en memoria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) session.importGallery(uri, onCaptureComplete)
        else session.discardPendingCapture()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                            cameraControl = camera.cameraControl
                        } catch (exc: Exception) {
                            Log.e("CameraX", "Use case binding failed", exc)
                            cameraError = "No se pudo iniciar la cámara. Puedes importar una fotografía."
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("Se requiere permiso de cámara para la evaluación foliar.", color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) { Text("Permitir cámara") }
                }
            }
        }

        val activePlots = plots.filter { it.active }
        val selectedPlotLabel = plots.firstOrNull { it.id == selectedPlot }?.let { "Parcela ${it.code}" } ?: "Seleccionar parcela"
        val errorText = cameraError.ifBlank { message }

        val onCloseClick: () -> Unit = {
            if (!captureInProgress && !isAnalyzing) onBack()
        }
        val onFlashToggle: () -> Unit = {
            isFlashOn = !isFlashOn
            cameraControl?.enableTorch(isFlashOn)
        }
        val onGalleryClick: () -> Unit = {
            session.createCapture { galleryLauncher.launch("image/*") }
        }
        val onCaptureClick: () -> Unit = {
            val capture = imageCapture
            if (capture != null) {
                cameraError = ""
                session.createCapture { path ->
                    captureInProgress = true
                    val photoFile = File(path)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    capture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                captureInProgress = false
                                session.finishCapturedFile(true, onCaptureComplete)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraX", "Fallo al guardar imagen: ${exception.message}", exception)
                                captureInProgress = false
                                cameraError = "No se pudo guardar la fotografía. Intenta de nuevo."
                                session.discardPendingCapture()
                            }
                        }
                    )
                }
            }
        }

        // Overlay adaptativo según el área disponible de la ventana (incluidos insets del sistema)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp)
        ) {
            val isSideControls = maxWidth > maxHeight && maxWidth >= 520.dp
            val isCompactWidth = maxWidth < 340.dp
            val isCompactHeight = maxHeight < 520.dp

            if (isSideControls) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CameraCloseButton(onClick = onCloseClick)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isCompactHeight) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CameraInstructionPill(
                                            text = "Encuadre la hoja con luz uniforme",
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        CameraPlotSelector(
                                            label = selectedPlotLabel,
                                            expanded = selectingPlot,
                                            onExpand = { selectingPlot = true },
                                            onDismiss = { selectingPlot = false },
                                            activePlots = activePlots,
                                            onSelectPlot = { plotId ->
                                                session.selectedPlot.value = plotId
                                                selectingPlot = false
                                            }
                                        )
                                    }
                                } else {
                                    CameraInstructionPill(text = "Busca luz uniforme, sin sombras")
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CameraInstructionPill(
                                            text = "Encuadre la hoja de limón dentro de la guía",
                                            rounded = true,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        CameraPlotSelector(
                                            label = selectedPlotLabel,
                                            expanded = selectingPlot,
                                            onExpand = { selectingPlot = true },
                                            onDismiss = { selectingPlot = false },
                                            activePlots = activePlots,
                                            onSelectPlot = { plotId ->
                                                session.selectedPlot.value = plotId
                                                selectingPlot = false
                                            }
                                        )
                                    }
                                }
                                if (errorText.isNotBlank()) {
                                    CameraErrorBanner(errorText)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAnalyzing || captureInProgress) {
                                CameraAnalyzingOverlay()
                            } else {
                                ResponsiveLeafGuide()
                            }
                        }
                    }

                    // Columna derecha para controles en ventanas anchas con poca altura
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(84.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        CameraFlashButton(isFlashOn = isFlashOn, onClick = onFlashToggle)

                        if (isAnalyzing || captureInProgress) {
                            Spacer(modifier = Modifier.size(72.dp))
                            Spacer(modifier = Modifier.size(56.dp))
                        } else {
                            CameraShutterButton(
                                enabled = captureEnabled && hasCameraPermission && imageCapture != null,
                                onClick = onCaptureClick
                            )
                            CameraGalleryButton(
                                enabled = captureEnabled,
                                onClick = onGalleryClick
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 6.dp else 10.dp)
                ) {
                    // Fila superior separada de las instrucciones para evitar solapamientos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CameraCloseButton(onClick = onCloseClick)
                        if (!isCompactWidth) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CameraPlotSelector(
                                    label = selectedPlotLabel,
                                    expanded = selectingPlot,
                                    onExpand = { selectingPlot = true },
                                    onDismiss = { selectingPlot = false },
                                    activePlots = activePlots,
                                    onSelectPlot = { plotId ->
                                        session.selectedPlot.value = plotId
                                        selectingPlot = false
                                    }
                                )
                            }
                        }
                        CameraFlashButton(isFlashOn = isFlashOn, onClick = onFlashToggle)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isCompactWidth) {
                            CameraPlotSelector(
                                label = selectedPlotLabel,
                                expanded = selectingPlot,
                                onExpand = { selectingPlot = true },
                                onDismiss = { selectingPlot = false },
                                activePlots = activePlots,
                                onSelectPlot = { plotId ->
                                    session.selectedPlot.value = plotId
                                    selectingPlot = false
                                }
                            )
                        }
                        if (isCompactHeight) {
                            CameraInstructionPill(text = "Encuadre la hoja con luz uniforme")
                        } else {
                            CameraInstructionPill(text = "Busca luz uniforme, sin sombras")
                            CameraInstructionPill(
                                text = "Encuadre la hoja de limón dentro de la guía",
                                rounded = true
                            )
                        }
                        if (errorText.isNotBlank()) {
                            CameraErrorBanner(errorText)
                        }
                    }

                    // Área libre central para la guía foliar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isAnalyzing && !captureInProgress) {
                            ResponsiveLeafGuide()
                        }
                    }

                    // Controles inferiores accesibles (>= 56dp), acotados en ancho para tablet vertical
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (isCompactHeight) 4.dp else 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 420.dp)
                                .fillMaxWidth()
                                .padding(horizontal = if (isCompactWidth) 8.dp else 16.dp)
                        ) {
                            if (isAnalyzing || captureInProgress) {
                                CameraAnalyzingOverlay(modifier = Modifier.align(Alignment.Center))
                            } else {
                                CameraGalleryButton(
                                    enabled = captureEnabled,
                                    onClick = onGalleryClick,
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                                CameraShutterButton(
                                    enabled = captureEnabled && hasCameraPermission && imageCapture != null,
                                    onClick = onCaptureClick,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResponsiveLeafGuide() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availWidth = (maxWidth - 16.dp).coerceAtLeast(0.dp)
        val availHeight = (maxHeight - 16.dp).coerceAtLeast(0.dp)
        val targetAspect = 220f / 300f
        val maxGuideH = minOf(availHeight, 300.dp)
        val maxGuideW = minOf(availWidth, 220.dp)
        val guideHeight = minOf(maxGuideH, maxGuideW / targetAspect)
        val guideWidth = guideHeight * targetAspect

        if (guideHeight >= 72.dp && guideWidth >= 52.dp) {
            Box(
                modifier = Modifier
                    .width(guideWidth)
                    .height(guideHeight)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
            )
        }
    }
}

@Composable
private fun CameraInstructionPill(
    text: String,
    modifier: Modifier = Modifier,
    rounded: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(if (rounded) RoundedCornerShape(8.dp) else CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = Typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CameraPlotSelector(
    label: String,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    activePlots: List<pe.edu.upn.agrovisionlurin.data.local.LocalPlot>,
    onSelectPlot: (String) -> Unit
) {
    Box {
        Surface(
            onClick = onExpand,
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Place, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    color = Color.White,
                    style = Typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 180.dp)
                )
                Icon(Icons.Rounded.ArrowDropDown, null, tint = Color.White)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            activePlots.forEach { plot ->
                DropdownMenuItem(
                    text = { Text("${plot.code} · ${plot.name}") },
                    onClick = { onSelectPlot(plot.id) }
                )
            }
            if (activePlots.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Crea una parcela en Configuración") },
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun CameraErrorBanner(message: String) {
    Text(
        text = message,
        color = Color.White,
        style = Typography.labelMedium,
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun CameraCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
    ) {
        Icon(Icons.Rounded.Close, contentDescription = "Volver", tint = Color.White)
    }
}

@Composable
private fun CameraFlashButton(isFlashOn: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFlashOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
            contentDescription = "Flash",
            tint = Color.White
        )
    }
}

@Composable
private fun CameraGalleryButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.PhotoLibrary, contentDescription = "Galería", tint = Color.White)
    }
}

@Composable
private fun CameraShutterButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(Icons.Rounded.PhotoCamera, contentDescription = "Capturar", tint = Color.Black)
    }
}

@Composable
private fun CameraAnalyzingOverlay(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = PrimaryGreen)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Analizando tejido foliar...", color = Color.White, style = Typography.labelLarge)
    }
}
