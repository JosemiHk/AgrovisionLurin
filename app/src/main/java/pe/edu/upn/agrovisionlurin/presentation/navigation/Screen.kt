package pe.edu.upn.agrovisionlurin.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Camera : Screen("camera")
    object Result : Screen("result/{inspectionId}") {
        fun createRoute(inspectionId: String) = "result/$inspectionId"
    }
    object History : Screen("history")
    object Settings : Screen("settings")
}
