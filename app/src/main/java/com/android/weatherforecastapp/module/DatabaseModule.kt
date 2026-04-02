package com.android.weatherforecastapp.module

import android.content.Context
import androidx.room.Room
import com.android.weatherforecastapp.data.WeatherDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): WeatherDatabase =
        Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather.db"
        ).build()

    @Provides
    fun provideDao(db: WeatherDatabase) = db.forecastDao()
}