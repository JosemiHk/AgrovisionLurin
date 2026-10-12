package pe.edu.upn.agrovisionlurin.domain.model

data class DiagnosticResult(
    val classId: Int,
    val className: String,
    val confidence: Float,
    val latencyMs: Long,
    val severityLevel: String = "",
    val state: String = "VALIDO",
    val classCode: String = "",
    val modelVersion: String = "",
    val errorMessage: String = ""
)
