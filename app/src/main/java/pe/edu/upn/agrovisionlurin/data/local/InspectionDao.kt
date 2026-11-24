package pe.edu.upn.agrovisionlurin.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.ABORT)
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

    @Update
    suspend fun updateInspection(inspection: InspectionEntity)

    @Query("DELETE FROM inspections WHERE id = :id")
    suspend fun deleteInspection(id: String)
}
