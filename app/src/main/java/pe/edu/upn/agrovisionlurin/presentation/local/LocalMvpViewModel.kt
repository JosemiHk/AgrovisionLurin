package pe.edu.upn.agrovisionlurin.presentation.local

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.edu.upn.agrovisionlurin.data.local.*
import pe.edu.upn.agrovisionlurin.data.local.ml.LemonClassifier
import pe.edu.upn.agrovisionlurin.data.repository.LocalMvpRepository
import java.io.File
import java.util.UUID

class LocalMvpViewModel(app: Application, private val saved: SavedStateHandle) : AndroidViewModel(app) {
    private val repo=LocalMvpRepository(AppDatabase.getDatabase(app))
    private val preferences=app.getSharedPreferences("local_verified_session_v2",0)
    private val classifier=LemonClassifier(app)
    private val _user=MutableStateFlow<LocalUser?>(null); val user=_user.asStateFlow()
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val users=user.flatMapLatest { actor -> if(actor==null) flowOf(emptyList<LocalUser>()) else repo.visibleUsers(actor.id) }
    val plots=repo.plots
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val inspections=user.flatMapLatest { actor -> if(actor==null) flowOf(emptyList<InspectionEntity>()) else repo.visibleInspections(actor.id) }
    private val _modelVersion=MutableStateFlow(""); val modelVersion=_modelVersion.asStateFlow()
    private val _modelAvailable=MutableStateFlow(false); val modelAvailable=_modelAvailable.asStateFlow()
    private val _setup=MutableStateFlow(false); val setup=_setup.asStateFlow()
    private val _ready=MutableStateFlow(false); val ready=_ready.asStateFlow()
    private val _message=MutableStateFlow(""); val message=_message.asStateFlow()
    private val _busy=MutableStateFlow(false); val busy=_busy.asStateFlow()
    private val _detail=MutableStateFlow<InspectionEntity?>(null); val detail=_detail.asStateFlow()
    val selectedPlot=MutableStateFlow("")
    val pendingCaptureId:String? get()=saved["pendingCaptureId"]
    init { action {
        _setup.value=repo.needsSetup()
        _user.value=repo.restore(preferences.getString("user_id",null))
        if(_user.value==null) preferences.edit().clear().apply()
        try {
            val assets=getApplication<Application>().assets
            val metadata=org.json.JSONObject(assets.open("models/model_manifest.json").bufferedReader().use { it.readText() })
            val bytes=assets.open("models/${metadata.getString("model_file")}").use { it.readBytes() }
            val hash=java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            _modelAvailable.value=hash==metadata.getString("model_sha256")
            _modelVersion.value=metadata.getString("model_version")
        } catch(cause:Exception) { if(cause is CancellationException) throw cause; _modelAvailable.value=false }
        _ready.value=true
    } }
    private fun actorId()=_user.value?.id ?: error("Inicia sesión")
    private fun action(block:suspend ()->Unit) {
        if(_busy.value) return
        _busy.value=true;_message.value=""
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { block() } }
            catch(cause:CancellationException) { throw cause }
            catch(cause:Exception) { _message.value=cause.message ?: "Operación fallida" }
            finally { _busy.value=false }
        }
    }
    fun login(username:String,password:String,displayName:String)=action {
        val account=repo.login(username,password,displayName)
        preferences.edit().putString("user_id",account.id).apply();_user.value=account;_setup.value=false
    }
    fun logout() { preferences.edit().clear().apply();_user.value=null;_detail.value=null;selectedPlot.value="";_message.value="" }
    fun saveUser(id:String?,username:String,name:String,password:String,role:String,active:Boolean)=action {
        repo.saveUser(actorId(),id,username,name,password,role,active)
        _user.value=repo.restore(actorId());_message.value="Usuario guardado"
    }
    fun deleteUser(id:String)=action { repo.deleteUser(actorId(),id);_message.value="Usuario eliminado" }
    fun savePlot(id:String?,code:String,name:String,active:Boolean)=action {
        repo.savePlot(actorId(),id,code,name,active);_message.value="Parcela guardada"
    }
    fun deletePlot(id:String)=action {
        repo.deletePlot(actorId(),id);if(selectedPlot.value==id) selectedPlot.value="";_message.value="Parcela eliminada"
    }
    fun createCapture(onReady:(String)->Unit)=action {
        val id=UUID.randomUUID().toString()
        val file=File(getApplication<Application>().filesDir,"images/$id.jpg").also { it.parentFile?.mkdirs() }
        repo.createDraft(actorId(),selectedPlot.value,id,file.path)
        withContext(Dispatchers.Main) {
            saved["pendingCaptureId"]=id
            _busy.value=false
            onReady(file.path)
        }
    }
    fun captureFinished(success:Boolean) = finishPending(success,null)
    fun galleryFinished(uri:android.net.Uri?) = finishPending(uri!=null,uri)
    fun finishCapturedFile(success:Boolean,onComplete:(String)->Unit) = finishPending(success,null,onComplete)
    fun importGallery(uri:android.net.Uri,onComplete:(String)->Unit) = finishPending(true,uri,onComplete)
    fun discardPendingCapture() { if(pendingCaptureId!=null) finishPending(false,null) }
    private fun finishPending(success:Boolean,uri:android.net.Uri?,onComplete:(String)->Unit = {}) {
        // Restored ActivityResult callbacks may arrive while the persisted session is being checked.
        viewModelScope.launch {
            busy.first { !it }
            action {
                val id=withContext(Dispatchers.Main) { pendingCaptureId } ?: error("No se encontró el borrador de captura")
                val record=repo.inspect(actorId(),id)
                withContext(Dispatchers.Main) { saved["pendingCaptureId"]=null }
                if(!success) { repo.deleteInspection(actorId(),id);File(record.imagePath).delete();_message.value="Captura cancelada" }
                else {
                    _detail.value=record
                    if(uri!=null) {
                        try {
                            val resolver=getApplication<Application>().contentResolver
                            requireNotNull(resolver.openInputStream(uri)) { "No se pudo leer la fotografía" }.use { input ->
                                File(record.imagePath).outputStream().use { output -> input.copyTo(output) }
                            }
                        } catch(cause:Exception) {
                            if(cause is CancellationException) throw cause
                            _detail.value=repo.complete(actorId(),id,pe.edu.upn.agrovisionlurin.domain.model.DiagnosticResult(-1,"Importación fallida",0f,0,state="ERROR",errorMessage=cause.message ?: "Error de lectura"))
                            withContext(Dispatchers.Main) { onComplete(id) }
                            return@action
                        }
                    }
                    evaluate(record)
                    withContext(Dispatchers.Main) { onComplete(id) }
                }
            }
        }
    }
    fun open(id:String)=action { _detail.value=repo.inspect(actorId(),id) }
    fun closeDetail() { _detail.value=null }
    fun retry(id:String)=action {
        val record=repo.inspect(actorId(),id)
        require(record.state in listOf("BORRADOR","ERROR")) { "Resultado congelado; captura una inspección nueva" }
        evaluate(record)
    }
    private suspend fun evaluate(record:InspectionEntity) {
        val result=classifier.classifyImageFile(File(record.imagePath))
        _detail.value=repo.complete(actorId(),record.id,result)
    }
    fun updateMetadata(id:String,subPlot:String)=action {
        _detail.value=repo.updateMetadata(actorId(),id,subPlot);_message.value="Nota actualizada; resultado conservado"
    }
    fun deleteInspection(id:String)=action {
        val record=repo.deleteInspection(actorId(),id)
        val file=File(record.imagePath)
        if(file.canonicalPath.startsWith(getApplication<Application>().filesDir.canonicalPath+File.separator)) file.delete()
        if(_detail.value?.id==id) _detail.value=null
    }
    fun export(id:String,onReady:(File)->Unit)=action {
        val record=repo.inspect(actorId(),id)
        val file=File(getApplication<Application>().cacheDir,"exports/$id.csv").also { it.parentFile?.mkdirs() }
        file.writeText(InspectionCsv.render(record),Charsets.UTF_8)
        withContext(Dispatchers.Main) { onReady(file) }
    }
    override fun onCleared() { classifier.close();super.onCleared() }
}

