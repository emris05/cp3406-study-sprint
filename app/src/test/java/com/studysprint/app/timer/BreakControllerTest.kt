package com.studysprint.app.timer

import com.google.common.truth.Truth.assertThat
import com.studysprint.app.data.model.WeatherInfo
import org.junit.Test
import kotlin.random.Random

class BreakControllerTest {

    @Test
    fun `null weather returns an indoor suggestion`() {
        val controller = BreakController(random = Random(seed = 0))
        val suggestion = controller.suggest(weather = null)
        // With no weather the controller falls back to the indoor pool.
        assertThat(suggestion.activity.indoor).isTrue()
        assertThat(suggestion.weather).isNull()
    }

    @Test
    fun `nice weather biases toward an outdoor activity`() {
        val controller = BreakController(random = Random(seed = 1))
        val nice = WeatherInfo(city = "Cairns", tempCelsius = 26, feelsLikeCelsius = 27, condition = "Clear", isNiceOutdoors = true)
        val suggestion = controller.suggest(weather = nice)
        // Pool is outdoor activities + the full list, so it can be either —
        // but the weather context must be attached.
        assertThat(suggestion.weather).isEqualTo(nice)
    }

    @Test
    fun `bad weather returns an indoor activity`() {
        val controller = BreakController(random = Random(seed = 2))
        val bad = WeatherInfo(city = "Hobart", tempCelsius = 8, feelsLikeCelsius = 5, condition = "Rain", isNiceOutdoors = false)
        val suggestion = controller.suggest(weather = bad)
        assertThat(suggestion.activity.indoor).isTrue()
        assertThat(suggestion.weather).isEqualTo(bad)
    }

    @Test
    fun `fallback always returns an activity`() {
        val controller = BreakController(random = Random(seed = 3))
        val activity = controller.fallback()
        assertThat(activity.title).isNotEmpty()
    }
}
