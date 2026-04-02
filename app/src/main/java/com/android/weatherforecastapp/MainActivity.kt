package com.android.weatherforecastapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.android.weatherforecastapp.data.WeatherApi
import com.android.weatherforecastapp.data.WeatherDatabase
import com.android.weatherforecastapp.data.WeatherRepository
import com.android.weatherforecastapp.ui.WeatherDetailsScreen
import com.android.weatherforecastapp.ui.WeatherRoute
import com.android.weatherforecastapp.ui.WeatherViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiKey = "d95d8ab37d85bb0cc95c4a6a71f2dba0"

//        val logging = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BASIC
//        }
//
//        val client = OkHttpClient.Builder().addInterceptor(logging).build()
//
//        val moshi = Moshi.Builder()
//            .add(KotlinJsonAdapterFactory())
//            .build()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://api.openweathermap.org/")
//            .client(client)
//            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .build()
//
//        val api = retrofit.create(WeatherApi::class.java)
//
//        val db = Room.databaseBuilder(
//            applicationContext,
//            WeatherDatabase::class.java,
//            "weather.db"
//        ).build()
//
//        val repository = WeatherRepository(api, db.forecastDao(), apiKey)

        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {

//                val viewModel: WeatherViewModel = viewModel {
//                    WeatherViewModel(repository)
//                }

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "weather") {
                  // ✅ correct place
                    // ✅ HOME SCREEN
                    composable("weather") {
                        val viewModel: WeatherViewModel = hiltViewModel()
                        WeatherRoute(
                             viewModel =viewModel , // ✅ GET HILT VIEWMODEL
                             onClick = { day ->
                                navController.navigate("details/${day.date}")
                                 }
                        )
                    }

                    // ✅ DETAIL SCREEN
                    composable("details/{date}") { backStackEntry ->

                        val viewModel: WeatherViewModel = hiltViewModel() // ✅ FIX


                        val date = backStackEntry.arguments?.getString("date") ?: ""

                        val state = viewModel.uiState.collectAsState().value

                        val day = state.forecast.find { it.date == date }

                        if (day != null) {
                            WeatherDetailsScreen(day = day)
                        } else {
                            Text("Details not available")
                        }
                    }
                }
            }
        }
    }
}