package pe.edu.upn.agrovisionlurin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [InspectionEntity::class, LocalUser::class, LocalPlot::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inspectionDao(): InspectionDao
    abstract fun accountDao(): AccountDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS users (id TEXT NOT NULL PRIMARY KEY, username TEXT NOT NULL, displayName TEXT NOT NULL, passwordHash TEXT NOT NULL, passwordSalt TEXT NOT NULL, role TEXT NOT NULL, active INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX index_users_username ON users(username)")
                db.execSQL("INSERT INTO users VALUES ('__legacy_user__','__legacy__','Registro histórico','','','LEGADO',0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS plots (id TEXT NOT NULL PRIMARY KEY, code TEXT NOT NULL, name TEXT NOT NULL, active INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX index_plots_code ON plots(code)")
                db.execSQL("INSERT INTO plots SELECT DISTINCT plotId, plotId, 'Parcela histórica', 0 FROM inspections")
                db.execSQL("CREATE TABLE inspections_new (id TEXT NOT NULL PRIMARY KEY, plotId TEXT NOT NULL, subPlot TEXT NOT NULL, imagePath TEXT NOT NULL, diagnosisClass INTEGER NOT NULL, confidence REAL NOT NULL, latencyMs INTEGER NOT NULL, severityLevel TEXT NOT NULL, recommendationsJson TEXT NOT NULL, syncStatus INTEGER NOT NULL, createdAt INTEGER NOT NULL, userId TEXT NOT NULL, state TEXT NOT NULL, classCode TEXT NOT NULL, classLabel TEXT NOT NULL, modelVersion TEXT NOT NULL, errorMessage TEXT NOT NULL, plotCodeSnapshot TEXT NOT NULL, userNameSnapshot TEXT NOT NULL, FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(plotId) REFERENCES plots(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("INSERT INTO inspections_new SELECT id,plotId,subPlot,imagePath,diagnosisClass,confidence,latencyMs,severityLevel,recommendationsJson,syncStatus,createdAt,'__legacy_user__','LEGADO','',CASE diagnosisClass WHEN 0 THEN 'Cultivo sano (cebolla; histórico)' WHEN 1 THEN 'Trips tabaci (cebolla; histórico)' WHEN 2 THEN 'Estrés hídrico (cebolla; histórico)' ELSE 'Clase histórica no registrada' END,'','',plotId,'Registro histórico' FROM inspections")
                db.execSQL("DROP TABLE inspections")
                db.execSQL("ALTER TABLE inspections_new RENAME TO inspections")
                listOf("syncStatus", "createdAt", "userId", "plotId").forEach {
                    db.execSQL("CREATE INDEX index_inspections_$it ON inspections($it)")
                }
            }
        }
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agrovision_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
