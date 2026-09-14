package pe.edu.upn.agrovisionlurin.presentation.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DiagnosticResultScreen(
    inspectionId: String,
    onBackToDashboard: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Resultados del Diagnóstico: $inspectionId")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackToDashboard) {
            Text("VOLVER AL PANEL")
        }
    }
}
