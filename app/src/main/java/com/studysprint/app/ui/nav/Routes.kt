package com.studysprint.app.ui.nav

/**
 * Type-safe-ish navigation destinations. Keeping routes in one object avoids
 * stringly-typed bugs and makes the nav graph easy to scan.
 *
 * @property route the unique nav-controller route
 * @property showInBottomBar whether this is a top-level bottom-nav destination
 */
sealed class Route(val route: String, val showInBottomBar: Boolean) {
    data object Home : Route("home", showInBottomBar = true)
    data object Focus : Route("focus", showInBottomBar = true)
    data object Tasks : Route("tasks", showInBottomBar = false)
    data object Stats : Route("stats", showInBottomBar = true)
    data object Settings : Route("settings", showInBottomBar = true)
}

/** Bottom-nav entries (the four core screens the brief requires). */
val bottomNavRoutes = listOf(Route.Home, Route.Focus, Route.Stats, Route.Settings)
