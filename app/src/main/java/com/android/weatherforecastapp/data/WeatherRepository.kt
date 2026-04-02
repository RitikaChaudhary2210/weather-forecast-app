package com.android.weatherforecastapp.data

import android.database.sqlite.SQLiteException
import com.android.weatherforecastapp.ApiKey
import retrofit2.HttpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.UnknownHostException
import java.sql.SQLXML
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class WeatherRepository  @Inject constructor(
    private val api: WeatherApi,
    private val dao: ForecastDao,
    @ApiKey private val apiKey: String
) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getThreeDayForecast(city: String, forceRefresh: Boolean = false): List<ForecastDay> {
        return withContext(Dispatchers.IO) {
            val local = dao.getForecastForCity(city)
            val isCacheValid = local.isNotEmpty() && !isCacheExpired(local.first())
            if (isCacheValid && !forceRefresh) {
                return@withContext local.map { it.toDomain() }
            }

            try {
                val remote = api.getForecast(city, apiKey)
                val days = aggregateToThreeDays(remote)
                val normalizedCity = remote.city.name.trim().lowercase()

                dao.clearForecastForCity(normalizedCity)
                dao.insertAll(
                    days.map {
                        ForecastEntity(
                            cityName = remote.city.name,
                            date = it.date,
                            tempMin = it.tempMin,
                            tempMax = it.tempMax,
                            description = it.description,
                            icon = it.iconUrl,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                )
                days
            }
            catch (e: Exception) {
                val message = when (e) {
                    is retrofit2.HttpException -> when (e.code()) {
                        404 -> "City not found. Please check the city name and try again."
                        400 -> "Bad request. Please check the city name and try again."
                        else -> e.message ?: "Failed to load forecast"
                    }

                    is UnknownHostException -> "No internet connection. Check your network or try on a real device."
                    else -> e.message ?: "Failed to load forecast"
                }
                throw Exception(message)
            }
        }
    }

    private fun ForecastEntity.toDomain(): ForecastDay =
        ForecastDay(
            date = date,
            tempMin = tempMin,
            tempMax = tempMax,
            description = description,
            iconUrl = icon
        )

    private fun aggregateToThreeDays(response: ForecastResponse): List<ForecastDay> {
        val grouped = response.items.groupBy { item ->
            dateFormatter.format(Date(item.dt * 1000L))
        }

        return grouped.entries
            .sortedBy { it.key }
            .take(3)
            .map { (date, entries) ->
                val minTemp = entries.minOf { it.main.tempMin }
                val maxTemp = entries.maxOf { it.main.tempMax }
                val weather = entries.firstOrNull()?.weather?.firstOrNull()
                ForecastDay(
                    date = date,
                    tempMin = minTemp,
                    tempMax = maxTemp,
                    description = weather?.description ?: "",
                    iconUrl = weather?.icon?.let { "https://openweathermap.org/img/wn/${it}@2x.png" } ?: ""
                )
            }
    }


    private  val CACHE_TIMEOUT= 60 * 60 * 1000L // 1 hour in milliseconds
    private fun isCacheExpired(entity: ForecastEntity):Boolean{
        val currentTime =System.currentTimeMillis()
        return currentTime - entity.timestamp>CACHE_TIMEOUT

    }
}
