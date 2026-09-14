package pe.edu.upn.agrovisionlurin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upn.agrovisionlurin.data.local.InspectionDao
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity
import pe.edu.upn.agrovisionlurin.domain.model.Inspection
import pe.edu.upn.agrovisionlurin.domain.model.Recommendation
import pe.edu.upn.agrovisionlurin.domain.repository.InspectionRepository
import org.json.JSONArray

class InspectionRepositoryImpl(
    private val dao: InspectionDao
) : InspectionRepository {

    override suspend fun saveInspection(inspection: Inspection) {
        val entity = InspectionEntity(
            id = inspection.id,
            plotId = inspection.plotId,
            subPlot = inspection.subPlot,
            imagePath = inspection.imagePath,
            diagnosisClass = inspection.diagnosisClass,
            confidence = inspection.confidence,
            latencyMs = inspection.latencyMs,
            severityLevel = inspection.severityLevel,
            recommendationsJson = JSONArray(inspection.recommendations.map { it.title }).toString(), // Simplified
            syncStatus = inspection.syncStatus,
            createdAt = inspection.createdAt
        )
        dao.insertInspection(entity)
    }

    override fun getAllInspections(): Flow<List<Inspection>> {
        return dao.getAllInspections().map { entities ->
            entities.map { entity ->
                Inspection(
                    id = entity.id,
                    plotId = entity.plotId,
                    subPlot = entity.subPlot,
                    imagePath = entity.imagePath,
                    diagnosisClass = entity.diagnosisClass,
                    confidence = entity.confidence,
                    latencyMs = entity.latencyMs,
                    severityLevel = entity.severityLevel,
                    recommendations = emptyList(), // Decode JSON if needed
                    syncStatus = entity.syncStatus,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override fun getPendingSyncCount(): Flow<Int> = dao.getPendingSyncCount()

    override suspend fun syncPendingInspections() {
        // Triggered via WorkManager normally, can be explicitly called here too.
    }
}
