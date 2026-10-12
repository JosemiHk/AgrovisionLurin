package pe.edu.upn.agrovisionlurin.presentation.management

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pe.edu.upn.agrovisionlurin.presentation.local.LocalMvpViewModel
import pe.edu.upn.agrovisionlurin.presentation.theme.*

/** Administrative CRUD uses the same local session and repository as the capture flow. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(session: LocalMvpViewModel, usersMode: Boolean, onBack: () -> Unit) {
    val currentUser by session.user.collectAsState()
    val users by session.users.collectAsState(initial = emptyList())
    val plots by session.plots.collectAsState(initial = emptyList())
    val busy by session.busy.collectAsState()
    val message by session.message.collectAsState()
    var editingId by remember(usersMode) { mutableStateOf<String?>(null) }
    var identifier by remember(usersMode) { mutableStateOf("") }
    var name by remember(usersMode) { mutableStateOf("") }
    var password by remember(usersMode) { mutableStateOf("") }
    var administrator by remember(usersMode) { mutableStateOf(false) }
    var active by remember(usersMode) { mutableStateOf(true) }
    var deleteTarget by remember(usersMode) { mutableStateOf<Pair<String, String>?>(null) }
    var requested by remember(usersMode) { mutableStateOf(false) }
    var pendingSave by remember(usersMode) { mutableStateOf(false) }
    var pendingDeleteId by remember(usersMode) { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val selfEditing = usersMode && editingId != null && editingId == currentUser?.id
    val expectedSave = if (usersMode) "Usuario guardado" else "Parcela guardada"
    val resetForm = {
        editingId = null; identifier = ""; name = ""; password = ""
        administrator = false; active = true
    }
    LaunchedEffect(message, busy) {
        if (pendingSave && !busy && message.isNotBlank()) {
            if (message == expectedSave) resetForm()
            pendingSave = false
        }
        if (pendingDeleteId != null && !busy && message.isNotBlank()) {
            if (message in listOf("Usuario eliminado", "Parcela eliminada") && editingId == pendingDeleteId) resetForm()
            pendingDeleteId = null
        }
    }

    Scaffold(
        containerColor = SurfaceNeutral,
        topBar = {
            TopAppBar(
                title = { Text(if (usersMode) "Usuarios" else "Parcelas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextCharcoal) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", tint = TextCharcoal) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { padding ->
        if (currentUser?.role != "ADMIN") {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Lock, null, tint = PrimaryGreen, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(16.dp))
                Text("Acceso de administrador", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextCharcoal)
                Text("Tu cuenta puede realizar inspecciones. La gestión de usuarios y parcelas requiere un administrador.", modifier = Modifier.padding(top = 8.dp), color = Color(0xFF5E636E))
                TextButton(onClick = onBack) { Text("Volver a Configuración", color = PrimaryGreen) }
            }
            return@Scaffold
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (usersMode) "Tu equipo de campo" else "Organiza tus parcelas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextCharcoal)
                    Text(if (usersMode) "Administra cuentas y permisos en este dispositivo." else "Asocia cada inspección a una parcela identificable.", fontSize = 14.sp, color = Color(0xFF5E636E))
                }
            }
            if (requested && message.isNotBlank()) {
                item {
                    val success = message in listOf("Usuario guardado", "Parcela guardada", "Usuario eliminado", "Parcela eliminada")
                    Surface(color = if (success) LightOptimalGreen else Color(0xFFFFF3E0), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().testTag("management_message")) {
                        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (success) Icons.Rounded.CheckCircle else Icons.Rounded.Info, null, tint = if (success) PrimaryGreen else Color(0xFF92400E), modifier = Modifier.size(20.dp))
                            Text(message, fontSize = 14.sp, color = if (success) PrimaryGreen else Color(0xFF92400E))
                        }
                    }
                }
            }
            item {
                ManagementCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (editingId == null) { if (usersMode) "Nuevo usuario" else "Nueva parcela" } else { if (usersMode) "Editar usuario" else "Editar parcela" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextCharcoal)
                        if (editingId != null) TextButton(enabled = !busy, onClick = { resetForm(); requested = false }) { Text("Cancelar", color = PrimaryGreen) }
                    }
                    ManagementField(identifier, { identifier = it }, if (usersMode) "Usuario" else "Código de parcela", !busy, "management_identifier")
                    ManagementField(name, { name = it }, if (usersMode) "Nombre completo" else "Nombre de parcela", !busy, "management_name")
                    if (usersMode) {
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            label = { Text(if (editingId == null) "Contraseña" else "Nueva contraseña") },
                            supportingText = { Text(if (editingId == null) "Mínimo 8 caracteres." else "Déjala vacía para conservar la actual.") },
                            visualTransformation = PasswordVisualTransformation(), singleLine = true,
                            enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("management_password"), shape = RoundedCornerShape(14.dp)
                        )
                        ManagementSwitch("Administrador", "Puede gestionar usuarios y parcelas", administrator, !busy && !selfEditing) { administrator = it }
                    }
                    ManagementSwitch(if (usersMode) "Usuario activo" else "Parcela activa", if (usersMode) "Habilita el acceso a la aplicación" else "Disponible para nuevas inspecciones", active, !busy && !selfEditing) { active = it }
                    if (selfEditing) Text("Tu propia cuenta debe permanecer activa y conservar su rol.", fontSize = 12.sp, color = Color(0xFF5E636E))
                    Button(
                        enabled = !busy && identifier.isNotBlank() && name.isNotBlank() && (!usersMode || editingId != null || password.length >= 8),
                        onClick = {
                            requested = true; pendingSave = true
                            if (usersMode) session.saveUser(editingId, identifier, name, password, if (administrator) "ADMIN" else "OPERADOR", active)
                            else session.savePlot(editingId, identifier, name, active)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("management_save"), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(if (usersMode) "Guardar usuario" else "Guardar parcela", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            item {
                Text(if (usersMode) "USUARIOS REGISTRADOS · ${users.count { it.role != "LEGADO" }}" else "PARCELAS REGISTRADAS · ${plots.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E636E), letterSpacing = 0.6.sp, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
            }
            if (usersMode) {
                if (users.none { it.role != "LEGADO" }) item { EmptyManagementCard("Aún no hay usuarios registrados.") }
                items(users.filter { it.role != "LEGADO" }, key = { it.id }) { account ->
                    RecordCard(account.displayName, "@${account.username} · ${if (account.role == "ADMIN") "Administrador" else "Operador"}", account.active, !busy, account.id != currentUser?.id,
                        onEdit = {
                            editingId = account.id; identifier = account.username; name = account.displayName
                            administrator = account.role == "ADMIN"; active = account.active; password = ""; requested = false
                            scope.launch { listState.animateScrollToItem(0) }
                        }, onDelete = { deleteTarget = account.id to account.username })
                }
            } else {
                if (plots.isEmpty()) item { EmptyManagementCard("Crea tu primera parcela para iniciar las inspecciones.") }
                items(plots, key = { it.id }) { plot ->
                    RecordCard(plot.name, plot.code, plot.active, !busy, true,
                        onEdit = {
                            editingId = plot.id; identifier = plot.code; name = plot.name; active = plot.active; requested = false
                            scope.launch { listState.animateScrollToItem(0) }
                        }, onDelete = { deleteTarget = plot.id to plot.code })
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!busy) deleteTarget = null },
            icon = { Icon(Icons.Rounded.DeleteOutline, null, tint = CriticalRed) },
            title = { Text("Eliminar ${target.second}", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción no se puede deshacer. Si tiene inspecciones asociadas, la eliminación será rechazada; puedes desactivarlo desde Editar.") },
            confirmButton = { TextButton(enabled = !busy, onClick = { requested = true; pendingSave = false; pendingDeleteId = target.first; if (usersMode) session.deleteUser(target.first) else session.deletePlot(target.first); deleteTarget = null }) { Text("Eliminar", color = CriticalRed) } },
            dismissButton = { TextButton(enabled = !busy, onClick = { deleteTarget = null }) { Text("Cancelar", color = Color(0xFF5E636E)) } },
            containerColor = Color.White, shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ManagementCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFE0E3EB))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun ManagementField(value: String, onChange: (String) -> Unit, label: String, enabled: Boolean, tag: String) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, enabled = enabled,
        singleLine = true, modifier = Modifier.fillMaxWidth().testTag(tag), shape = RoundedCornerShape(14.dp))
}

@Composable
private fun ManagementSwitch(title: String, subtitle: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextCharcoal)
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF5E636E))
        }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled, colors = SwitchDefaults.colors(checkedTrackColor = PrimaryGreen))
    }
}

@Composable
private fun RecordCard(title: String, subtitle: String, active: Boolean, enabled: Boolean, canDelete: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    ManagementCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = LightOptimalGreen, shape = CircleShape) { Icon(Icons.Rounded.FolderOpen, null, tint = PrimaryGreen, modifier = Modifier.padding(10.dp).size(22.dp)) }
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextCharcoal)
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF5E636E))
            }
            Surface(color = if (active) LightOptimalGreen else Color(0xFFF1F5F9), shape = CircleShape) {
                Text(if (active) "Activo" else "Inactivo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (active) PrimaryGreen else Color(0xFF5E636E), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        HorizontalDivider(color = Color(0xFFF1F5F9))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(enabled = enabled, onClick = onEdit) { Icon(Icons.Rounded.Edit, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Editar") }
            TextButton(enabled = enabled && canDelete, onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(16.dp), tint = if (canDelete) CriticalRed else Color.Gray); Spacer(Modifier.width(6.dp)); Text("Eliminar", color = if (canDelete) CriticalRed else Color.Gray) }
        }
    }
}

@Composable
private fun EmptyManagementCard(message: String) {
    ManagementCard {
        Text(message, fontSize = 14.sp, color = Color(0xFF5E636E), modifier = Modifier.padding(vertical = 12.dp))
    }
}
