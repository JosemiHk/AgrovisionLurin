package pe.edu.upn.agrovisionlurin.domain.model

data class CachedDiagnostic(
    val inspectionId: String,
    val imagePath: String,
    val result: DiagnosticResult
)

object ResultCache {
    var lastCachedDiagnostic: CachedDiagnostic? = null
    
    var lastResult: DiagnosticResult?
        get() = lastCachedDiagnostic?.result
        set(value) {
            if (value != null) {
                lastCachedDiagnostic = CachedDiagnostic(
                    inspectionId = java.util.UUID.randomUUID().toString(),
                    imagePath = lastCachedDiagnostic?.imagePath ?: "",
                    result = value
                )
            } else {
                lastCachedDiagnostic = null
            }
        }
}
