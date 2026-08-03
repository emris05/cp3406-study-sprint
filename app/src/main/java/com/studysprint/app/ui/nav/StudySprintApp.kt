package com.studysprint.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studysprint.app.R
import com.studysprint.app.ui.focus.FocusScreen
import com.studysprint.app.ui.flashcards.DeckDetailScreen
import com.studysprint.app.ui.flashcards.DeckListScreen
import com.studysprint.app.ui.flashcards.ReviewScreen
import com.studysprint.app.ui.home.HomeScreen
import com.studysprint.app.ui.settings.SettingsScreen
import com.studysprint.app.ui.stats.StatsScreen
import com.studysprint.app.ui.tasks.TasksScreen

/**
 * Root composable. Holds the [NavHost] and the bottom navigation bar.
 * Tasks is reachable from Home and Focus (not a bottom-nav item) to keep
 * the bar uncluttered with the four core screens.
 */
@Composable
fun StudySprintApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Only show the bottom bar on the four core screens.
            if (currentRoute in bottomNavRoutes.map { it.route }) {
                NavigationBar {
                    bottomNavRoutes.forEach { route ->
                        val label = when (route) {
                            Route.Home -> stringResource(R.string.nav_home)
                            Route.Focus -> stringResource(R.string.nav_focus)
                            Route.Stats -> stringResource(R.string.nav_stats)
                            Route.Settings -> stringResource(R.string.nav_settings)
                            else -> ""
                        }
                        val icon = when (route) {
                            Route.Home -> Icons.Outlined.Home
                            Route.Focus -> Icons.Outlined.Timer
                            Route.Stats -> Icons.Outlined.BarChart
                            Route.Settings -> Icons.Outlined.Settings
                            else -> Icons.Outlined.Home
                        }
                        val selected = backStackEntry?.destination?.hierarchy?.any { it.route == route.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateTo(route) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Route.Home.route) {
                HomeScreen(
                    onNavigateToFocus = { navController.navigate(Route.Focus.route) },
                    onNavigateToTasks = { navController.navigate(Route.Tasks.route) },
                    onNavigateToFlashcards = { navController.navigate(Route.Flashcards.route) },
                )
            }
            composable(Route.Focus.route) {
                FocusScreen(
                    onNavigateToTasks = { navController.navigate(Route.Tasks.route) },
                )
            }
            composable(Route.Tasks.route) {
                TasksScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.Stats.route) {
                StatsScreen()
            }
            composable(Route.Settings.route) {
                SettingsScreen()
            }
            composable(Route.Flashcards.route) {
                DeckListScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDeck = { deckId -> navController.navigate(Route.DeckDetail.build(deckId)) },
                )
            }
            composable(Route.DeckDetail.route) { backStackEntry ->
                DeckDetailScreen(
                    onBack = { navController.popBackStack() },
                    onReview = { deckId -> navController.navigate(Route.Review.build(deckId)) },
                )
            }
            composable(Route.Review.route) {
                ReviewScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Standard single-top navigation helper for bottom-nav items. */
private fun androidx.navigation.NavController.navigateTo(route: Route) {
    navigate(route.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
