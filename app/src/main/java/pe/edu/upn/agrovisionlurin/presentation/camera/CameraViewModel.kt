package pe.edu.upn.agrovisionlurin.presentation.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.edu.upn.agrovisionlurin.domain.usecase.RunLeafInferenceUseCase
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CameraViewModel : ViewModel() {
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun processImage(context: Context, bitmap: Bitmap, onComplete: (String) -> Unit) {
        _isAnalyzing.value = true
        viewModelScope.launch {
            // Guardar imagen en almacenamiento interno privado
            val imagePath = withContext(Dispatchers.IO) {
                val filename = "capture_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                file.absolutePath
            }

            @Suppress("UNUSED_VARIABLE")
            val runInference = RunLeafInferenceUseCase(context)
            val result = runInference(bitmap)
            
            pe.edu.upn.agrovisionlurin.domain.model.ResultCache.lastResult = result

            // Mock ID de la inspección para navegación
            val mockId = UUID.randomUUID().toString()
            _isAnalyzing.value = false
            onComplete(mockId)
        }
    }
}
