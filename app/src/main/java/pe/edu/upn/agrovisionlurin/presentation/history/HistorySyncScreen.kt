package pe.edu.upn.agrovisionlurin.presentation.history

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity
import pe.edu.upn.agrovisionlurin.presentation.theme.PrimaryGreen
import pe.edu.upn.agrovisionlurin.presentation.theme.Typography
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySyncScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AGROVISION-LURÍN",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1B5E20)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF1B5E20)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = "Condición solar",
                            tint = Color(0xFF1B5E20)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = Color(0xFFDDE2D5)
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 88.dp // Evita solapamiento con el NavigationBar
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Section
            item(key = "header_section") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Historial de Muestreos",
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF1C1B1F)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lurín – Temporada Actual",
                            style = Typography.bodyMedium.copy(fontSize = 15.sp),
                            color = Color(0xFF41493E)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE5E1E7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE2D5))
                        ) {
                            Text(
                                text = "${uiState.totalLocalCount} guardados localmente",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1C1B1F)
                            )
                        }
                    }
                }
            }

            // 2. Top Sync Status Banner (si hay registros pendientes)
            if (uiState.pendingSyncCount > 0) {
                item(key = "sync_banner") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFE65100), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFCC80)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudUpload,
                                        contentDescription = "Sincronización pendiente",
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${uiState.pendingSyncCount} registros pendientes de subir a la nube",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF531800)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Los diagnósticos están seguros en la memoria de este teléfono.",
                                        style = Typography.bodyMedium.copy(fontSize = 14.sp),
                                        color = Color(0xFF802A00)
                                    )
                                }
                            }

                            Button(
                                onClick = { /* Fase 4: Trigger WorkManager */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Sync,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sincronizar Ahora (Esperando Wi-Fi / 4G)",
                                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 3. Horizontal Filter Chips Row
            item(key = "filter_chips") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == HistoryFilter.ALL,
                            onClick = { viewModel.setFilter(HistoryFilter.ALL) },
                            label = {
                                Text(
                                    text = "Todos (${uiState.totalLocalCount})",
                                    fontWeight = FontWeight.Bold,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF1C1B1F)
                            ),
                            shape = CircleShape,
                            modifier = Modifier.height(44.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == HistoryFilter.PESTS,
                            onClick = { viewModel.setFilter(HistoryFilter.PESTS) },
                            label = {
                                Text(
                                    text = "Con Plaga (${uiState.pestCount})",
                                    fontWeight = FontWeight.Bold,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFC62828))
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFC62828),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF1C1B1F)
                            ),
                            shape = CircleShape,
                            modifier = Modifier.height(44.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == HistoryFilter.WATER_STRESS,
                            onClick = { viewModel.setFilter(HistoryFilter.WATER_STRESS) },
                            label = {
                                Text(
                                    text = "Estrés Hídrico (${uiState.waterStressCount})",
                                    fontWeight = FontWeight.Bold,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint = if (uiState.selectedFilter == HistoryFilter.WATER_STRESS) Color.White else Color(0xFFE65100),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE65100),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF1C1B1F)
                            ),
                            shape = CircleShape,
                            modifier = Modifier.height(44.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == HistoryFilter.HEALTHY,
                            onClick = { viewModel.setFilter(HistoryFilter.HEALTHY) },
                            label = {
                                Text(
                                    text = "Cultivo Sano (${uiState.healthyCount})",
                                    fontWeight = FontWeight.Bold,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = if (uiState.selectedFilter == HistoryFilter.HEALTHY) Color.White else Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF1C1B1F)
                            ),
                            shape = CircleShape,
                            modifier = Modifier.height(44.dp)
                        )
                    }
                }
            }

            // 4. Inspection Cards List
            if (uiState.inspections.isEmpty() && !uiState.isLoading) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SearchOff,
                                contentDescription = null,
                                tint = Color(0xFF717A6D),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No se encontraron muestreos en esta categoría",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1C1B1F)
                            )
                            Text(
                                text = "Prueba seleccionando otro filtro o realiza una nueva inspección.",
                                style = Typography.bodyMedium,
                                color = Color(0xFF41493E)
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.inspections,
                    key = { it.id }
                ) { inspection ->
                    InspectionCardItem(
                        inspection = inspection,
                        onViewDetail = { onNavigateToDetail(inspection.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun InspectionCardItem(
    inspection: InspectionEntity,
    onViewDetail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFFDDE2D5), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leaf Thumbnail
                InspectionThumbnail(
                    imagePath = inspection.imagePath,
                    modifier = Modifier.size(64.dp)
                )

                // Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = inspection.subPlot.ifBlank { "Surco 4 - Valle Bajo" },
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1C1B1F)
                        )
                        Text(
                            text = formatInspectionDate(inspection.createdAt),
                            style = Typography.labelMedium,
                            color = Color(0xFF717A6D)
                        )
                    }

                    // Status Pill Badge
                    StatusBadge(diagnosisClass = inspection.diagnosisClass)
                }
            }

            // Bottom Divider & Actions
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE5E1E7))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sync status indicator
                if (inspection.syncStatus == 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = "Guardado local",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Guardado Localmente",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE65100)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDone,
                            contentDescription = "Sincronizado",
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Sincronizado en Nube",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                TextButton(
                    onClick = onViewDetail,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Ver Ficha",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(diagnosisClass: Int) {
    val (bgColor, textColor, icon, label) = when (diagnosisClass) {
        1 -> Quadruple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            Icons.Rounded.BugReport,
            "Trips Detectado"
        )
        2 -> Quadruple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Icons.Rounded.WaterDrop,
            "Estrés Hídrico Leve"
        )
        else -> Quadruple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Icons.Rounded.CheckCircle,
            "Cultivo Sano"
        )
    }

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

@Composable
fun InspectionThumbnail(
    imagePath: String,
    modifier: Modifier = Modifier
) {
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = imagePath) {
        value = withContext(Dispatchers.IO) {
            try {
                val file = File(imagePath)
                if (file.exists() && file.length() > 0) {
                    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
                    var inSampleSize = 1
                    while ((boundsOptions.outHeight / inSampleSize) >= 128 && (boundsOptions.outWidth / inSampleSize) >= 128) {
                        inSampleSize *= 2
                    }
                    val decodeOptions = BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Muestra foliar",
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFDDE2D5), RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8F5E9))
                .border(1.dp, Color(0xFFDDE2D5), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

fun formatInspectionDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Fecha no disp."
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneDay = 24 * 60 * 60 * 1000L
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale("es", "PE"))

    return when {
        diff < oneDay && diff >= 0 -> "Hoy, ${timeFormat.format(Date(timestamp))}"
        diff < 2 * oneDay && diff >= 0 -> "Ayer, ${timeFormat.format(Date(timestamp))}"
        else -> dateFormat.format(Date(timestamp))
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
