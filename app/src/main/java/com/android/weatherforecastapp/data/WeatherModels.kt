package com.android.weatherforecastapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for local storage of 3-day forecast so users can access it offline.
 * Fields align with the API forecast: city, date, min/max temp, conditions, icon.
 */
@Entity(tableName = "forecast")
data class ForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val cityName: String,
    val date: String,
    val tempMin: Double,
    val tempMax: Double,
    val description: String,
    val icon: String,
    val timestamp: Long
)

data class ForecastDay(
    val date: String,
    val tempMin: Double,
    val tempMax: Double,
    val description: String,
    val iconUrl: String
)
