package pe.edu.upn.agrovisionlurin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.upn.agrovisionlurin.presentation.dashboard.DashboardScreen
import pe.edu.upn.agrovisionlurin.presentation.camera.GuidedCameraScreen
import pe.edu.upn.agrovisionlurin.presentation.result.DiagnosticResultScreen
import pe.edu.upn.agrovisionlurin.presentation.history.HistorySyncScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
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
                onBack = { navController.popBackStack() }
            )
        }
    }
}
