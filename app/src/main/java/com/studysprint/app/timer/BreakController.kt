package com.studysprint.app.timer

import com.studysprint.app.data.model.BreakActivity
import com.studysprint.app.data.model.BreakActivityLibrary
import com.studysprint.app.data.model.BreakSuggestion
import com.studysprint.app.data.model.WeatherInfo

/**
 * Decides what break activity to suggest. Pure logic over inputs — no Android,
 * no coroutines — so the suggestion rule is unit-testable on the JVM.
 *
 * Rule: if we have weather and it's nice out, bias toward an outdoor activity.
 * Otherwise pick from the indoor set. If weather is null (offline / API error),
 * fall back to the full library.
 */
class BreakController(
    private val library: BreakActivityLibrary = BreakActivityLibrary,
    private val random: kotlin.random.Random = kotlin.random.Random,
) {

    fun suggest(weather: WeatherInfo?): BreakSuggestion {
        val preferOutdoor = weather?.isNiceOutdoors == true
        val activity = library.pick(preferOutdoor = preferOutdoor, random = random)
        return BreakSuggestion(activity = activity, weather = weather)
    }

    /** Convenience for tests / callers that just want a quick fallback activity. */
    fun fallback(): BreakActivity = library.pick(preferOutdoor = false, random = random)
}
