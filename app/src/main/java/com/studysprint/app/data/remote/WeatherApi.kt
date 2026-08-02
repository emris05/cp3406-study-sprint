package com.studysprint.app.data.remote

import com.studysprint.app.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the OpenWeatherMap current-weather endpoint.
 * Units are metric so we get Celsius. The API key is injected per-request
 * by the repository (kept out of the interface for testability).
 */
interface WeatherApi {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
    ): WeatherResponse
}
