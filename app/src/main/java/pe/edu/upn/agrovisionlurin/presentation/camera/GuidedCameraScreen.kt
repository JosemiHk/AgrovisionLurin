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

        // Overlay: Guía visual neutra para encuadre de hoja de limón sutil
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(220.dp)
                .height(300.dp)
                .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
        )

        // Instrucciones de captura: no se presentan como mediciones automáticas.
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Busca luz uniforme, sin sombras", color = Color.White, style = Typography.labelMedium)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Encuadre la hoja de limón dentro de la guía",
                    color = Color.White,
                    style = Typography.labelMedium
                )
            }
            Box {
                Surface(
                    onClick = { selectingPlot = true },
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Place, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(plots.firstOrNull { it.id == selectedPlot }?.let { "Parcela ${it.code}" } ?: "Seleccionar parcela", color = Color.White, style = Typography.labelMedium)
                        Icon(Icons.Rounded.ArrowDropDown, null, tint = Color.White)
                    }
                }
                DropdownMenu(expanded = selectingPlot, onDismissRequest = { selectingPlot = false }) {
                    plots.filter { it.active }.forEach { plot ->
                        DropdownMenuItem(text = { Text("${plot.code} · ${plot.name}") }, onClick = { session.selectedPlot.value = plot.id; selectingPlot = false })
                    }
                    if (plots.none { it.active }) DropdownMenuItem(text = { Text("Crea una parcela en Configuración") }, onClick = { selectingPlot = false })
                }
            }
            val error = cameraError.ifBlank { message }
            if (error.isNotBlank()) Text(error, color = Color.White, style = Typography.labelMedium,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp)).padding(10.dp))
        }

        IconButton(onClick = { if (!captureInProgress && !isAnalyzing) onBack() }, modifier = Modifier.align(Alignment.TopStart).padding(top = 48.dp, start = 16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
            Icon(Icons.Rounded.Close, contentDescription = "Volver", tint = Color.White)
        }

        // Top Right: Control de Linterna / Flash
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable {
                    isFlashOn = !isFlashOn
                    cameraControl?.enableTorch(isFlashOn)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFlashOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                contentDescription = "Flash",
                tint = Color.White
            )
        }

        // Overlay: Controles de Captura Accesibles (>= 56dp)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 32.dp, end = 32.dp)
        ) {
            if (isAnalyzing || captureInProgress) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analizando tejido foliar...", color = Color.White, style = Typography.labelLarge)
                }
            } else {
                // Selector de Galería
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = captureEnabled) {
                            session.createCapture { galleryLauncher.launch("image/*") }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = "Galería", tint = Color.White)
                }

                // Botón de Disparo File-First
                Button(
                    onClick = {
                        val capture = imageCapture ?: return@Button
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
                    },
                    enabled = captureEnabled && hasCameraPermission && imageCapture != null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Rounded.PhotoCamera, contentDescription = "Capturar", tint = Color.Black)
                }
            }
        }
    }
}
