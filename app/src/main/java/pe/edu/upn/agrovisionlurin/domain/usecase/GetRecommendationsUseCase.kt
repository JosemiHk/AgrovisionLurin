package pe.edu.upn.agrovisionlurin.domain.usecase

import pe.edu.upn.agrovisionlurin.domain.model.Recommendation

class GetRecommendationsUseCase {

    operator fun invoke(classificationCode: Int): List<Recommendation> {
        return when (classificationCode) {
            1 -> listOf(
                Recommendation("Aislamiento", "Revisión en radio de 5m por propagación de Trips tabaci."),
                Recommendation("Tratamiento", "Aplicación de bioinsumos o jabón potásico en horario de baja temperatura (mañana/noche)."),
                Recommendation("Control", "Evitar el estrés térmico en el cultivo.")
            )
            2 -> listOf(
                Recommendation("Calibración", "Revisar y calibrar el manómetro de presión en las líneas de goteo."),
                Recommendation("Riego", "Programar turnos de riego cortos en horas de baja evapotranspiración.")
            )
            else -> listOf(
                Recommendation("Monitoreo", "Registro de revisión rutinaria completado con estado saludable."),
                Recommendation("Siguiente Acción", "Siguiente barrido programado en 7 días.")
            )
        }
    }
}
