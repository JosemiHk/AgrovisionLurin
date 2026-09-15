package pe.edu.upn.agrovisionlurin.domain.usecase

import pe.edu.upn.agrovisionlurin.domain.model.DiagnosticResult
import pe.edu.upn.agrovisionlurin.domain.repository.LeafClassifier
import java.io.File

class RunLeafInferenceUseCase(
    private val leafClassifier: LeafClassifier
) {
    suspend operator fun invoke(imageFile: File): DiagnosticResult {
        return leafClassifier.classifyImageFile(imageFile)
    }
}
