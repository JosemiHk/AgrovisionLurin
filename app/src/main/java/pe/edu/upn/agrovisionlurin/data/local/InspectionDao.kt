package pe.edu.upn.agrovisionlurin.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Query("SELECT * FROM inspections ORDER BY createdAt DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE syncStatus = 0")
    suspend fun getPendingSyncInspections(): List<InspectionEntity>
    
    @Query("SELECT COUNT(*) FROM inspections WHERE syncStatus = 0")
    fun getPendingSyncCount(): Flow<Int>

    @Query("UPDATE inspections SET syncStatus = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
