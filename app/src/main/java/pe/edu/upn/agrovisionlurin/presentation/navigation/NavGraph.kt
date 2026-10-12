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
import pe.edu.upn.agrovisionlurin.presentation.login.LoginScreen
import pe.edu.upn.agrovisionlurin.presentation.result.DiagnosticResultScreen
import pe.edu.upn.agrovisionlurin.presentation.settings.SettingsScreen
import pe.edu.upn.agrovisionlurin.presentation.local.LocalMvpViewModel
import pe.edu.upn.agrovisionlurin.presentation.management.ManagementScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier,
    session: LocalMvpViewModel = viewModel()
) {
    val user by session.user.collectAsStateWithLifecycle()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                session = session,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            if(user!=null) DashboardScreen(
                session = session,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToDetail = { inspectionId ->
                    navController.navigate(Screen.Result.createRoute(inspectionId))
                }
            )
        }
        composable(Screen.Camera.route) {
            if(user!=null) GuidedCameraScreen(
                session = session,
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
            if(user!=null) DiagnosticResultScreen(
                session = session,
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
            if(user!=null) HistorySyncScreen(
                session = session,
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { inspectionId ->
                    navController.navigate(Screen.Result.createRoute(inspectionId))
                }
            )
        }
        composable(Screen.Settings.route) {
            if(user!=null) SettingsScreen(
                session = session,
                onManageUsers = { navController.navigate(Screen.Users.route) },
                onManagePlots = { navController.navigate(Screen.Plots.route) },
                onLogout = {
                    session.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Users.route) {
            if(user!=null) ManagementScreen(session=session,usersMode=true,onBack={navController.popBackStack()})
        }
        composable(Screen.Plots.route) {
            if(user!=null) ManagementScreen(session=session,usersMode=false,onBack={navController.popBackStack()})
        }
    }
}
