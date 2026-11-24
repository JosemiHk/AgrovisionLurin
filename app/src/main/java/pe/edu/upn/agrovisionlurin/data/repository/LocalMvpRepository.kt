package pe.edu.upn.agrovisionlurin.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import pe.edu.upn.agrovisionlurin.data.local.*
import pe.edu.upn.agrovisionlurin.domain.model.DiagnosticResult
import java.util.Locale
import java.util.UUID

/** Local relational application boundary; permission checks apply independently of Compose. */
class LocalMvpRepository(private val db: AppDatabase) {
    private val accounts=db.accountDao(); private val records=db.inspectionDao()
    val plots=accounts.plots(); val inspections=records.getAllInspections()
    fun visibleInspections(actorId:String):Flow<List<InspectionEntity>> = combine(accounts.users(),records.getAllInspections()) { users,rows ->
        val requester=users.firstOrNull { it.id==actorId && it.active && it.role!="LEGADO" }
        when {
            requester==null -> emptyList()
            requester.role=="ADMIN" -> rows
            else -> rows.filter { it.userId==actorId }
        }
    }
    fun visibleUsers(actorId:String):Flow<List<LocalUser>> = accounts.users().map { rows ->
        val requester=rows.firstOrNull { it.id==actorId }
        if(requester?.active!=true || requester.role!="ADMIN") emptyList()
        else rows.filter { it.role!="LEGADO" }.map { it.copy(passwordHash="",passwordSalt="") }
    }
    suspend fun needsSetup()=accounts.configuredUsers()==0
    suspend fun restore(id:String?)=id?.let { accounts.user(it) }?.takeIf { it.active && it.role!="LEGADO" }
    private suspend fun actor(id:String):LocalUser=accounts.user(id)?.takeIf { it.active && it.role!="LEGADO" } ?: error("Sesión inválida o usuario desactivado")
    private suspend fun admin(id:String)=actor(id).also { require(it.role=="ADMIN") { "Solo administrador" } }
    suspend fun login(username:String,password:String,name:String):LocalUser = db.withTransaction {
        val normalized=username.trim().lowercase(Locale.ROOT)
        require(normalized.matches(Regex("[a-z0-9._-]{3,40}"))) { "Usuario: 3–40 letras, números, punto o guion" }
        if(accounts.configuredUsers()==0) {
            require(password.length>=8 && name.isNotBlank()) { "Nombre y contraseña de al menos 8 caracteres requeridos" }
            val salt=PasswordHash.salt()
            LocalUser(UUID.randomUUID().toString(),normalized,name.trim(),PasswordHash.hash(password,salt),salt,"ADMIN").also { accounts.addUser(it) }
        } else {
            val found=accounts.findUser(normalized)
            require(found!=null && found.active && PasswordHash.verify(password,found.passwordSalt,found.passwordHash)) { "Credenciales incorrectas o cuenta desactivada" }
            found
        }
    }
    suspend fun saveUser(actorId:String,id:String?,username:String,name:String,password:String,role:String,active:Boolean) = db.withTransaction {
        val actor=admin(actorId); val normalized=username.trim().lowercase(Locale.ROOT)
        require(normalized.matches(Regex("[a-z0-9._-]{3,40}")) && name.isNotBlank()) { "Usuario o nombre inválido" }
        require(role in listOf("ADMIN","OPERADOR")) { "Rol inválido" }
        val old=id?.let { accounts.user(it) ?: error("Usuario inexistente") }
        require(old?.role!="LEGADO") { "Registro histórico protegido" }
        if(old?.id==actor.id) require(active && role=="ADMIN") { "No puedes desactivar o degradar tu propia cuenta" }
        if(old?.role=="ADMIN" && old.active && (!active || role!="ADMIN")) require(accounts.activeAdmins()>1) { "Debe quedar un administrador activo" }
        require(old!=null || password.length>=8) { "Contraseña de al menos 8 caracteres requerida" }
        require(password.isEmpty() || password.length>=8) { "Contraseña demasiado corta" }
        val salt=if(password.isEmpty()) requireNotNull(old).passwordSalt else PasswordHash.salt()
        val hash=if(password.isEmpty()) requireNotNull(old).passwordHash else PasswordHash.hash(password,salt)
        val value=LocalUser(id ?: UUID.randomUUID().toString(),normalized,name.trim(),hash,salt,role,active)
        if(old==null) accounts.addUser(value) else accounts.updateUser(value)
    }
    suspend fun deleteUser(actorId:String,id:String) = db.withTransaction {
        val actor=admin(actorId); val target=accounts.user(id) ?: error("Usuario inexistente")
        require(target.id!=actor.id && target.role!="LEGADO") { "No puedes eliminar esta cuenta" }
        if(target.role=="ADMIN" && target.active) require(accounts.activeAdmins()>1) { "Debe quedar un administrador activo" }
        try { accounts.deleteUser(target) } catch(cause:android.database.sqlite.SQLiteConstraintException) { error("Usuario con inspecciones: desactívalo para conservar trazabilidad") }
    }
    suspend fun savePlot(actorId:String,id:String?,code:String,name:String,active:Boolean) = db.withTransaction {
        admin(actorId);require(code.trim().isNotEmpty() && name.trim().isNotEmpty()) { "Código y nombre requeridos" }
        val value=LocalPlot(id ?: UUID.randomUUID().toString(),code.trim(),name.trim(),active)
        if(id==null) accounts.addPlot(value) else { require(accounts.plot(id)!=null); accounts.updatePlot(value) }
    }
    suspend fun deletePlot(actorId:String,id:String) = db.withTransaction {
        admin(actorId); val value=accounts.plot(id) ?: error("Parcela inexistente")
        try { accounts.deletePlot(value) } catch(cause:android.database.sqlite.SQLiteConstraintException) { error("Parcela con inspecciones: desactívala para conservar trazabilidad") }
    }
    suspend fun createDraft(actorId:String,plotId:String,id:String,imagePath:String):InspectionEntity = db.withTransaction {
        val actor=actor(actorId);val plot=accounts.plot(plotId)?.takeIf { it.active } ?: error("Selecciona una parcela activa")
        InspectionEntity(id,plot.id,"",imagePath,-1,0f,0,"","",0,System.currentTimeMillis(),actor.id,"BORRADOR",
            plotCodeSnapshot=plot.code,userNameSnapshot=actor.displayName).also { records.insertInspection(it) }
    }
    suspend fun inspect(actorId:String,id:String):InspectionEntity {
        val actor=actor(actorId);val record=records.getInspectionById(id) ?: error("Inspección inexistente")
        require(actor.role=="ADMIN" || actor.id==record.userId) { "Inspección de otro usuario" };return record
    }
    suspend fun complete(actorId:String,id:String,result:DiagnosticResult):InspectionEntity = db.withTransaction {
        val original=inspect(actorId,id)
        require(original.state in listOf("BORRADOR","ERROR")) { "Resultado congelado; captura una inspección nueva" }
        require(result.state in listOf("VALIDO","NO_CONCLUYENTE","ERROR") && result.latencyMs>=0) { "Estado de inferencia inválido" }
        require(result.confidence.isFinite() && result.confidence in 0f..1f) { "Puntuación inválida" }
        when(result.state) {
            "VALIDO" -> require(result.classCode in listOf("MINADOR_COMPATIBLE","SIN_SINTOMAS_VISIBLES") && result.modelVersion.isNotBlank()) { "Resultado sin contrato de modelo" }
            "NO_CONCLUYENTE" -> require(result.classCode.isEmpty() && result.modelVersion.isNotBlank()) { "Abstención sin contrato de modelo" }
            "ERROR" -> require(result.classCode.isEmpty() && result.classId == -1) { "Un error no puede contener un diagnóstico" }
        }
        val recommendation=when(result.classCode) {
            "MINADOR_COMPATIBLE" -> "Registrar daño y consultar al técnico agronómico para confirmar minador."
            "SIN_SINTOMAS_VISIBLES" -> "Registrar observación y continuar monitoreo; esto no descarta otras alteraciones."
            else -> "Repetir una captura clara de una sola hoja y solicitar revisión técnica."
        }
        original.copy(diagnosisClass=result.classId,state=result.state,classCode=result.classCode,classLabel=result.className,
            confidence=result.confidence,latencyMs=result.latencyMs,modelVersion=result.modelVersion,errorMessage=result.errorMessage,
            recommendationsJson=org.json.JSONArray(listOf(recommendation)).toString()).also { records.updateInspection(it) }
    }
    suspend fun updateMetadata(actorId:String,id:String,note:String):InspectionEntity = db.withTransaction {
        inspect(actorId,id).copy(subPlot=note.trim()).also { records.updateInspection(it) }
    }
    suspend fun deleteInspection(actorId:String,id:String):InspectionEntity = db.withTransaction {
        inspect(actorId,id).also { records.deleteInspection(id) }
    }
}
