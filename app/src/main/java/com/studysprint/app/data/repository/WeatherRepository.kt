package com.studysprint.app.data.repository

import com.studysprint.app.BuildConfig
import com.studysprint.app.data.model.WeatherInfo
import com.studysprint.app.data.remote.WeatherApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the [WeatherApi]. Returns null on any failure (network, parse, bad key)
 * so callers can fall back to the offline break-activity library. No exceptions
 * leak to the UI — a missing weather suggestion is never fatal.
 */
interface WeatherRepository {
    suspend fun getCurrentWeather(city: String): WeatherInfo?
}

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
) : WeatherRepository {

    override suspend fun getCurrentWeather(city: String): WeatherInfo? = runCatching {
        if (city.isBlank()) return null
        if (BuildConfig.OPEN_WEATHER_API_KEY.isBlank() ||
            BuildConfig.OPEN_WEATHER_API_KEY == "PLACEHOLDER_REPLACE_IN_LOCAL_PROPERTIES"
        ) {
            return null
        }
        api.getCurrentWeather(city = city.trim(), apiKey = BuildConfig.OPEN_WEATHER_API_KEY)
            .toDomain()
    }.getOrNull()
}
