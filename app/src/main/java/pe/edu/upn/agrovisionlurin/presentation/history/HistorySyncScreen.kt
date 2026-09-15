package pe.edu.upn.agrovisionlurin.presentation.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity
import pe.edu.upn.agrovisionlurin.presentation.components.LeafThumbnail
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySyncScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de Muestreos",
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
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Sync Banner
            item(key = "sync_banner") {
                SyncBanner(
                    pendingCount = uiState.pendingSyncCount,
                    isSyncing = isSyncing,
                    onSyncNow = { viewModel.syncPending() }
                )
            }

            // 2. Horizontal Filter Chips
            item(key = "filter_chips") {
                FilterChipsRow(
                    selectedFilter = uiState.selectedFilter,
                    totalCount = uiState.totalLocalCount,
                    pestCount = uiState.pestCount,
                    waterStressCount = uiState.waterStressCount,
                    healthyCount = uiState.healthyCount,
                    onFilterSelected = { viewModel.setFilter(it) }
                )
            }

            // 3. Inspections List or Empty State
            if (uiState.inspections.isEmpty()) {
                item(key = "empty_state") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E3EB))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SearchOff,
                                contentDescription = null,
                                tint = Color(0xFF5E636E),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Sin registros coincidentes",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F)
                            )
                            Text(
                                text = "No se encontraron muestras foliares para este filtro.",
                                fontSize = 13.sp,
                                color = Color(0xFF5E636E),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.inspections,
                    key = { it.id }
                ) { inspection ->
                    HistoryItemCard(
                        item = inspection,
                        onClick = { onNavigateToDetail(inspection.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncBanner(
    pendingCount: Int,
    isSyncing: Boolean,
    onSyncNow: () -> Unit
) {
    if (pendingCount > 0) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFEF3C7),
            border = BorderStroke(1.dp, Color(0xFFFDE68A))
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
                        imageVector = Icons.Rounded.CloudUpload,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "$pendingCount por sincronizar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "Guardado local en Room",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = Color(0xFFD97706)
                    )
                } else {
                    Surface(
                        modifier = Modifier.clickable(onClick = onSyncNow),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFD97706)
                    ) {
                        Text(
                            text = "Subir ahora",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFECFDF5),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudDone,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Todo sincronizado con el servidor",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF065F46)
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: HistoryFilter,
    totalCount: Int,
    pestCount: Int,
    waterStressCount: Int,
    healthyCount: Int,
    onFilterSelected: (HistoryFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChipItem(
            label = "Todos ($totalCount)",
            isSelected = selectedFilter == HistoryFilter.ALL,
            onClick = { onFilterSelected(HistoryFilter.ALL) }
        )
        FilterChipItem(
            label = "Plagas ($pestCount)",
            isSelected = selectedFilter == HistoryFilter.PESTS,
            onClick = { onFilterSelected(HistoryFilter.PESTS) }
        )
        FilterChipItem(
            label = "Déficit Hídrico ($waterStressCount)",
            isSelected = selectedFilter == HistoryFilter.WATER_STRESS,
            onClick = { onFilterSelected(HistoryFilter.WATER_STRESS) }
        )
        FilterChipItem(
            label = "Sanos ($healthyCount)",
            isSelected = selectedFilter == HistoryFilter.HEALTHY,
            onClick = { onFilterSelected(HistoryFilter.HEALTHY) }
        )
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF1B5E20) else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color(0xFF1B5E20) else Color(0xFFE0E3EB)
        )
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF5E636E),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: InspectionEntity,
    onClick: () -> Unit
) {
    val (statusLabel, statusColor, statusBg, statusBorder) = when (item.diagnosisClass) {
        1 -> Quadruple(
            "Trips",
            Color(0xFFBA1A1A),
            Color(0xFFFEE2E2),
            Color(0xFFFECACA)
        )
        2 -> Quadruple(
            "Déficit Hídrico",
            Color(0xFFD97706),
            Color(0xFFFEF3C7),
            Color(0xFFFDE68A)
        )
        else -> Quadruple(
            "Sano",
            Color(0xFF059669),
            Color(0xFFECFDF5),
            Color(0xFFA7F3D0)
        )
    }

    val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.createdAt))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E3EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LeafThumbnail(
                imagePath = item.imagePath,
                size = 56.dp,
                shapeRadius = 12.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.subPlot.ifBlank { "Surco sin definir" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = Color(0xFF5E636E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.latencyMs} ms · ${(item.confidence * 100).toInt()}% confianza",
                    fontSize = 11.sp,
                    color = Color(0xFF5E636E).copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status chip
            Surface(
                shape = CircleShape,
                color = statusBg,
                border = BorderStroke(1.dp, statusBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
