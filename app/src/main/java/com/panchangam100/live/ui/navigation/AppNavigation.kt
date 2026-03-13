package com.panchangam100.live.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.panchangam100.live.data.datastore.AppPreferences
import com.panchangam100.live.data.model.Language
import com.panchangam100.live.ui.screens.calendar.CalendarScreen
import com.panchangam100.live.ui.screens.detail.DayDetailScreen
import com.panchangam100.live.ui.screens.festivals.FestivalsScreen
import com.panchangam100.live.ui.screens.home.HomeScreen
import com.panchangam100.live.ui.screens.location.LocationPickerScreen
import com.panchangam100.live.ui.screens.settings.SettingsScreen
import com.panchangam100.live.ui.screens.splash.SplashScreen
import com.panchangam100.live.utils.LanguageManager

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Festivals : Screen("festivals")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{date}") {
        fun route(date: String) = "detail/$date"
    }
    object LocationPicker : Screen("location_picker")
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val labelKey: String
)

@Composable
fun AppNavigation(prefs: AppPreferences) {
    val navController = rememberNavController()
    val lang by prefs.languageFlow.collectAsState(initial = Language.TELUGU)

    val bottomItems = listOf(
        BottomNavItem(Screen.Home, Icons.Default.Home, "today"),
        BottomNavItem(Screen.Calendar, Icons.Default.CalendarMonth, "calendar"),
        BottomNavItem(Screen.Festivals, Icons.Default.Celebration, "festivals"),
        BottomNavItem(Screen.Settings, Icons.Default.Settings, "settings"),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Calendar.route,
        Screen.Festivals.route, Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDest = navBackStackEntry?.destination
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDest?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(LanguageManager.label(item.labelKey, lang)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding),
            enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onReady = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    prefs = prefs,
                    onDateClick = { date -> navController.navigate(Screen.Detail.route(date)) },
                    onLocationClick = { navController.navigate(Screen.LocationPicker.route) }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    prefs = prefs,
                    onDateClick = { date -> navController.navigate(Screen.Detail.route(date)) }
                )
            }
            composable(Screen.Festivals.route) {
                FestivalsScreen(
                    prefs = prefs,
                    onDateClick = { date -> navController.navigate(Screen.Detail.route(date)) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    prefs = prefs,
                    onLocationClick = { navController.navigate(Screen.LocationPicker.route) }
                )
            }
            composable(
                Screen.Detail.route,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStack ->
                val dateStr = backStack.arguments?.getString("date") ?: ""
                DayDetailScreen(
                    dateStr = dateStr,
                    prefs = prefs,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.LocationPicker.route) {
                LocationPickerScreen(
                    prefs = prefs,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
