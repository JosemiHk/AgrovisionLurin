package pe.edu.upn.agrovisionlurin.presentation.local

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upn.agrovisionlurin.data.local.*
import pe.edu.upn.agrovisionlurin.presentation.components.LeafThumbnail
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun LocalMvpScreen(vm: LocalMvpViewModel = viewModel()) {
    val context=LocalContext.current
    val user by vm.user.collectAsStateWithLifecycle()
    val setup by vm.setup.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val detail by vm.detail.collectAsStateWithLifecycle()
    val users by vm.users.collectAsStateWithLifecycle(emptyList())
    val plots by vm.plots.collectAsStateWithLifecycle(emptyList())
    val inspections by vm.inspections.collectAsStateWithLifecycle(emptyList())
    val selectedPlot by vm.selectedPlot.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf("Inicio") }
    var launchError by remember { mutableStateOf("") }
    val capture=rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { vm.captureFinished(it) }
    val gallery=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { vm.galleryFinished(it) }
    val launchCapture: () -> Unit = {
        vm.createCapture { path ->
            try { capture.launch(FileProvider.getUriForFile(context,context.packageName+".files",File(path))) }
            catch(error: Exception) { launchError="No se pudo abrir la cámara: ${error.message}"; vm.captureFinished(false) }
        }
    }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if(it) launchCapture() else launchError="Permiso de cámara denegado. Puedes volver a solicitarlo."
    }
    BackHandler(enabled=detail!=null || tab!="Inicio") { if(detail!=null) vm.closeDetail() else tab="Inicio" }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("AgroVision Sullana",style=MaterialTheme.typography.headlineMedium)
        Text("Limón sutil · LIMONORT-PERU · almacenamiento local")
        if(busy || !ready) LinearProgressIndicator(Modifier.fillMaxWidth())
        if(message.isNotEmpty()) Text(message,color=MaterialTheme.colorScheme.primary)
        if(launchError.isNotEmpty()) Text(launchError,color=MaterialTheme.colorScheme.error)
        if (ready) {
            val currentUser = user
            if (currentUser == null) {
                LoginForm(setup, busy) { username, password, name -> vm.login(username, password, name) }
            } else {
                Text("${currentUser.displayName} · ${currentUser.role}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(enabled = !busy, onClick = { vm.closeDetail(); tab = "Inicio" }) { Text("Inicio") }
                    TextButton(enabled = !busy, onClick = { vm.closeDetail(); tab = "Historial" }) { Text("Historial") }
                    TextButton(enabled = !busy, onClick = { vm.logout(); tab = "Inicio" }) { Text("Salir") }
                }
                if (currentUser.role == "ADMIN") Row {
                    TextButton(enabled = !busy, onClick = { vm.closeDetail(); tab = "Parcelas" }) { Text("Parcelas") }
                    TextButton(enabled = !busy, onClick = { vm.closeDetail(); tab = "Usuarios" }) { Text("Usuarios") }
                }
                val record = detail
                if (record != null) {
                    InspectionDetail(
                        record = record,
                        busy = busy,
                        vm = vm,
                        onExport = {
                            vm.export(record.id) { file ->
                                val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
                                try {
                                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        clipData = android.content.ClipData.newRawUri("Inspección CSV", uri)
                                    }, "Compartir inspección"))
                                } catch (error: Exception) {
                                    launchError = "No hay aplicación para compartir: ${error.message}"
                                }
                            }
                        }
                    )
                } else when (tab) {
                    "Inicio" -> {
                        Text("Fotografía una sola hoja, enfocada y con luz uniforme. El resultado apoya la revisión agronómica; no mide severidad.")
                        Text("Selecciona una parcela antes de capturar:")
                        plots.filter { it.active }.forEach { plot ->
                            Row {
                                RadioButton(selected = selectedPlot == plot.id, onClick = { vm.selectedPlot.value = plot.id }, enabled = !busy)
                                Text("${plot.code} · ${plot.name}", Modifier.padding(top = 12.dp))
                            }
                        }
                        if (plots.none { it.active }) Text("El administrador debe crear una parcela activa.")
                        Button(enabled = !busy && selectedPlot.isNotEmpty(), onClick = {
                            launchError = ""
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) launchCapture()
                            else permission.launch(Manifest.permission.CAMERA)
                        }) { Text("Capturar hoja") }
                        OutlinedButton(enabled = !busy && selectedPlot.isNotEmpty(), onClick = {
                            vm.createCapture {
                                try { gallery.launch("image/*") }
                                catch (error: Exception) { launchError = "No se pudo abrir la galería: ${error.message}"; vm.captureFinished(false) }
                            }
                        }) { Text("Importar fotografía") }
                        Text("No se realiza sincronización remota. Comparte el CSV desde el detalle para conservar una copia externa.")
                    }
                    "Historial" -> {
                        val visible = inspections.filter { currentUser.role == "ADMIN" || it.userId == currentUser.id }
                        Text("${visible.size} inspecciones locales")
                        if (visible.isEmpty()) Text("Sin inspecciones registradas")
                        visible.forEach { inspection ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("${inspection.plotCodeSnapshot} · ${inspection.state}")
                                    Text(inspection.classLabel.ifBlank { "Captura pendiente" })
                                    Text(DateFormat.getDateTimeInstance().format(Date(inspection.createdAt)))
                                    TextButton(enabled = !busy, onClick = { vm.open(inspection.id) }) { Text("Abrir ${inspection.id.take(8)}") }
                                }
                            }
                        }
                    }
                    "Usuarios" -> if (currentUser.role == "ADMIN") UsersEditor(users, busy, vm)
                    "Parcelas" -> if (currentUser.role == "ADMIN") PlotsEditor(plots, busy, vm)
                }
            }
        }
    }
}

@Composable private fun LoginForm(setup: Boolean, busy: Boolean, onSubmit: (String,String,String)->Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    Text(if(setup) "Configura el primer administrador local" else "Iniciar sesión")
    OutlinedTextField(username,{username=it},label={Text("Usuario")},singleLine=true)
    if(setup) OutlinedTextField(name,{name=it},label={Text("Nombre")},singleLine=true)
    OutlinedTextField(password,{password=it},label={Text("Contraseña")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
    Button(enabled=!busy,onClick={onSubmit(username,password,name);password=""}) { Text(if(setup) "Crear administrador" else "Ingresar") }
    Text("Autenticación local. No hay contraseñas de demostración ni conexión a servidor.")
}

@Composable private fun InspectionDetail(record: InspectionEntity, busy: Boolean, vm: LocalMvpViewModel, onExport:()->Unit) {
    var note by remember(record.id) { mutableStateOf(record.subPlot) }
    var confirmDelete by remember(record.id) { mutableStateOf(false) }
    Text("Inspección ${record.id}",style=MaterialTheme.typography.titleLarge)
    LeafThumbnail(record.imagePath,size=180.dp)
    Text("${record.plotCodeSnapshot} · ${record.userNameSnapshot}")
    Text("Estado: ${record.state}")
    Text(record.classLabel.ifBlank { "Captura pendiente" })
    if (record.awaitingLemonOrigin) {
        Button(enabled = !busy, onClick = { vm.confirmLemonOrigin(record.id) }) {
            Text("Sí, es de limonero. Analizar")
        }
    }
    if(record.state=="VALIDO" || record.state=="NO_CONCLUYENTE") {
        if (record.diagnosisClass >= 0) Text("Puntuación: ${"%.3f".format(java.util.Locale.ROOT,record.confidence)}")
        Text("Tiempo de procesamiento: ${record.latencyMs} ms")
        Text("Modelo: ${record.modelVersion}")
    }
    if(record.state=="LEGADO") Text("Registro histórico del dominio anterior. Clase ordinal ${record.diagnosisClass}; severidad histórica ${record.severityLevel}. No representa diagnóstico de limón.")
    if(record.errorMessage.isNotEmpty()) Text(record.errorMessage,color=MaterialTheme.colorScheme.error)
    if(record.recommendationsJson.isNotEmpty()) {
        val recommendations=remember(record.recommendationsJson) {
            try { val values=org.json.JSONArray(record.recommendationsJson);(0 until values.length()).map { values.optString(it) } }
            catch(ignored:Exception) { listOf(record.recommendationsJson) }
        }
        recommendations.forEach { Text("• $it") }
    }
    OutlinedTextField(note,{note=it},label={Text("Nota de ubicación / subparcela")})
    Button(enabled=!busy,onClick={vm.updateMetadata(record.id,note)}) { Text("Actualizar nota") }
    if(record.state in listOf("BORRADOR","ERROR")) Button(enabled=!busy,onClick={vm.retry(record.id)}) { Text("Reintentar análisis") }
    Button(enabled=!busy,onClick=onExport) { Text("Compartir CSV") }
    TextButton(enabled=!busy,onClick={confirmDelete=true}) { Text("Eliminar inspección") }
    if(confirmDelete) AlertDialog(onDismissRequest={confirmDelete=false},title={Text("¿Eliminar inspección y fotografía?")},
        text={Text("La eliminación local no se puede deshacer.")},confirmButton={TextButton(onClick={vm.deleteInspection(record.id);confirmDelete=false}) { Text("Eliminar") }},
        dismissButton={TextButton(onClick={confirmDelete=false}) { Text("Cancelar") }})
}

@Composable private fun UsersEditor(users: List<LocalUser>, busy:Boolean, vm:LocalMvpViewModel) {
    var id by remember { mutableStateOf<String?>(null) }; var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    var admin by remember { mutableStateOf(false) }; var active by remember { mutableStateOf(true) }
    var delete by remember { mutableStateOf<LocalUser?>(null) }
    Text(if(id==null) "Crear usuario" else "Editar usuario")
    OutlinedTextField(username,{username=it},label={Text("Usuario")},singleLine=true)
    OutlinedTextField(name,{name=it},label={Text("Nombre")},singleLine=true)
    OutlinedTextField(password,{password=it},label={Text(if(id==null) "Contraseña" else "Nueva contraseña (vacía conserva actual)")},visualTransformation=PasswordVisualTransformation())
    Row { Checkbox(admin,{admin=it});Text("Administrador",Modifier.padding(top=12.dp)) }
    Row { Checkbox(active,{active=it});Text("Activo",Modifier.padding(top=12.dp)) }
    Button(enabled=!busy,onClick={vm.saveUser(id,username,name,password,if(admin) "ADMIN" else "OPERADOR",active);password=""}) { Text("Guardar usuario") }
    TextButton(onClick={id=null;username="";name="";password="";admin=false;active=true}) { Text("Nuevo") }
    users.filter { it.role!="LEGADO" }.forEach { value -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
        Text("${value.username} · ${value.displayName} · ${value.role} · ${if(value.active) "activo" else "inactivo"}")
        Row {
            TextButton(enabled=!busy,onClick={id=value.id;username=value.username;name=value.displayName;admin=value.role=="ADMIN";active=value.active;password=""}) { Text("Editar") }
            TextButton(enabled=!busy,onClick={delete=value}) { Text("Eliminar") }
        }
    } } }
    delete?.let { value -> AlertDialog(onDismissRequest={delete=null},title={Text("Eliminar ${value.username}")},text={Text("Si tiene inspecciones, desactívalo; la eliminación será rechazada.")},
        confirmButton={TextButton(onClick={vm.deleteUser(value.id);delete=null}) { Text("Eliminar") }},dismissButton={TextButton(onClick={delete=null}) { Text("Cancelar") }}) }
}

@Composable private fun PlotsEditor(plots: List<LocalPlot>,busy:Boolean,vm:LocalMvpViewModel) {
    var id by remember { mutableStateOf<String?>(null) }; var code by remember { mutableStateOf("") }; var name by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }; var delete by remember { mutableStateOf<LocalPlot?>(null) }
    Text(if(id==null) "Crear parcela" else "Editar parcela")
    OutlinedTextField(code,{code=it},label={Text("Código")},singleLine=true)
    OutlinedTextField(name,{name=it},label={Text("Nombre")},singleLine=true)
    Row { Checkbox(active,{active=it});Text("Activa",Modifier.padding(top=12.dp)) }
    Button(enabled=!busy,onClick={vm.savePlot(id,code,name,active)}) { Text("Guardar parcela") }
    TextButton(onClick={id=null;code="";name="";active=true}) { Text("Nueva") }
    plots.forEach { value -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
        Text("${value.code} · ${value.name} · ${if(value.active) "activa" else "inactiva"}")
        Row {
            TextButton(enabled=!busy,onClick={id=value.id;code=value.code;name=value.name;active=value.active}) { Text("Editar") }
            TextButton(enabled=!busy,onClick={delete=value}) { Text("Eliminar") }
        }
    } } }
    delete?.let { value -> AlertDialog(onDismissRequest={delete=null},title={Text("Eliminar ${value.code}")},text={Text("Si tiene inspecciones, desactívala; la eliminación será rechazada.")},
        confirmButton={TextButton(onClick={vm.deletePlot(value.id);delete=null}) { Text("Eliminar") }},dismissButton={TextButton(onClick={delete=null}) { Text("Cancelar") }}) }
}
