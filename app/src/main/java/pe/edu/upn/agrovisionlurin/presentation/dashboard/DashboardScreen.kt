package pe.edu.upn.agrovisionlurin.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
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
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity
import pe.edu.upn.agrovisionlurin.presentation.components.LeafThumbnail
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    session: LocalMvpViewModel
) {
    val inspections by session.inspections.collectAsStateWithLifecycle(initialValue = emptyList())
    val busy by session.busy.collectAsStateWithLifecycle()
    val message by session.message.collectAsStateWithLifecycle()
    val pendingCount = inspections.count { it.state in listOf("BORRADOR", "ERROR", "NO_CONCLUYENTE") }
    val recentInspections = inspections.sortedByDescending { it.createdAt }.take(5)
    val totalScans = inspections.size

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Header Row
            item(key = "header_row") {
                DashboardHeader(pendingCount = pendingCount)
            }

            if (busy) {
                item(key = "loading") { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1B5E20)) }
            }
            if (message.isNotBlank()) {
                item(key = "operation_message") { Text(message, color = Color(0xFF5E636E), fontSize = 13.sp) }
            }

            // 2. Hero Scan Card
            item(key = "hero_scan_card") {
                HeroScanCard(onClick = onNavigateToCamera)
            }

            // 3. Quick Metrics Bar
            item(key = "quick_metrics_bar") {
                QuickMetricsBar(
                    pendingCount = pendingCount,
                    totalScans = totalScans
                )
            }

            // 4. Section Header: Últimas Inspecciones
            item(key = "recent_inspections_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Últimas Inspecciones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    TextButton(
                        onClick = onNavigateToHistory,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Ver historial",
                            color = Color(0xFF1B5E20),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 5. Recent Inspections List
            if (recentInspections.isEmpty()) {
                item(key = "empty_inspections") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E3EB))
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = null,
                                tint = Color(0xFF1B5E20).copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Sin inspecciones registradas",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F)
                            )
                            Text(
                                text = "Presiona 'Escanear Cultivo' para registrar la primera muestra del lote.",
                                fontSize = 13.sp,
                                color = Color(0xFF5E636E),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = recentInspections,
                    key = { it.id }
                ) { inspection ->
                    RecentInspectionCard(
                        item = inspection,
                        onClick = { onNavigateToDetail(inspection.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(pendingCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "AgroVision",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = null,
                    tint = Color(0xFF1B5E20),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Cooperativa Agraria LIMONORT-PERU, Sullana",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5E636E)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        // Compact local status pill
        val isPending = pendingCount > 0
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isPending) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
            border = BorderStroke(
                width = 1.dp,
                color = if (isPending) Color(0xFFFDE68A) else Color(0xFFE2E8F0)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (isPending) Icons.Rounded.PendingActions else Icons.Rounded.PhoneAndroid,
                    contentDescription = "Guardado local",
                    tint = if (isPending) Color(0xFFD97706) else Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isPending) "Revisar · $pendingCount" else "Local",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPending) Color(0xFF92400E) else Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
private fun HeroScanCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1B5E20),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Escanear Cultivo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Analizar hojas de limón sutil en el lote",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Iniciar escaneo",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun QuickMetricsBar(
    pendingCount: Int,
    totalScans: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Metric 1: Pendientes
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE0E3EB))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PendingActions,
                    contentDescription = null,
                    tint = if (pendingCount > 0) Color(0xFFD97706) else Color(0xFF1B5E20),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = pendingCount.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = "Por revisar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5E636E)
                )
            }
        }

        // Metric 2: Total Muestreos
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE0E3EB))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircleOutline,
                    contentDescription = null,
                    tint = Color(0xFF1B5E20),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = totalScans.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = "Muestreos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5E636E)
                )
            }
        }
    }
}

@Composable
private fun RecentInspectionCard(
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

    val formattedDate = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(item.createdAt))

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
                size = 52.dp,
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
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Clean status chip
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
