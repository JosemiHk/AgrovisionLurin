package pe.edu.upn.agrovisionlurin.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
data class LocalUser(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val passwordHash: String,
    val passwordSalt: String,
    val role: String,
    val active: Boolean = true
)

@Entity(tableName = "plots", indices = [Index(value = ["code"], unique = true)])
data class LocalPlot(@PrimaryKey val id: String, val code: String, val name: String, val active: Boolean = true)

@Dao
interface AccountDao {
    @Query("SELECT * FROM users ORDER BY username") fun users(): Flow<List<LocalUser>>
    @Query("SELECT * FROM plots ORDER BY code") fun plots(): Flow<List<LocalPlot>>
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1") suspend fun findUser(username: String): LocalUser?
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1") suspend fun user(id: String): LocalUser?
    @Query("SELECT * FROM plots WHERE id = :id LIMIT 1") suspend fun plot(id: String): LocalPlot?
    @Query("SELECT COUNT(*) FROM users WHERE id != '__legacy_user__'") suspend fun configuredUsers(): Int
    @Insert suspend fun addUser(user: LocalUser)
    @Update suspend fun updateUser(user: LocalUser)
    @Delete suspend fun deleteUser(user: LocalUser)
    @Insert suspend fun addPlot(plot: LocalPlot)
    @Update suspend fun updatePlot(plot: LocalPlot)
    @Delete suspend fun deletePlot(plot: LocalPlot)
    @Query("SELECT COUNT(*) FROM users WHERE role='ADMIN' AND active=1") suspend fun activeAdmins(): Int
}
