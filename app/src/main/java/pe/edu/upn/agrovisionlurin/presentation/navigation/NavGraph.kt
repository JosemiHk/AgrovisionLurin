package pe.edu.upn.agrovisionlurin.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.upn.agrovisionlurin.presentation.camera.GuidedCameraScreen
import pe.edu.upn.agrovisionlurin.presentation.dashboard.DashboardScreen
import pe.edu.upn.agrovisionlurin.presentation.history.HistorySyncScreen
import pe.edu.upn.agrovisionlurin.presentation.result.DiagnosticResultScreen
import pe.edu.upn.agrovisionlurin.presentation.theme.Typography

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }
        composable(Screen.Camera.route) {
            GuidedCameraScreen(
                onBack = { navController.popBackStack() },
                onCaptureComplete = { inspectionId -> 
                    navController.navigate(Screen.Result.createRoute(inspectionId)) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            )
        }
        composable(Screen.Result.route) { backStackEntry ->
            val inspectionId = backStackEntry.arguments?.getString("inspectionId") ?: ""
            DiagnosticResultScreen(
                resultId = inspectionId,
                onBack = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            )
        }
        composable(Screen.History.route) {
            HistorySyncScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { inspectionId ->
                    navController.navigate(Screen.Result.createRoute(inspectionId))
                }
            )
        }
        composable(Screen.Settings.route) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Configuración del Sistema Agrícola\nValle de Lurín",
                    style = Typography.titleMedium,
                    color = Color(0xFF1C1B1F)
                )
            }
        }
    }
}
