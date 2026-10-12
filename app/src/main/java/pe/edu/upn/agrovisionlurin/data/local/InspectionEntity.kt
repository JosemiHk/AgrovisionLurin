package pe.edu.upn.agrovisionlurin.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "inspections",
    foreignKeys = [
        ForeignKey(entity = LocalUser::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = LocalPlot::class, parentColumns = ["id"], childColumns = ["plotId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [
        Index(value = ["syncStatus"]),
        Index(value = ["createdAt"]),
        Index(value = ["userId"]),
        Index(value = ["plotId"])
    ]
)
data class InspectionEntity(
    @PrimaryKey
    val id: String,
    val plotId: String,
    val subPlot: String,
    val imagePath: String,
    val diagnosisClass: Int, // Legacy ordinal: never reinterpret as a new label.
    val confidence: Float,
    val latencyMs: Long,
    val severityLevel: String, // Historical field only; new observations leave empty.
    val recommendationsJson: String,
    val syncStatus: Int, // Historical flag, no remote guarantee.
    val createdAt: Long,
    val userId: String = "__legacy_user__",
    val state: String = "LEGADO",
    val classCode: String = "",
    val classLabel: String = "",
    val modelVersion: String = "",
    val errorMessage: String = "",
    val plotCodeSnapshot: String = "",
    val userNameSnapshot: String = ""
)
