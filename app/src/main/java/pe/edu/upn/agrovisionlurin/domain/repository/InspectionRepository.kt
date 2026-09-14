package pe.edu.upn.agrovisionlurin.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upn.agrovisionlurin.domain.model.Inspection

interface InspectionRepository {
    suspend fun saveInspection(inspection: Inspection)
    fun getAllInspections(): Flow<List<Inspection>>
    fun getPendingSyncCount(): Flow<Int>
    suspend fun syncPendingInspections()
}
