package pe.edu.upn.agrovisionlurin.domain.usecase

import pe.edu.upn.agrovisionlurin.domain.model.Recommendation

class GetRecommendationsUseCase {

    operator fun invoke(classId: Int, confidence: Float): List<Recommendation> {
        return when (classId) {
            1 -> { // Trips tabaci / Daño Foliar
                if (confidence >= 0.70f) {
                    listOf(
                        Recommendation("1", "Aislamiento y revisión en 5 metros a la redonda para verificar dispersión de ninfas (Ejecutar dentro de 2 horas)."),
                        Recommendation("2", "Calibrar riego para evitar estrés térmico en el lote (reduce proliferación)."),
                        Recommendation("3", "Aplicación focalizada de bio-insumo o jabón potásico en horas de baja radiación (tarde 16:30 - 18:00 hrs).")
                    )
                } else {
                    listOf(
                        Recommendation("1", "Posible daño inicial. Monitorear zona con trampa cromática azul.")
                    )
                }
            }
            2 -> { // Estrés Hídrico / Déficit de Riego
                if (confidence >= 0.70f) {
                    listOf(
                        Recommendation("1", "Inspeccionar presión de manómetros en cinta de goteo en el surco evaluado."),
                        Recommendation("2", "Programar turno de riego corto complementario en ventana de menor evapotranspiración (madrugada)."),
                        Recommendation("3", "Verificar retención de humedad en raíces de Allium cepa a 15 cm de profundidad.")
                    )
                } else {
                    listOf(
                        Recommendation("1", "Revisar humedad del suelo de forma manual.")
                    )
                }
            }
            0 -> { // Cultivo Sano
                listOf(
                    Recommendation("1", "Condición foliar óptima. Registrar muestreo preventivo en la bitácora."),
                    Recommendation("2", "Programar siguiente inspección de rutina en 7 días.")
                )
            }
            else -> emptyList()
        }
    }
}
