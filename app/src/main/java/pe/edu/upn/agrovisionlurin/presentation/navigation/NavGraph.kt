package pe.edu.upn.agrovisionlurin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.upn.agrovisionlurin.presentation.camera.GuidedCameraScreen
import pe.edu.upn.agrovisionlurin.presentation.dashboard.DashboardScreen
import pe.edu.upn.agrovisionlurin.presentation.history.HistorySyncScreen
import pe.edu.upn.agrovisionlurin.presentation.result.DiagnosticResultScreen
import pe.edu.upn.agrovisionlurin.presentation.settings.SettingsScreen

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
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToDetail = { inspectionId ->
                    navController.navigate(Screen.Result.createRoute(inspectionId))
                }
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
            SettingsScreen()
        }
    }
}
