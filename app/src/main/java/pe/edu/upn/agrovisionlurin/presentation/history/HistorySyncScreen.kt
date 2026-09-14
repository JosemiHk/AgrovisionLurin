package pe.edu.upn.agrovisionlurin.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HistorySyncScreen(
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial y Sincronización")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("VOLVER")
        }
    }
}
