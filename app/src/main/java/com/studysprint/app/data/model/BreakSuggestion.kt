package com.studysprint.app.data.model

/**
 * What the Focus screen shows the user when a break begins. Combines the
 * chosen [BreakActivity] with the weather context (if any) that informed the
 * choice — so the UI can say "It's 24° and sunny — try a quick walk outside".
 */
data class BreakSuggestion(
    val activity: BreakActivity,
    val weather: WeatherInfo?,
)
