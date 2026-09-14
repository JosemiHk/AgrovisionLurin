package pe.edu.upn.agrovisionlurin.presentation.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upn.agrovisionlurin.presentation.theme.PrimaryGreen
import pe.edu.upn.agrovisionlurin.presentation.theme.Typography
import java.util.concurrent.Executors

@Composable
fun GuidedCameraScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit,
    onCaptureComplete: (String) -> Unit,
    viewModel: CameraViewModel = viewModel()
) {
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
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
    val executor = remember { Executors.newSingleThreadExecutor() }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                viewModel.processImage(context, bitmap, onCaptureComplete)
            }
        }
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
                Text("Se requiere permiso de cámara para la evaluación foliar.", color = Color.White)
            }
        }

        // Overlay: Reticle for Allium cepa (Red Onion)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(120.dp)
                .height(400.dp)
                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(60.dp))
        )

        // Overlay: Dynamic Lighting Pill
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Iluminación Óptima", color = Color.White, style = Typography.labelMedium)
        }

        // Top Right: Flash Toggle
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
            Text(if (isFlashOn) "⚡" else "💡", color = Color.White, style = Typography.titleMedium)
        }

        // Overlay: Bottom Controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 32.dp, end = 32.dp)
        ) {
            if (isAnalyzing) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analizando tejido foliar...", color = Color.White, style = Typography.labelLarge)
                }
            } else {
                // Gallery Button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🖼️", color = Color.White, style = Typography.titleLarge)
                }

                // Capture Button
                Button(
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        capture.takePicture(
                            executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val buffer = image.planes[0].buffer
                                    val bytes = ByteArray(buffer.remaining())
                                    buffer.get(bytes)
                                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    
                                    val matrix = Matrix()
                                    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
                                    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                    
                                    image.close()

                                    viewModel.processImage(context, rotatedBitmap, onCaptureComplete)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                                }
                            }
                        )
                    },
                    modifier = Modifier.align(Alignment.Center).size(72.dp), // Touch target >= 56dp requirement
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(modifier = Modifier.size(60.dp).clip(CircleShape).border(2.dp, Color.Black, CircleShape))
                }
            }
        }
    }
}
