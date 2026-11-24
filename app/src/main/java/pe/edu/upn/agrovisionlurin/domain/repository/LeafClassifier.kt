package pe.edu.upn.agrovisionlurin.domain.repository

import pe.edu.upn.agrovisionlurin.domain.model.DiagnosticResult
import java.io.File

interface LeafClassifier {
    suspend fun classifyImageFile(imageFile: File): DiagnosticResult
}
