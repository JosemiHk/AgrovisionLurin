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
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PendingActions
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
import pe.edu.upn.agrovisionlurin.presentation.local.LocalMvpViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity
import pe.edu.upn.agrovisionlurin.presentation.components.LeafThumbnail
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySyncScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    session: LocalMvpViewModel
) {
    val inspections by session.inspections.collectAsStateWithLifecycle(initialValue = emptyList())
    val busy by session.busy.collectAsStateWithLifecycle()
    val message by session.message.collectAsStateWithLifecycle()
    var selectedFilter by rememberSaveable { mutableStateOf("Todos") }
    val pendingCount = inspections.count { it.state in listOf("BORRADOR", "ERROR", "NO_CONCLUYENTE") }
    val filteredInspections = inspections.filter {
        when (selectedFilter) {
            "Minador" -> it.state == "VALIDO" && it.classCode == "MINADOR_COMPATIBLE"
            "Sin síntomas" -> it.state == "VALIDO" && it.classCode == "SIN_SINTOMAS_VISIBLES"
            "Revisar" -> it.state in listOf("BORRADOR", "ERROR", "NO_CONCLUYENTE")
            "Legado" -> it.state == "LEGADO"
            else -> true
        }
    }.sortedByDescending { it.createdAt }

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
                LocalBanner(pendingCount = pendingCount)
            }
            if (busy) {
                item(key = "loading") { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1B5E20)) }
            }
            if (message.isNotBlank()) {
                item(key = "operation_message") { Text(message, color = Color(0xFF5E636E), fontSize = 13.sp) }
            }

            // 2. Horizontal Filter Chips
            item(key = "filter_chips") {
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    inspections = inspections,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            // 3. Inspections List or Empty State
            if (filteredInspections.isEmpty()) {
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
                    items = filteredInspections,
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
private fun LocalBanner(pendingCount: Int) {
    val pending = pendingCount > 0
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (pending) Color(0xFFFEF3C7) else Color(0xFFECFDF5),
        border = BorderStroke(1.dp, if (pending) Color(0xFFFDE68A) else Color(0xFFA7F3D0))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (pending) Icons.Rounded.PendingActions else Icons.Rounded.PhoneAndroid,
                contentDescription = null,
                tint = if (pending) Color(0xFFD97706) else Color(0xFF059669),
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (pending) "$pendingCount por revisar" else "Inspecciones guardadas en el dispositivo",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (pending) Color(0xFF92400E) else Color(0xFF065F46)
                )
                Text(
                    text = "Historial local · disponible sin internet",
                    fontSize = 12.sp,
                    color = if (pending) Color(0xFFB45309) else Color(0xFF065F46)
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: String,
    inspections: List<InspectionEntity>,
    onFilterSelected: (String) -> Unit
) {
    val counts = linkedMapOf(
        "Todos" to inspections.size,
        "Minador" to inspections.count { it.state == "VALIDO" && it.classCode == "MINADOR_COMPATIBLE" },
        "Sin síntomas" to inspections.count { it.state == "VALIDO" && it.classCode == "SIN_SINTOMAS_VISIBLES" },
        "Revisar" to inspections.count { it.state in listOf("BORRADOR", "ERROR", "NO_CONCLUYENTE") },
        "Legado" to inspections.count { it.state == "LEGADO" }
    )
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        counts.forEach { (label, count) ->
            FilterChipItem("$label ($count)", selectedFilter == label) { onFilterSelected(label) }
        }
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
    val label = when (item.state) {
        "LEGADO" -> "Legado"
        "BORRADOR" -> "Borrador"
        "ERROR" -> "Error"
        "NO_CONCLUYENTE" -> "No concluyente"
        else -> item.classLabel.ifBlank { "Sin etiqueta" }
    }
    val (statusLabel, statusColor, statusBg, statusBorder) = when {
        item.state == "LEGADO" -> Quadruple(label, Color(0xFF64748B), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
        item.state != "VALIDO" -> Quadruple(label, Color(0xFFD97706), Color(0xFFFEF3C7), Color(0xFFFDE68A))
        item.classCode == "MINADOR_COMPATIBLE" -> Quadruple(label, Color(0xFFBA1A1A), Color(0xFFFEE2E2), Color(0xFFFECACA))
        else -> Quadruple(label, Color(0xFF059669), Color(0xFFECFDF5), Color(0xFFA7F3D0))
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
                    text = item.plotCodeSnapshot.ifBlank { "Parcela" } + item.subPlot.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty(),
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
                    text = when (item.state) {
                        "VALIDO" -> "${item.latencyMs} ms · ${(item.confidence * 100).toInt()}% puntuación"
                        "NO_CONCLUYENTE" -> item.classLabel.ifBlank { "Resultado no concluyente" }
                        "ERROR" -> item.errorMessage.ifBlank { "Análisis no disponible" }
                        "LEGADO" -> "Registro histórico · resultado original conservado"
                        else -> "Captura pendiente de análisis"
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                        modifier = Modifier.widthIn(max = 120.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
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
