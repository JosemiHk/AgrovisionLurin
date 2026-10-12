package pe.edu.upn.agrovisionlurin.data.local.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import pe.edu.upn.agrovisionlurin.domain.model.DiagnosticResult
import pe.edu.upn.agrovisionlurin.domain.repository.LeafClassifier
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/** Single strict FLOAT32 contract shared with the reproducible Python pipeline. */
class LemonClassifier(private val context: Context, private val manifestAsset: String = "models/model_manifest.json") : LeafClassifier {
    private val mutex = Mutex()
    private var interpreter: Interpreter? = null
    private var manifest: JSONObject? = null
    @Volatile private var closed = false
    fun close() {
        closed = true
        if (mutex.tryLock()) try { interpreter?.close(); interpreter=null; manifest=null } finally { mutex.unlock() }
    }
    private fun load(): Pair<Interpreter, JSONObject> {
        check(!closed) { "Clasificador cerrado" }
        interpreter?.let { return it to requireNotNull(manifest) }
        val metadata = JSONObject(context.assets.open(manifestAsset).bufferedReader().use { it.readText() })
        val file = metadata.getString("model_file")
        require(file == "lemon_leaf_mobilenetv2_float32.tflite") { "Nombre de modelo incompatible" }
        require(metadata.getString("input_dtype") == "FLOAT32" && metadata.getString("output_dtype") == "FLOAT32")
        require(metadata.getString("normalization") == "minus_one_to_one" && metadata.getString("input_color") == "RGB")
        require(metadata.getString("resize") == "bilinear")
        require(metadata.getJSONArray("input_shape").toString() == "[1,224,224,3]")
        require(metadata.getJSONArray("output_shape").toString() == "[1,2]")
        require(metadata.getJSONArray("labels").toString() == "[\"MINADOR_COMPATIBLE\",\"SIN_SINTOMAS_VISIBLES\"]")
        require(metadata.getDouble("abstention_threshold") in 0.5..1.0)
        require(metadata.getString("model_version").isNotBlank())
        val bytes = context.assets.open("models/$file").use { it.readBytes() }
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        require(hash == metadata.getString("model_sha256")) { "Hash del modelo no coincide" }
        val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).apply { put(bytes); rewind() }
        val loaded = Interpreter(buffer, Interpreter.Options().apply { setNumThreads(2) })
        try {
            require(loaded.inputTensorCount == 1 && loaded.outputTensorCount == 1)
            val input = loaded.getInputTensor(0); val output = loaded.getOutputTensor(0)
            require(input.shape().contentEquals(intArrayOf(1,224,224,3)) && output.shape().contentEquals(intArrayOf(1,2)))
            require(input.dataType() == DataType.FLOAT32 && output.dataType() == DataType.FLOAT32)
            require(input.quantizationParams().scale == 0f && output.quantizationParams().scale == 0f)
            val inputQuant=metadata.getJSONObject("input_quantization"); val outputQuant=metadata.getJSONObject("output_quantization")
            require(inputQuant.getDouble("scale")==input.quantizationParams().scale.toDouble() && inputQuant.getInt("zero_point")==input.quantizationParams().zeroPoint)
            require(outputQuant.getDouble("scale")==output.quantizationParams().scale.toDouble() && outputQuant.getInt("zero_point")==output.quantizationParams().zeroPoint)
        } catch (error: Exception) { loaded.close(); throw error }
        interpreter = loaded; manifest = metadata
        return loaded to metadata
    }
    override suspend fun classifyImageFile(imageFile: File): DiagnosticResult = withContext(Dispatchers.Default) {
        mutex.withLock {
            val start = SystemClock.elapsedRealtime()
            try {
                require(imageFile.isFile && imageFile.length() > 0) { "Imagen ausente o vacía" }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(imageFile.path, bounds)
                require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Imagen corrupta o formato no compatible" }
                var sample = 1
                while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
                val decoded = BitmapFactory.decodeFile(imageFile.path, BitmapFactory.Options().apply { inSampleSize = sample })
                    ?: error("No se pudo decodificar la imagen")
                val orientation = try { ExifInterface(imageFile.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
                    catch(ignored:java.io.IOException) { ExifInterface.ORIENTATION_NORMAL }
                val matrix = Matrix().apply {
                    when(orientation) {
                        2 -> setScale(-1f,1f); 3 -> setRotate(180f); 4 -> setScale(1f,-1f)
                        5 -> { setRotate(90f); postScale(-1f,1f) }; 6 -> setRotate(90f)
                        7 -> { setRotate(-90f); postScale(-1f,1f) }; 8 -> setRotate(-90f)
                    }
                }
                val rotated = Bitmap.createBitmap(decoded,0,0,decoded.width,decoded.height,matrix,true)
                if(rotated !== decoded) decoded.recycle()
                val scaled = Bitmap.createScaledBitmap(rotated,224,224,true)
                if(scaled !== rotated) rotated.recycle()
                val pixels = IntArray(224*224)
                scaled.getPixels(pixels,0,224,0,0,224,224); scaled.recycle()
                val low=intArrayOf(255,255,255);val high=intArrayOf(0,0,0)
                pixels.forEach { pixel ->
                    val rgb=intArrayOf((pixel shr 16) and 255,(pixel shr 8) and 255,pixel and 255)
                    for(channel in 0..2) { low[channel]=minOf(low[channel],rgb[channel]);high[channel]=maxOf(high[channel],rgb[channel]) }
                }
                require((0..2).any { high[it]-low[it]>=8 }) { "Imagen sin detalles suficientes: captura una hoja enfocada (no valida que la imagen sea una hoja)" }
                val (model, metadata) = load()
                val buffer = ByteBuffer.allocateDirect(4*224*224*3).order(ByteOrder.nativeOrder())
                pixels.forEach { pixel ->
                    buffer.putFloat(((pixel shr 16) and 255)/127.5f-1f)
                    buffer.putFloat(((pixel shr 8) and 255)/127.5f-1f)
                    buffer.putFloat((pixel and 255)/127.5f-1f)
                }
                buffer.rewind()
                val output = Array(1) { FloatArray(2) }; model.run(buffer, output)
                val scores = output[0]
                require(scores.all { it.isFinite() && it in 0f..1f } && kotlin.math.abs(scores.sum()-1f)<0.01f) { "Salida del modelo inválida" }
                val index = if(scores[0] >= scores[1]) 0 else 1
                val accepted = scores[index] >= metadata.getDouble("abstention_threshold")
                DiagnosticResult(index, if(!accepted) "Resultado no concluyente" else if(index==0) "Daño compatible con minador" else "Sin síntomas visibles",
                    scores[index],SystemClock.elapsedRealtime()-start,
                    state=if(accepted) "VALIDO" else "NO_CONCLUYENTE",
                    classCode=if(accepted) metadata.getJSONArray("labels").getString(index) else "",
                    modelVersion=metadata.getString("model_version"))
            } catch(error: CancellationException) { throw error }
            catch(error: Exception) {
                DiagnosticResult(-1,"Evaluación no disponible",0f,SystemClock.elapsedRealtime()-start,
                    state="ERROR", errorMessage=error.message ?: "Error de inferencia")
            } finally { if(closed) { interpreter?.close();interpreter=null;manifest=null } }
        }
    }
}
