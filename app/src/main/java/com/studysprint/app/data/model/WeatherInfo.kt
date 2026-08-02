package com.studysprint.app.data.model

/**
 * Domain-level weather snapshot. The repository maps the raw DTO into this so
 * the UI never sees Retrofit/Moshi types.
 */
data class WeatherInfo(
    val city: String,
    val tempCelsius: Int,
    val feelsLikeCelsius: Int,
    val condition: String,
    val isNiceOutdoors: Boolean,
)
