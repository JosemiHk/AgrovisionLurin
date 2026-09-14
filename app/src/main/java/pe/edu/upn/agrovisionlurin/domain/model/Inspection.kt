package pe.edu.upn.agrovisionlurin.domain.model

data class Inspection(
    val id: String,
    val plotId: String,
    val subPlot: String,
    val imagePath: String,
    val diagnosisClass: Int,
    val confidence: Float,
    val latencyMs: Long,
    val severityLevel: String,
    val recommendations: List<Recommendation>,
    val syncStatus: Int,
    val createdAt: Long
)
