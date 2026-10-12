package pe.edu.upn.agrovisionlurin.domain.usecase
import pe.edu.upn.agrovisionlurin.domain.model.Recommendation

/** Prior-screen adapter. Confidence never determines severity or agricultural treatment. */
class GetRecommendationsUseCase {
    @Suppress("UNUSED_PARAMETER")
    operator fun invoke(classId: Int, confidence: Float): List<Recommendation> =
        listOf(Recommendation("Revisión técnica","Registrar observación y solicitar confirmación agronómica."))
}
