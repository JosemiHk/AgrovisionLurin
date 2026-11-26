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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upn.agrovisionlurin.presentation.components.LeafThumbnail
import pe.edu.upn.agrovisionlurin.data.local.awaitingLemonOrigin
import pe.edu.upn.agrovisionlurin.domain.model.CONFIRMED_ORIGIN_SUFFIX
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
    var editingNote by remember(resultId) { mutableStateOf(false) }
    var confirmDelete by remember(resultId) { mutableStateOf(false) }
    var deleting by remember(resultId) { mutableStateOf(false) }
    var note by remember(resultId) { mutableStateOf("") }
    LaunchedEffect(resultId) { session.open(resultId) }
    val record = detail?.takeIf { it.id == resultId }
    LaunchedEffect(record?.id, record?.subPlot) { record?.let { note = it.subPlot } }
    LaunchedEffect(deleting, busy, detail) {
        if (deleting && !busy) {
            if (detail?.id != resultId) onBack()
            deleting = false
        }
    }
    Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver") }
            Spacer(Modifier.width(8.dp))
            Text("Resultado del Diagnóstico", style = Typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        if (record == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (busy) CircularProgressIndicator(color = PrimaryGreen)
                else Text(message.ifBlank { "No se encontró esta inspección." }, style = Typography.bodyLarge)
            }
        } else {
            val statusColor = when {
                record.state == "ERROR" -> Color(0xFFC62828)
                record.state in listOf("BORRADOR", "NO_CONCLUYENTE", "LEGADO") -> Color(0xFFE65100)
                record.classCode == "MINADOR_COMPATIBLE" -> Color(0xFFC62828)
                else -> Color(0xFF2E7D32)
            }
            val title = when (record.state) {
                "ERROR" -> "Análisis no completado"
                "BORRADOR" -> "Fotografía pendiente de análisis"
                "NO_CONCLUYENTE" -> record.classLabel.ifBlank { "Resultado no concluyente" }
                "LEGADO" -> "Registro histórico"
                else -> record.classLabel.ifBlank { "Resultado sin etiqueta" }
            }
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFEEEEEE), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(if (record.modelVersion.isNotBlank()) "Analizado en ${record.latencyMs / 1000.0}s · Inferencia local" else "Registro conservado en este dispositivo",
                    style = Typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.DarkGray)
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Card(Modifier.fillMaxWidth().border(2.dp, statusColor, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(when {
                                record.state == "ERROR" -> Icons.Rounded.ErrorOutline
                                record.state != "VALIDO" -> Icons.Rounded.Info
                                record.classCode == "MINADOR_COMPATIBLE" -> Icons.Rounded.BugReport
                                else -> Icons.Rounded.CheckCircle
                            }, null, tint = statusColor, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(title, style = Typography.titleLarge, color = statusColor, fontWeight = FontWeight.Bold)
                            if (record.state == "VALIDO") Text("Puntuación del modelo: ${(record.confidence * 100).toInt()}%", style = Typography.bodyMedium)
                            if (record.awaitingLemonOrigin) {
                                Text("Se reconoce una hoja. Confirma el árbol de origen para evaluar sus síntomas.", style = Typography.bodyMedium)
                                Button(enabled = !busy, onClick = { session.confirmLemonOrigin(record.id) }) {
                                    Text("Sí, es de limonero. Analizar")
                                }
                                TextButton(enabled = !busy, onClick = onNavigateToCamera) { Text("Tomar otra foto") }
                            } else if (record.state == "NO_CONCLUYENTE") {
                                val guidance = if (record.diagnosisClass == -1) {
                                    "Acércate a una sola hoja de limón, enfócala y toma otra foto."
                                } else {
                                    "No se emitió una clasificación. Revisa la muestra con un técnico."
                                }
                                Text(guidance, style = Typography.bodyMedium)
                            }
                            if (record.modelVersion.endsWith(CONFIRMED_ORIGIN_SUFFIX)) {
                                Text("Origen confirmado por el usuario", style = Typography.bodyMedium)
                            }
                            if (record.state == "LEGADO") Text("Dominio anterior: clase ${record.diagnosisClass}. No representa un diagnóstico de limón.", style = Typography.bodyMedium)
                            if (record.errorMessage.isNotBlank()) Text(record.errorMessage, style = Typography.bodyMedium)
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LeafThumbnail(record.imagePath, size = 88.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Parcela ${record.plotCodeSnapshot.ifBlank { record.plotId }}", style = Typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(record.userNameSnapshot, style = Typography.bodyMedium)
                            if (record.modelVersion.isNotBlank()) Text("Modelo: ${record.modelVersion}", style = Typography.labelSmall)
                            if (record.subPlot.isNotBlank()) Text(record.subPlot, style = Typography.bodyMedium)
                        }
                    }
                }
                item {
                    Text("Recomendaciones Agronómicas", style = Typography.titleMedium, fontWeight = FontWeight.Bold)
                    val recommendations = remember(record.recommendationsJson) {
                        try {
                            val values = JSONArray(record.recommendationsJson)
                            (0 until values.length()).map { index ->
                                val entry = values.opt(index)
                                if (entry is org.json.JSONObject) entry.optString("description", entry.toString()) else entry.toString()
                            }
                        } catch (_: Exception) { if (record.recommendationsJson.isBlank()) emptyList() else listOf(record.recommendationsJson) }
                    }
                    if (recommendations.isEmpty()) Text("Sin recomendaciones automáticas para este estado.", style = Typography.bodyMedium)
                    recommendations.forEach { recommendation ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Search, null, tint = PrimaryGreen)
                                Spacer(Modifier.width(12.dp))
                                Text(recommendation, style = Typography.bodyMedium)
                            }
                        }
                    }
                }
                item {
                    if (record.state in listOf("BORRADOR", "ERROR")) OutlinedButton(enabled = !busy, onClick = { session.retry(record.id) }, modifier = Modifier.fillMaxWidth()) { Text("Reintentar análisis", color = PrimaryGreen) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(enabled = !busy, onClick = { editingNote = true }, modifier = Modifier.weight(1f)) { Text("Editar nota") }
                        OutlinedButton(enabled = !busy, onClick = {
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
                        }, modifier = Modifier.weight(1f)) { Icon(Icons.Rounded.Share, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("CSV") }
                    }
                    TextButton(enabled = !busy, onClick = { confirmDelete = true }) { Text("Eliminar inspección", color = Color(0xFFC62828)) }
                    if (message.isNotBlank()) Text(message, style = Typography.labelMedium)
                    if (shareError.isNotBlank()) Text(shareError, color = MaterialTheme.colorScheme.error)
                    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = PrimaryGreen)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                Icon(Icons.Rounded.CheckCircle, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Registro guardado local", color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(enabled = !busy, onClick = onNavigateToCamera, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Nueva Foto de Muestra", color = PrimaryGreen) }
            if (editingNote) AlertDialog(onDismissRequest = { editingNote = false }, title = { Text("Nota de ubicación / subparcela") },
                text = { OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nota") }) },
                confirmButton = { TextButton(enabled = !busy, onClick = { session.updateMetadata(record.id, note); editingNote = false }) { Text("Guardar") } },
                dismissButton = { TextButton(onClick = { editingNote = false }) { Text("Cancelar") } })
            if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("¿Eliminar esta inspección?") },
                text = { Text("Se eliminarán el registro y su fotografía local. Esta acción no se puede deshacer.") },
                confirmButton = { TextButton(enabled = !busy, onClick = { deleting = true; confirmDelete = false; session.deleteInspection(record.id) }) { Text("Eliminar") } },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } })
        }
    }
}
