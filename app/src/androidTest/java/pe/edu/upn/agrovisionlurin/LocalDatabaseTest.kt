package pe.edu.upn.agrovisionlurin

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import pe.edu.upn.agrovisionlurin.data.local.*
import pe.edu.upn.agrovisionlurin.data.local.ml.LemonClassifier
import pe.edu.upn.agrovisionlurin.data.local.ml.PillowBilinearResizer
import pe.edu.upn.agrovisionlurin.data.repository.LocalMvpRepository
import pe.edu.upn.agrovisionlurin.domain.model.DiagnosticResult
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LocalDatabaseTest {
    private val context=ApplicationProvider.getApplicationContext<Context>()
    private val fixtureAssets get()=InstrumentationRegistry.getInstrumentation().context.assets
    @Test fun realMigrationPreservesOriginalDiagnosisAndAddsForeignKeys()=runBlocking {
        val name="migration-${UUID.randomUUID()}"
        val helper=FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context).name(name)
            .callback(object:SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db:SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE inspections (id TEXT NOT NULL PRIMARY KEY, plotId TEXT NOT NULL, subPlot TEXT NOT NULL, imagePath TEXT NOT NULL, diagnosisClass INTEGER NOT NULL, confidence REAL NOT NULL, latencyMs INTEGER NOT NULL, severityLevel TEXT NOT NULL, recommendationsJson TEXT NOT NULL, syncStatus INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
                    db.execSQL("CREATE INDEX index_inspections_syncStatus ON inspections(syncStatus)")
                    db.execSQL("CREATE INDEX index_inspections_createdAt ON inspections(createdAt)")
                    db.execSQL("INSERT INTO inspections VALUES ('old','P01','sector','/old.jpg',1,0.88,120,'Alta','[\"original\"]',1,123)")
                }
                override fun onUpgrade(db:SupportSQLiteDatabase,oldVersion:Int,newVersion:Int) = Unit
            }).build())
        helper.writableDatabase;helper.close()
        val db=Room.databaseBuilder(context,AppDatabase::class.java,name).addMigrations(AppDatabase.MIGRATION_1_2).build()
        try {
            val record=requireNotNull(db.inspectionDao().getInspectionById("old"))
            assertEquals("LEGADO",record.state);assertEquals(1,record.diagnosisClass)
            assertTrue(record.classLabel.contains("Trips tabaci"))
            assertEquals("Alta",record.severityLevel);assertEquals("[\"original\"]",record.recommendationsJson)
            assertEquals(1,record.syncStatus);assertEquals("__legacy_user__",record.userId)
            assertEquals("P01",db.accountDao().plot("P01")?.code)
            assertFalse(requireNotNull(db.accountDao().user("__legacy_user__")).active)
            db.openHelper.writableDatabase.query("PRAGMA foreign_key_check").use { assertEquals(0,it.count) }
            assertFails { db.accountDao().deletePlot(requireNotNull(db.accountDao().plot("P01"))) }
        } finally { db.close();context.deleteDatabase(name) }
    }
    @Test fun authCrudIdentityAndFrozenResultsEnforceRepositoryRules()=runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        try {
            val repo=LocalMvpRepository(db)
            val admin=repo.login("Admin","password123","Administrador")
            assertEquals("ADMIN",admin.role)
            assertEquals(admin.id,repo.login("ADMIN","password123","").id)
            assertFails { repo.login("admin","incorrect","") }
            repo.saveUser(admin.id,null,"operator","Operador","password123","OPERADOR",true)
            val operator=requireNotNull(db.accountDao().findUser("operator"))
            assertTrue(repo.visibleUsers(operator.id).first().isEmpty())
            val displayed=repo.visibleUsers(admin.id).first()
            assertEquals(2,displayed.size)
            assertTrue(displayed.all { it.passwordHash.isEmpty() && it.passwordSalt.isEmpty() })
            assertTrue(requireNotNull(db.accountDao().user(admin.id)).passwordHash.isNotEmpty())
            assertTrue(repo.visibleUsers("missing-user").first().isEmpty())
            repo.saveUser(admin.id,operator.id,"operator","Operador","","OPERADOR",false)
            assertTrue(repo.visibleUsers(operator.id).first().isEmpty())
            assertFails { repo.savePlot(operator.id,null,"P0","Forbidden",true) }
            assertFails { repo.deleteUser(admin.id,admin.id) }
            assertFails { repo.saveUser(admin.id,admin.id,"admin","Admin","","OPERADOR",true) }
            assertFails { repo.saveUser(admin.id,null,"ADMIN","Duplicate","password123","ADMIN",true) }
            assertFails { repo.savePlot(admin.id,"does-not-exist","P01","Invalid",true) }
        } finally { db.close() }
    }
    @Test fun inspectionIdentitySurvivesSecondResultAndMetadataUpdate()=runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        try {
            val repo=LocalMvpRepository(db);val admin=repo.login("admin","password123","Admin")
            repo.savePlot(admin.id,null,"P01","Parcela",true)
            val plot=requireNotNull(db.accountDao().findPlotForTest("P01"))
            repo.createDraft(admin.id,plot,"A","/a.jpg");repo.createDraft(admin.id,plot,"B","/b.jpg")
            repo.complete(admin.id,"A",DiagnosticResult(0,"Daño compatible con minador",0.9f,12,classCode="MINADOR_COMPATIBLE",modelVersion="v1"))
            repo.complete(admin.id,"B",DiagnosticResult(1,"Sin síntomas visibles",0.95f,14,classCode="SIN_SINTOMAS_VISIBLES",modelVersion="v1"))
            val a=repo.inspect(admin.id,"A");assertEquals("MINADOR_COMPATIBLE",a.classCode);assertEquals("/a.jpg",a.imagePath)
            val edited=repo.updateMetadata(admin.id,"A","sector 2")
            assertEquals(a.classCode,edited.classCode);assertEquals(a.modelVersion,edited.modelVersion)
            assertTrue(InspectionCsv.render(a).contains("\"A\""));assertFalse(InspectionCsv.render(a).contains("/b.jpg"))
            assertFails { repo.complete(admin.id,"A",DiagnosticResult(1,"Changed",0.9f,1)) }
            assertFails { repo.deletePlot(admin.id,plot) }
        } finally { db.close() }
    }
    @Test fun invalidImageAndMissingModelNeverProduceDiagnosis()=runBlocking {
        val result=LemonClassifier(context).classifyImageFile(File(context.cacheDir,"missing-${UUID.randomUUID()}.jpg"))
        assertEquals("ERROR",result.state);assertEquals("",result.classCode);assertEquals(-1,result.classId)
        val file=File(context.cacheDir,"valid-${UUID.randomUUID()}.png")
        val bitmap=android.graphics.Bitmap.createBitmap(32,32,android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.GREEN);bitmap.setPixel(16,16,android.graphics.Color.WHITE)
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
        val classifier=LemonClassifier(context,"models/nonexistent-test-manifest.json")
        try { val missing=classifier.classifyImageFile(file);assertEquals("ERROR",missing.state);assertEquals("",missing.classCode) }
        finally { classifier.close();file.delete() }
    }
    private suspend fun assertFails(block:suspend ()->Unit) {
        var failed=false
        try { block() } catch(expected:Exception) { failed=true }
        assertTrue("Expected operation to fail",failed)
    }
    @Test fun rawInterpreterFloat32ParityForPipelineFixtures() {
        val fixture=org.json.JSONObject(fixtureAssets.open("models/parity_fixtures.json").bufferedReader().use { it.readText() })
        val bytes=context.assets.open("models/lemon_leaf_mobilenetv2_float32.tflite").use { it.readBytes() }
        val buffer=java.nio.ByteBuffer.allocateDirect(bytes.size).order(java.nio.ByteOrder.nativeOrder()).apply {put(bytes);rewind()}
        val interpreter=org.tensorflow.lite.Interpreter(buffer)
        try {
            val values=fixture.getJSONArray("fixtures")
            for(index in 0 until values.length()) {
                val value=values.getJSONObject(index);val rgb=value.getJSONArray("rgb")
                val input=java.nio.ByteBuffer.allocateDirect(4*224*224*3).order(java.nio.ByteOrder.nativeOrder())
                repeat(224*224) { for(channel in 0..2) input.putFloat(rgb.getInt(channel)/127.5f-1f) };input.rewind()
                val output=Array(1) {FloatArray(2)};interpreter.run(input,output)
                val probabilities=value.getJSONArray("probabilities")
                for(channel in 0..1) assertEquals(probabilities.getDouble(channel),output[0][channel].toDouble(),fixture.getDouble("tolerance"))
            }
        } finally { interpreter.close() }
    }
    @Test fun uniformImagesFailQualityCheckInsteadOfFabricatedDiagnosis()=runBlocking {
        val classifier=LemonClassifier(context)
        try { listOf(android.graphics.Color.BLACK,android.graphics.Color.WHITE,android.graphics.Color.GREEN,android.graphics.Color.RED).forEach { color ->
            val bitmap=android.graphics.Bitmap.createBitmap(224,224,android.graphics.Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
            val file=File(context.cacheDir,"quality-$color.png")
            try {
                file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
                val result=classifier.classifyImageFile(file);assertEquals("ERROR",result.state);assertEquals("",result.classCode)
                assertTrue(result.errorMessage.contains("sin detalles"))
            } finally { file.delete() }
        } } finally { classifier.close() }
    }
    @Test fun identicalNaturalInputTensorsMatchPythonRuntime() {
        val fixture=org.json.JSONObject(fixtureAssets.open("models/parity_fixtures.json").bufferedReader().use {it.readText()})
        val bytes=context.assets.open("models/lemon_leaf_mobilenetv2_float32.tflite").use {it.readBytes()}
        val model=java.nio.ByteBuffer.allocateDirect(bytes.size).order(java.nio.ByteOrder.nativeOrder()).apply {put(bytes);rewind()}
        val interpreter=org.tensorflow.lite.Interpreter(model)
        try {
            val natural=fixture.getJSONArray("natural_fixtures")
            for(index in 0 until natural.length()) {
                val sample=natural.getJSONObject(index)
                val inputBytes=fixtureAssets.open("models/"+sample.getString("input_file")).use {it.readBytes()}
                assertEquals(4*224*224*3,inputBytes.size)
                val input=java.nio.ByteBuffer.allocateDirect(inputBytes.size).order(java.nio.ByteOrder.LITTLE_ENDIAN).apply {put(inputBytes);rewind()}
                val output=Array(1) {FloatArray(2)};interpreter.run(input,output)
                val expected=sample.getJSONArray("probabilities")
                for(channel in 0..1) assertEquals(expected.getDouble(channel),output[0][channel].toDouble(),fixture.getDouble("natural_fixture_tensor_probability_tolerance"))
            }
        } finally {interpreter.close()}
    }
    @Test fun greenGraphicWithDetailDoesNotProduceADiagnosis() = runBlocking {
        val bitmap = android.graphics.Bitmap.createBitmap(224, 224, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint()
        canvas.drawColor(android.graphics.Color.LTGRAY)
        paint.color = android.graphics.Color.rgb(30, 142, 40)
        canvas.drawRoundRect(15f, 60f, 209f, 165f, 10f, 10f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 32f
        canvas.drawText("ACEPTAR", 34f, 127f, paint)
        val file = File.createTempFile("green-button-", ".png", context.cacheDir)
        val classifier = LemonClassifier(context)
        try {
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            val result = classifier.classifyImageFile(file)
            assertEquals("NO_CONCLUYENTE", result.state)
            assertEquals("", result.classCode)
            assertEquals(-1, result.classId)
            assertTrue(result.className.contains("hoja de limón"))
            assertEquals(-1, classifier.classifyImageFile(file, confirmedOrigin = true).classId)
        } finally {
            classifier.close()
            bitmap.recycle()
            file.delete()
        }
    }
    @Test fun transparentCutoutHasTheSameInputAsAWhiteBackground() {
        val cutout = android.graphics.Bitmap.createBitmap(32, 32, android.graphics.Bitmap.Config.ARGB_8888)
        val flattened = cutout.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
        try {
            val paint = android.graphics.Paint().apply { color = android.graphics.Color.GREEN }
            android.graphics.Canvas(cutout).drawOval(8f, 3f, 24f, 29f, paint)
            flattened.eraseColor(android.graphics.Color.WHITE)
            android.graphics.Canvas(flattened).drawBitmap(cutout, 0f, 0f, null)
            assertArrayEquals(PillowBilinearResizer.resizePixels(flattened), PillowBilinearResizer.resizePixels(cutout))
        } finally {
            cutout.recycle()
            flattened.recycle()
        }
    }
    @Test fun onlyAnInspectionWaitingForOriginCanBeConfirmed() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repo = LocalMvpRepository(db)
            val actor = repo.login("admin", "password123", "Admin").id
            repo.savePlot(actor, null, "P01", "Parcela", true)
            repo.createDraft(actor, requireNotNull(db.accountDao().findPlotForTest("P01")), "leaf", "/leaf.png")
            val final = DiagnosticResult(0, "Daño compatible con minador", .98f, 10,
                classCode = "MINADOR_COMPATIBLE", modelVersion = "test+origen-confirmado")
            assertFails { repo.complete(actor, "leaf", final, confirmedOrigin = true) }
            repo.complete(actor, "leaf", DiagnosticResult(pe.edu.upn.agrovisionlurin.domain.model.LEAF_ORIGIN_PENDING,
                "¿La hoja proviene de un limonero?", 0f, 10, state = "NO_CONCLUYENTE", modelVersion = "test"))
            assertTrue(repo.inspect(actor, "leaf").awaitingLemonOrigin)
            assertFails { repo.complete(actor, "leaf", final) }
            assertEquals(final.classCode, repo.complete(actor, "leaf", final, confirmedOrigin = true).classCode)
            assertFails { repo.complete(actor, "leaf", final, confirmedOrigin = true) }
        } finally { db.close() }
    }
    @Test fun naturalPhotographOutputsMatchProposedProbabilityTolerance()=runBlocking {
        // Unlike raw tensor parity, this measures the complete JPEG/EXIF/Android resize path.
        // The 0.02 probability tolerance is provisional until this runs on the target device.
        val fixture=org.json.JSONObject(fixtureAssets.open("models/parity_fixtures.json").bufferedReader().use {it.readText()})
        val classifier=LemonClassifier(context)
        try {
            val samples=fixture.getJSONArray("natural_fixtures")
            for(index in 0 until samples.length()) {
                val sample=samples.getJSONObject(index);val file=File(context.cacheDir,"natural-parity-$index.jpg")
                try {
                    fixtureAssets.open("models/"+sample.getString("image_file")).use {input -> file.outputStream().use {input.copyTo(it)}}
                    val result=classifier.classifyImageFile(file);assertEquals("VALIDO",result.state)
                    val expected=sample.getJSONArray("probabilities")
                    val winner=if(expected.getDouble(0)>=expected.getDouble(1)) 0 else 1
                    assertEquals(winner,result.classId)
                    assertEquals(expected.getDouble(winner),result.confidence.toDouble(),0.02)
                } finally {file.delete()}
            }
        } finally {classifier.close()}
    }
    private suspend fun AccountDao.findPlotForTest(code:String):String? {
        // Flow snapshot uses the same Room DAO queried by production; no test-only production methods.
        return plots().first().firstOrNull { it.code==code }?.id
    }
}
