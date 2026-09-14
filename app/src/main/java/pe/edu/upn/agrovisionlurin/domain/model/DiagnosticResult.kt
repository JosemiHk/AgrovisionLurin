package pe.edu.upn.agrovisionlurin.domain.model

data class DiagnosticResult(
    val classificationCode: Int,
    val label: String,
    val confidence: Float,
    val latencyMs: Long
)
