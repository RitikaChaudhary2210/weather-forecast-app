package com.android.weatherforecastapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository(
    private val api: WeatherApi,
    private val dao: ForecastDao,
    private val apiKey: String
) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getThreeDayForecast(city: String, forceRefresh: Boolean = false): List<ForecastDay> {
        return withContext(Dispatchers.IO) {
            val local = dao.getForecastForCity(city)
            if (local.isNotEmpty() && !forceRefresh) {
                return@withContext local.map { it.toDomain() }
            }

            try {
                val remote = api.getForecast(city, apiKey)
                val days = aggregateToThreeDays(remote)

                dao.clearForecastForCity(remote.city.name)
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
            } catch (e: Exception) {
                if (local.isNotEmpty()) {
                    local.map { it.toDomain() }
                } else {
                    throw e
                }
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
}
