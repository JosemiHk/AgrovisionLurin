package pe.edu.upn.agrovisionlurin.presentation.result

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upn.agrovisionlurin.data.local.InspectionEntity
import pe.edu.upn.agrovisionlurin.presentation.components.LeafThumbnail
import pe.edu.upn.agrovisionlurin.presentation.local.LocalMvpViewModel
import pe.edu.upn.agrovisionlurin.presentation.theme.PrimaryGreen
import pe.edu.upn.agrovisionlurin.presentation.theme.Typography
import org.json.JSONArray

@Composable
fun DiagnosticResultScreen(resultId: String, onBack: () -> Unit, onNavigateToCamera: () -> Unit, session: LocalMvpViewModel = viewModel()) {
    val detail by session.detail.collectAsStateWithLifecycle()
    val busy by session.busy.collectAsStateWithLifecycle()
    val message by session.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var shareError by remember(resultId) { mutableStateOf("") }
    var editingNote by rememberSaveable(resultId) { mutableStateOf(false) }
    var confirmDelete by rememberSaveable(resultId) { mutableStateOf(false) }
    var deleting by remember(resultId) { mutableStateOf(false) }
    var note by rememberSaveable(resultId) { mutableStateOf("") }
    LaunchedEffect(resultId) { session.open(resultId) }
    val record = detail?.takeIf { it.id == resultId }
    LaunchedEffect(record?.id, record?.subPlot) {
        if (!editingNote) {
            record?.let { note = it.subPlot }
        }
    }
    LaunchedEffect(deleting, busy, detail) {
        if (deleting && !busy) {
            if (detail?.id != resultId) onBack()
            deleting = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val useTwoColumns = maxWidth >= 680.dp
            val isCompactHeight = maxHeight < 520.dp
            val contentMaxWidth = if (useTwoColumns) 1080.dp else 640.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .align(Alignment.TopCenter)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver") }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Resultado del Diagnóstico",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (useTwoColumns && record != null) {
                        Spacer(Modifier.width(12.dp))
                        LatencyPill(record = record)
                    }
                }
                Spacer(Modifier.height(if (isCompactHeight) 8.dp else 14.dp))

                if (record == null) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (busy) CircularProgressIndicator(color = PrimaryGreen)
                        else Text(message.ifBlank { "No se encontró esta inspección." }, style = Typography.bodyLarge)
                    }
                } else {
                    val recommendations = remember(record.recommendationsJson) {
                        try {
                            val values = JSONArray(record.recommendationsJson)
                            (0 until values.length()).map { index ->
                                val entry = values.opt(index)
                                if (entry is org.json.JSONObject) entry.optString("description", entry.toString()) else entry.toString()
                            }
                        } catch (_: Exception) { if (record.recommendationsJson.isBlank()) emptyList() else listOf(record.recommendationsJson) }
                    }
                    val onEditNoteClick = {
                        note = record.subPlot
                        editingNote = true
                    }
                    val onExportCsvClick = {
                        session.export(record.id) { file ->
                            try {
                                val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
                                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"; putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    clipData = ClipData.newRawUri("Inspección CSV", uri)
                                }, "Compartir inspección"))
                            } catch (_: Exception) { shareError = "No se pudo compartir el CSV. Intenta de nuevo." }
                        }
                    }

                    if (useTwoColumns) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Columna izquierda: veredicto, imagen ampliada, metadatos y acciones principales
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                item { VerdictCard(record = record) }
                                item {
                                    InspectionMetadataSection(
                                        record = record,
                                        thumbnailSize = 148.dp,
                                        shapeRadius = 16.dp
                                    )
                                }
                                item {
                                    Spacer(Modifier.height(4.dp))
                                    PrimaryResultActions(
                                        busy = busy,
                                        onBack = onBack,
                                        onNavigateToCamera = onNavigateToCamera
                                    )
                                }
                            }

                            // Columna derecha: recomendaciones agronómicas y acciones secundarias
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                item { RecommendationsSection(recommendations = recommendations) }
                                item {
                                    SecondaryResultActions(
                                        record = record,
                                        busy = busy,
                                        message = message,
                                        shareError = shareError,
                                        onRetry = { session.retry(record.id) },
                                        onEditNote = onEditNoteClick,
                                        onExportCsv = onExportCsvClick,
                                        onDelete = { confirmDelete = true }
                                    )
                                }
                            }
                        }
                    } else {
                        if (!isCompactHeight) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                LatencyPill(record = record)
                            }
                            Spacer(Modifier.height(14.dp))
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            if (isCompactHeight) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LatencyPill(record = record)
                                    }
                                }
                            }
                            item { VerdictCard(record = record) }
                            item {
                                InspectionMetadataSection(
                                    record = record,
                                    thumbnailSize = 88.dp,
                                    shapeRadius = 12.dp
                                )
                            }
                            item { RecommendationsSection(recommendations = recommendations) }
                            item {
                                SecondaryResultActions(
                                    record = record,
                                    busy = busy,
                                    message = message,
                                    shareError = shareError,
                                    onRetry = { session.retry(record.id) },
                                    onEditNote = onEditNoteClick,
                                    onExportCsv = onExportCsvClick,
                                    onDelete = { confirmDelete = true }
                                )
                            }
                            if (isCompactHeight) {
                                item {
                                    Spacer(Modifier.height(4.dp))
                                    PrimaryResultActions(
                                        busy = busy,
                                        onBack = onBack,
                                        onNavigateToCamera = onNavigateToCamera
                                    )
                                }
                            }
                        }

                        if (!isCompactHeight) {
                            Spacer(Modifier.height(12.dp))
                            PrimaryResultActions(
                                busy = busy,
                                onBack = onBack,
                                onNavigateToCamera = onNavigateToCamera
                            )
                        }
                    }

                    if (editingNote) AlertDialog(
                        onDismissRequest = { editingNote = false },
                        title = { Text("Nota de ubicación / subparcela") },
                        text = { OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nota") }) },
                        confirmButton = {
                            TextButton(enabled = !busy, onClick = { session.updateMetadata(record.id, note); editingNote = false }) {
                                Text("Guardar")
                            }
                        },
                        dismissButton = { TextButton(onClick = { editingNote = false }) { Text("Cancelar") } }
                    )
                    if (confirmDelete) AlertDialog(
                        onDismissRequest = { confirmDelete = false },
                        title = { Text("¿Eliminar esta inspección?") },
                        text = { Text("Se eliminarán el registro y su fotografía local. Esta acción no se puede deshacer.") },
                        confirmButton = {
                            TextButton(enabled = !busy, onClick = { deleting = true; confirmDelete = false; session.deleteInspection(record.id) }) {
                                Text("Eliminar")
                            }
                        },
                        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
                    )
                }
            }
        }
    }
}

@Composable
private fun LatencyPill(record: InspectionEntity) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFEEEEEE)) {
        Text(
            text = if (record.modelVersion.isNotBlank()) "Analizado en ${record.latencyMs / 1000.0}s · Inferencia local" else "Registro conservado en este dispositivo",
            style = Typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.DarkGray
        )
    }
}

@Composable
private fun VerdictCard(record: InspectionEntity) {
    val statusColor = when {
        record.state == "ERROR" -> Color(0xFFC62828)
        record.state in listOf("BORRADOR", "NO_CONCLUYENTE", "LEGADO") -> Color(0xFFE65100)
        record.classCode == "MINADOR_COMPATIBLE" -> Color(0xFFC62828)
        else -> Color(0xFF2E7D32)
    }
    val title = when (record.state) {
        "ERROR" -> "Análisis no completado"
        "BORRADOR" -> "Fotografía pendiente de análisis"
        "NO_CONCLUYENTE" -> "Resultado no concluyente"
        "LEGADO" -> "Registro histórico"
        else -> record.classLabel.ifBlank { "Resultado sin etiqueta" }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, statusColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when {
                    record.state == "ERROR" -> Icons.Rounded.ErrorOutline
                    record.state != "VALIDO" -> Icons.Rounded.Info
                    record.classCode == "MINADOR_COMPATIBLE" -> Icons.Rounded.BugReport
                    else -> Icons.Rounded.CheckCircle
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(title, style = Typography.titleLarge, color = statusColor, fontWeight = FontWeight.Bold)
            if (record.state == "VALIDO") Text("Puntuación del modelo: ${(record.confidence * 100).toInt()}%", style = Typography.bodyMedium)
            if (record.state == "NO_CONCLUYENTE") Text("No se emitió una clasificación. Revisa la muestra con un técnico.", style = Typography.bodyMedium)
            if (record.state == "LEGADO") Text("Dominio anterior: clase ${record.diagnosisClass}. No representa un diagnóstico de limón.", style = Typography.bodyMedium)
            if (record.errorMessage.isNotBlank()) Text(record.errorMessage, style = Typography.bodyMedium)
        }
    }
}

@Composable
private fun InspectionMetadataSection(
    record: InspectionEntity,
    thumbnailSize: Dp,
    shapeRadius: Dp
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        LeafThumbnail(record.imagePath, size = thumbnailSize, shapeRadius = shapeRadius)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Parcela ${record.plotCodeSnapshot.ifBlank { record.plotId }}", style = Typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(record.userNameSnapshot, style = Typography.bodyMedium)
            if (record.modelVersion.isNotBlank()) Text("Modelo: ${record.modelVersion}", style = Typography.labelSmall)
            if (record.subPlot.isNotBlank()) Text(record.subPlot, style = Typography.bodyMedium)
        }
    }
}

@Composable
private fun RecommendationsSection(recommendations: List<String>) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Recomendaciones Agronómicas", style = Typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        if (recommendations.isEmpty()) {
            Text("Sin recomendaciones automáticas para este estado.", style = Typography.bodyMedium)
        }
        recommendations.forEach { recommendation ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Search, null, tint = PrimaryGreen)
                    Spacer(Modifier.width(12.dp))
                    Text(recommendation, style = Typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SecondaryResultActions(
    record: InspectionEntity,
    busy: Boolean,
    message: String,
    shareError: String,
    onRetry: () -> Unit,
    onEditNote: () -> Unit,
    onExportCsv: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (record.state in listOf("BORRADOR", "ERROR")) {
            OutlinedButton(enabled = !busy, onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text("Reintentar análisis", color = PrimaryGreen)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = !busy, onClick = onEditNote, modifier = Modifier.weight(1f)) {
                Text("Editar nota")
            }
            OutlinedButton(enabled = !busy, onClick = onExportCsv, modifier = Modifier.weight(1f)) {
                Icon(Icons.Rounded.Share, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("CSV")
            }
        }
        TextButton(enabled = !busy, onClick = onDelete) {
            Text("Eliminar inspección", color = Color(0xFFC62828))
        }
        if (message.isNotBlank()) Text(message, style = Typography.labelMedium)
        if (shareError.isNotBlank()) Text(shareError, color = MaterialTheme.colorScheme.error)
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = PrimaryGreen)
    }
}

@Composable
private fun PrimaryResultActions(
    busy: Boolean,
    onBack: () -> Unit,
    onNavigateToCamera: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(Icons.Rounded.CheckCircle, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Registro guardado local", color = Color.White)
        }
        OutlinedButton(
            enabled = !busy,
            onClick = onNavigateToCamera,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Nueva Foto de Muestra", color = PrimaryGreen)
        }
    }
}
