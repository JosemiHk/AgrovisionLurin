package pe.edu.upn.agrovisionlurin.data.remote

import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AzureSyncApi {
    @POST("api/inspections/sync")
    suspend fun syncInspections(@Body inspections: List<InspectionEntity>): Response<SyncResponse>
}

data class SyncResponse(val success: Boolean, val syncedIds: List<String>)
