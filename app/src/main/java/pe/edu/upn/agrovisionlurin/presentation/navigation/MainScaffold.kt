package pe.edu.upn.agrovisionlurin.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.edu.upn.agrovisionlurin.data.local.AppDatabase
import pe.edu.upn.agrovisionlurin.data.local.UserPreferencesManager

@Composable
fun MainScaffold() {
    val context = LocalContext.current

    // Determinar destino inicial en base a sesión persistida
    val prefsManager = remember { UserPreferencesManager.getInstance(context) }
    val startDestination = if (prefsManager.isLoggedIn) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val pendingSyncCount by remember(context) {
        AppDatabase.getDatabase(context).inspectionDao().getPendingSyncCount()
    }.collectAsStateWithLifecycle(initialValue = 0)

    // Ocultar NavigationBar en login, captura y detalle diagnóstico
    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.History.route,
        Screen.Settings.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFE5E7EB)
                    )
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        // 1. Inicio
                        val isDashboardSelected = currentRoute == Screen.Dashboard.route
                        NavigationBarItem(
                            selected = isDashboardSelected,
                            onClick = {
                                if (!isDashboardSelected) {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isDashboardSelected) Icons.Rounded.Home else Icons.Outlined.Home,
                                    contentDescription = "Inicio"
                                )
                            },
                            label = {
                                Text(
                                    text = "Inicio",
                                    fontSize = 12.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1B5E20),
                                selectedTextColor = Color(0xFF1B5E20),
                                indicatorColor = Color(0xFFE8F5E9),
                                unselectedIconColor = Color(0xFF5E636E),
                                unselectedTextColor = Color(0xFF5E636E)
                            )
                        )

                        // 2. Historial (con BadgedBox reactivo a Room)
                        val isHistorySelected = currentRoute == Screen.History.route
                        NavigationBarItem(
                            selected = isHistorySelected,
                            onClick = {
                                if (!isHistorySelected) {
                                    navController.navigate(Screen.History.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (pendingSyncCount > 0) {
                                            Badge(
                                                containerColor = Color(0xFFD97706),
                                                contentColor = Color.White
                                            ) {
                                                Text(pendingSyncCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isHistorySelected) Icons.Rounded.History else Icons.Outlined.History,
                                        contentDescription = "Historial"
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = "Historial",
                                    fontSize = 12.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1B5E20),
                                selectedTextColor = Color(0xFF1B5E20),
                                indicatorColor = Color(0xFFE8F5E9),
                                unselectedIconColor = Color(0xFF5E636E),
                                unselectedTextColor = Color(0xFF5E636E)
                            )
                        )

                        // 3. Configuración
                        val isSettingsSelected = currentRoute == Screen.Settings.route
                        NavigationBarItem(
                            selected = isSettingsSelected,
                            onClick = {
                                if (!isSettingsSelected) {
                                    navController.navigate(Screen.Settings.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSettingsSelected) Icons.Rounded.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Configuración"
                                )
                            },
                            label = {
                                Text(
                                    text = "Configuración",
                                    fontSize = 12.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1B5E20),
                                selectedTextColor = Color(0xFF1B5E20),
                                indicatorColor = Color(0xFFE8F5E9),
                                unselectedIconColor = Color(0xFF5E636E),
                                unselectedTextColor = Color(0xFF5E636E)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
