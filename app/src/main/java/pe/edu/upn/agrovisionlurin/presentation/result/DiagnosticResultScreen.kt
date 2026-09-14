package pe.edu.upn.agrovisionlurin.presentation.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upn.agrovisionlurin.presentation.theme.PrimaryGreen
import pe.edu.upn.agrovisionlurin.presentation.theme.Typography

@Composable
fun DiagnosticResultScreen(
    resultId: String,
    onBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    viewModel: ResultViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(resultId) {
        viewModel.loadResult(resultId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Resultado del Diagnóstico", style = Typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is ResultUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            is ResultUiState.Success -> {
                val diag = state.diagnostic
                
                // Latency Chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEEEEEE),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Analizado en ${diag.latencyMs / 1000.0}s (Inferencia Local)",
                        style = Typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.DarkGray
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Status Card
                val borderColor = when (diag.classId) {
                    1 -> Color(0xFFC62828) // Trips (Red)
                    2 -> Color(0xFFE65100) // Estrés hídrico (Orange)
                    else -> Color(0xFF2E7D32) // Sano (Green)
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val icon = when (diag.classId) {
                            1 -> Icons.Rounded.BugReport
                            2 -> Icons.Rounded.WaterDrop
                            else -> Icons.Rounded.CheckCircle
                        }
                        Icon(icon, contentDescription = null, tint = borderColor, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(diag.className, style = Typography.headlineMedium, color = borderColor, fontWeight = FontWeight.Bold)
                        Text("Confianza: ${(diag.confidence * 100).toInt()}% • Severidad: ${diag.severityLevel}", style = Typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Recomendaciones Agronómicas", style = Typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.recommendations) { rec ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                val recIcon = when {
                                    rec.description.contains("riego", ignoreCase = true) -> Icons.Rounded.WaterDrop
                                    rec.description.contains("aplicación", ignoreCase = true) -> Icons.Rounded.Science
                                    else -> Icons.Rounded.Search
                                }
                                Icon(recIcon, contentDescription = null, tint = PrimaryGreen)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(rec.description, style = Typography.bodyMedium)
                            }
                        }
                    }
                }

                // Bottom Buttons
                Button(
                    onClick = { viewModel.saveToLocalRegistry(onBack) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Guardar en Registro Local", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onNavigateToCamera,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Repetir Foto de Muestra", color = PrimaryGreen)
                }
            }
            is ResultUiState.Error -> {
                Text("Error: ${state.message}", color = Color.Red)
            }
        }
    }
}
