package com.android.weatherforecastapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.android.weatherforecastapp.data.WeatherApi
import com.android.weatherforecastapp.data.WeatherDatabase
import com.android.weatherforecastapp.data.WeatherRepository
import com.android.weatherforecastapp.ui.WeatherRoute
import com.android.weatherforecastapp.ui.WeatherViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiKey = BuildConfig.WEATHER_API_KEY

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val api = retrofit.create(WeatherApi::class.java)
        val db = Room.databaseBuilder(applicationContext, WeatherDatabase::class.java, "weather.db").build()
        val repository = WeatherRepository(api, db.forecastDao(), apiKey)

        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val viewModel: WeatherViewModel = viewModel {
                    WeatherViewModel(repository)
                }
                WeatherRoute(viewModel)
            }
        }
    }
}
