package pe.edu.upn.agrovisionlurin.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspections(inspections: List<InspectionEntity>)

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    suspend fun getInspectionById(id: String): InspectionEntity?

    @Query("SELECT * FROM inspections ORDER BY createdAt DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Transaction
    @Query("SELECT * FROM inspections WHERE syncStatus = 0 ORDER BY createdAt ASC")
    suspend fun getPendingSyncInspections(): List<InspectionEntity>
    
    @Query("SELECT COUNT(*) FROM inspections WHERE syncStatus = 0")
    fun getPendingSyncCount(): Flow<Int>

    @Query("UPDATE inspections SET syncStatus = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Transaction
    @Query("UPDATE inspections SET syncStatus = 1 WHERE id IN (:ids)")
    suspend fun markAllAsSynced(ids: List<String>)
}
