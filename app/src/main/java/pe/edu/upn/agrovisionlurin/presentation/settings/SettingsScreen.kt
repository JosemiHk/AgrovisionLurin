package pe.edu.upn.agrovisionlurin.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upn.agrovisionlurin.presentation.local.LocalMvpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    session: LocalMvpViewModel,
    onLogout: () -> Unit,
    onManageUsers: () -> Unit,
    onManagePlots: () -> Unit
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showPlotDialog by rememberSaveable { mutableStateOf(false) }
    val user by session.user.collectAsState()
    val plots by session.plots.collectAsState(initial = emptyList())
    val selectedId by session.selectedPlot.collectAsState()
    val plot = plots.firstOrNull { it.id == selectedId && it.active }
    val modelVersion by session.modelVersion.collectAsState()
    val modelAvailable by session.modelAvailable.collectAsState()
    val inspections by session.inspections.collectAsState(initial = emptyList())
    val lastInference = inspections.filter { it.state in listOf("VALIDO", "NO_CONCLUYENTE") }
        .maxByOrNull { it.createdAt }
    val busy by session.busy.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val horizontalPadding = ((maxWidth - 760.dp) / 2).coerceAtLeast(20.dp)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // Grupo 1: Lote y Parcela
            item(key = "group_plot") {
                SettingsGroup(title = "LOTE Y PARCELA") {
                    SettingsRow(
                        icon = Icons.Rounded.Place,
                        label = "Nombre de parcela",
                        value = plot?.let { "${it.code} · ${it.name}" } ?: "Sin seleccionar"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Rounded.Eco,
                        label = "Cultivo",
                        value = "Limón Sutil"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Rounded.LocationOn,
                        label = "Ubicación",
                        value = "Sullana, Piura"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsActionRow(Icons.Rounded.SwapHoriz, "Seleccionar parcela", "Para tus próximas inspecciones", !busy) { showPlotDialog = true }
                }
            }

            // Grupo 2: Diagnóstico Local (Edge AI)
            item(key = "group_ai") {
                SettingsGroup(title = "DIAGNÓSTICO LOCAL (EDGE AI)") {
                    SettingsRow(
                        icon = Icons.Rounded.Memory,
                        label = "Motor de inferencia",
                        value = "TensorFlow Lite"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Rounded.Psychology,
                        label = "Modelo",
                        value = modelVersion.ifBlank { "No disponible" }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRowWithBadge(
                        icon = Icons.Rounded.WifiOff,
                        label = "Estado",
                        value = if (modelAvailable) "Sin internet" else "Revisar modelo",
                        badgeText = if (modelAvailable) "Disponible" else "Ausente",
                        available = modelAvailable
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Rounded.Speed,
                        label = "Última inferencia",
                        value = lastInference?.let { "${it.latencyMs} ms" } ?: "Sin medición"
                    )
                }
            }

            // Los datos permanecen en el dispositivo; no se simula respaldo remoto.
            item(key = "group_data") {
                SettingsGroup(title = "DATOS LOCALES") {
                    SettingsRow(Icons.Rounded.Storage, "Almacenamiento", "SQLite Room")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(Icons.Rounded.History, "Inspecciones", inspections.size.toString())
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(Icons.Rounded.WifiOff, "Funcionamiento", "Sin conexión")
                }
            }

            if (user?.role == "ADMIN") {
                item(key = "group_management") {
                    SettingsGroup(title = "ADMINISTRACIÓN") {
                        SettingsActionRow(Icons.Rounded.People, "Usuarios", "Cuentas, roles y acceso local", !busy, onManageUsers)
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        SettingsActionRow(Icons.Rounded.Landscape, "Parcelas", "Organiza los registros de campo", !busy, onManagePlots)
                    }
                }
            }
            // Grupo 4: Sesión Activa
            item(key = "group_session") {
                SettingsGroup(title = "SESIÓN ACTIVA") {
                    SettingsRow(
                        icon = Icons.Rounded.AccountCircle,
                        label = "Usuario",
                        value = user?.username ?: "—"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Rounded.Badge,
                        label = "Rol",
                        value = if (user?.role == "ADMIN") "Administrador" else "Operador"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Botón cerrar sesión
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !busy) { showLogoutDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Logout,
                            contentDescription = null,
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Cerrar sesión",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBA1A1A)
                        )
                    }
                }
            }

            // Grupo 5: Acerca de la App
            item(key = "group_about") {
                SettingsGroup(title = "SISTEMA") {
                    SettingsRow(
                        icon = Icons.Rounded.Info,
                        label = "Versión",
                        value = "v1.0 (Tesis UPN 2026)"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Rounded.School,
                        label = "Institución",
                        value = "Universidad Privada del Norte"
                    )
                }
            }

            item(key = "footer_space") {
                Spacer(modifier = Modifier.height(16.dp))
            }
            }
        }
    }

    if (showPlotDialog) {
        AlertDialog(
            onDismissRequest = { showPlotDialog = false },
            title = { Text("Seleccionar parcela", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (plots.none { it.active }) {
                        item { Text("No hay parcelas activas. Un administrador puede crearlas en Configuración.") }
                    }
                    plots.filter { it.active }.forEach { candidate ->
                        item(key = candidate.id) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable(enabled = !busy) {
                                    session.selectedPlot.value = candidate.id
                                    showPlotDialog = false
                                }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = candidate.id == selectedId, onClick = null)
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(candidate.name, fontWeight = FontWeight.Medium)
                                    Text(candidate.code, color = Color(0xFF5E636E), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPlotDialog = false }) { Text("Cerrar") } },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Diálogo de confirmación de cierre de sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Logout,
                    contentDescription = null,
                    tint = Color(0xFFBA1A1A)
                )
            },
            title = {
                Text(
                    text = "Cerrar sesión",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Deseas cerrar la sesión de \"${user?.displayName ?: ""}\"? Tus datos locales se conservarán.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = Color(0xFFBA1A1A),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = Color(0xFF5E636E))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5E636E),
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE0E3EB))
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF5E636E),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1B1F)
            )
        }
        Text(
            text = value,
            modifier = Modifier.weight(0.9f, fill = false).padding(start = 12.dp),
            textAlign = TextAlign.End,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF5E636E)
        )
    }
}

@Composable
private fun SettingsActionRow(icon: ImageVector, label: String, subtitle: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF5E636E))
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color(0xFF5E636E), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsRowWithBadge(
    icon: ImageVector,
    label: String,
    value: String,
    badgeText: String,
    available: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF5E636E),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1B1F)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                color = Color(0xFF5E636E)
            )
            Surface(
                shape = CircleShape,
                color = if (available) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (available) Color(0xFF1B5E20) else Color(0xFF92400E),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

