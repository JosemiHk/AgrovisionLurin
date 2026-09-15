package pe.edu.upn.agrovisionlurin.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inspections",
    indices = [
        Index(value = ["syncStatus"]),
        Index(value = ["createdAt"])
    ]
)
data class InspectionEntity(
    @PrimaryKey
    val id: String,
    val plotId: String,
    val subPlot: String,
    val imagePath: String,
    val diagnosisClass: Int, // 0=Sano, 1=Trips, 2=Estrés Hídrico
    val confidence: Float,
    val latencyMs: Long,
    val severityLevel: String, // "Bajo", "Medio", "Crítico"
    val recommendationsJson: String,
    val syncStatus: Int, // 0=Local/Pending, 1=Synced to Azure
    val createdAt: Long
)
