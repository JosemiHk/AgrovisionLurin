package pe.edu.upn.agrovisionlurin.presentation.camera

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.edu.upn.agrovisionlurin.data.local.ml.TFLiteLeafClassifier
import pe.edu.upn.agrovisionlurin.domain.model.CachedDiagnostic
import pe.edu.upn.agrovisionlurin.domain.model.ResultCache
import pe.edu.upn.agrovisionlurin.domain.usecase.RunLeafInferenceUseCase
import java.io.File
import java.util.UUID

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val classifier = TFLiteLeafClassifier.getInstance(application)
    private val runInference = RunLeafInferenceUseCase(classifier)

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun processImageFile(file: File, onComplete: (String) -> Unit) {
        _isAnalyzing.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val result = runInference(file)
            val inspectionId = UUID.randomUUID().toString()

            ResultCache.lastCachedDiagnostic = CachedDiagnostic(
                inspectionId = inspectionId,
                imagePath = file.absolutePath,
                result = result
            )

            withContext(Dispatchers.Main) {
                _isAnalyzing.value = false
                onComplete(inspectionId)
            }
        }
    }
}
