package com.android.weatherforecastapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** DAO for local forecast storage; used for offline access to last fetched data. */
@Dao
interface ForecastDao {

    @Query("SELECT * FROM forecast WHERE cityName = :city ORDER BY date LIMIT 3")
    suspend fun getForecastForCity(city: String): List<ForecastEntity>

    @Query("DELETE FROM forecast WHERE cityName = :city")
    suspend fun clearForecastForCity(city: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ForecastEntity>)
}
