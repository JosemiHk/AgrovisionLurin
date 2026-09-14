package pe.edu.upn.agrovisionlurin.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upn.agrovisionlurin.presentation.theme.*

@Composable
fun DashboardScreen(
    onNavigateToCamera: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigateToHistory: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val pendingCount by viewModel.pendingSyncCount.collectAsState()

    Scaffold(
        topBar = {
            Column {
                OfflineBanner(pendingCount)
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCamera,
                containerColor = PrimaryGreen,
                contentColor = OnPrimary,
                modifier = Modifier.height(56.dp)
            ) {
                Text(text = "NUEVA INSPECCIÓN", style = Typography.labelLarge)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Panel de Parcela", style = Typography.headlineLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Valle Bajo - Lurín", style = Typography.bodyLarge, color = TextCharcoal.copy(alpha = 0.7f))
            }
            
            items(3) { index ->
                InspectionCard(index = index)
            }
        }
    }
}

@Composable
fun OfflineBanner(pendingCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AmberWarning)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Modo Local (Offline Listo) — $pendingCount pendientes",
            color = OnPrimary,
            style = Typography.labelMedium
        )
    }
}

@Composable
fun InspectionCard(index: Int) {
    val (statusColor, statusText, statusBg) = when(index) {
        0 -> Triple(StressOrange, "Estrés Hídrico", LightStressOrange)
        1 -> Triple(CriticalRed, "Alerta Plaga", LightPestRed)
        else -> Triple(OptimalGreen, "Óptimo", LightOptimalGreen)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BorderOutline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Surco ${index + 4}", style = Typography.titleLarge)
                
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(statusText, color = statusColor, style = Typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceNeutral, contentColor = PrimaryGreen),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryGreen)
            ) {
                Text("VER DETALLES", style = Typography.labelLarge)
            }
        }
    }
}
