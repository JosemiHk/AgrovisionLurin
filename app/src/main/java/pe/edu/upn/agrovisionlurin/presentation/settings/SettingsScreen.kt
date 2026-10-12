package pe.edu.upn.agrovisionlurin.presentation.settings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pe.edu.upn.agrovisionlurin.data.local.UserPreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefsManager = remember { UserPreferencesManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    var isCheckingAzure by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var azureStatusMessage by remember { mutableStateOf<String?>(null) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Grupo 1: Lote y Parcela
            item(key = "group_plot") {
                SettingsGroup(title = "LOTE Y PARCELA") {
                    SettingsRow(
                        icon = Icons.Rounded.Place,
                        label = "Nombre de parcela",
                        value = "Parcela 04 - LIMONORT"
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
                        value = "Sullana, Piura - LIMONORT-PERU"
                    )
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
                        value = "MobileNetV2 INT8"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRowWithBadge(
                        icon = Icons.Rounded.WifiOff,
                        label = "Estado",
                        value = "Listo sin internet",
                        badgeText = "Activo"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Rounded.Speed,
                        label = "Latencia estimada",
                        value = "~120 ms"
                    )
                }
            }

            // Grupo 3: Datos y Nube
            item(key = "group_data") {
                SettingsGroup(title = "DATOS Y NUBE") {
                    SettingsRow(
                        icon = Icons.Rounded.Storage,
                        label = "Almacenamiento local",
                        value = "SQLite Room"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Rounded.CloudQueue,
                        label = "Destino de nube",
                        value = "Azure Blob & CosmosDB"
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Acción: Comprobar conexión Azure
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isCheckingAzure) {
                                coroutineScope.launch {
                                    isCheckingAzure = true
                                    azureStatusMessage = null
                                    delay(1000)
                                    azureStatusMessage = "Dispositivo en modo local (sin internet disponible)."
                                    isCheckingAzure = false
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = null,
                                tint = Color(0xFF1B5E20),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Comprobar conexión Azure",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "Verificar conectividad de enlace",
                                    fontSize = 12.sp,
                                    color = Color(0xFF5E636E)
                                )
                            }
                        }

                        if (isCheckingAzure) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF1B5E20)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF5E636E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = azureStatusMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = azureStatusMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Grupo 4: Sesión Activa
            item(key = "group_session") {
                SettingsGroup(title = "SESIÓN ACTIVA") {
                    SettingsRow(
                        icon = Icons.Rounded.AccountCircle,
                        label = "Usuario",
                        value = prefsManager.loggedInUsername.ifBlank { "—" }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Rounded.Badge,
                        label = "Rol",
                        value = prefsManager.loggedInRole.ifBlank { "—" }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Botón cerrar sesión
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true }
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
                        value = "v1.0.0 (Tesis UPN 2026)"
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
                    text = "¿Deseas cerrar la sesión de \"${prefsManager.loggedInDisplayName}\"? Tus datos locales se conservarán.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        prefsManager.clearSession()
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF5E636E)
        )
    }
}

@Composable
private fun SettingsRowWithBadge(
    icon: ImageVector,
    label: String,
    value: String,
    badgeText: String
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
                color = Color(0xFFE8F5E9)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
