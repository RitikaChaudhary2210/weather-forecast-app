package com.android.weatherforecastapp.data

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse
}

data class ForecastResponse(
    @Json(name = "city") val city: CityInfo,
    @Json(name = "list") val items: List<ForecastItem>
)

data class CityInfo(
    @Json(name = "name") val name: String
)

data class ForecastItem(
    @Json(name = "dt") val dt: Long,
    @Json(name = "main") val main: MainInfo,
    @Json(name = "weather") val weather: List<WeatherInfo>
)

data class MainInfo(
    @Json(name = "temp_min") val tempMin: Double,
    @Json(name = "temp_max") val tempMax: Double
)

data class WeatherInfo(
    @Json(name = "description") val description: String,
    @Json(name = "icon") val icon: String
)
