package com.studysprint.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * OpenWeatherMap "current weather" response (subset of fields we use).
 * Only the bits relevant to suggesting a break activity are modelled.
 */
@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "weather") val weather: List<WeatherCondition> = emptyList(),
    @Json(name = "main") val main: Main = Main(),
    @Json(name = "name") val name: String? = null,
)

@JsonClass(generateAdapter = true)
data class WeatherCondition(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "main") val main: String = "",
    @Json(name = "description") val description: String = "",
)

@JsonClass(generateAdapter = true)
data class Main(
    @Json(name = "temp") val temp: Double = 0.0,
    @Json(name = "feels_like") val feelsLike: Double = 0.0,
    @Json(name = "humidity") val humidity: Int = 0,
)
