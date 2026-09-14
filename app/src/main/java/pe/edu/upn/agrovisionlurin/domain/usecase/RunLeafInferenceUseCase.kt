package pe.edu.upn.agrovisionlurin.domain.usecase

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import pe.edu.upn.agrovisionlurin.domain.model.DiagnosticResult

class RunLeafInferenceUseCase {

    suspend operator fun invoke(@Suppress("UNUSED_PARAMETER") bitmap: Bitmap): DiagnosticResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        
        // Mock inference latency < 2.0s as per requirement
        delay((500..1800).random().toLong())
        
        // Mock classification distribution
        val rand = (0..100).random()
        val (classification, label, confidence) = when {
            rand < 40 -> Triple(0, "Cultivo Sano", 0.95f)
            rand < 75 -> Triple(1, "Trips tabaci / Daño Foliar", 0.88f)
            else -> Triple(2, "Estrés Hídrico", 0.92f)
        }
        
        val latency = System.currentTimeMillis() - startTime
        
        DiagnosticResult(
            classificationCode = classification,
            label = label,
            confidence = confidence,
            latencyMs = latency
        )
    }
}
