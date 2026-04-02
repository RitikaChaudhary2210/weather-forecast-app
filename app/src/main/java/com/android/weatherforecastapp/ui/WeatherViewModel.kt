package com.android.weatherforecastapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.weatherforecastapp.data.ForecastDay
import com.android.weatherforecastapp.data.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

data class WeatherUiState(
    val city: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val forecast: List<ForecastDay> = emptyList()
)
@HiltViewModel // ✅ ADD
class WeatherViewModel @Inject constructor(             // ✅ ADD @Inject
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState

    private val cityFlow = MutableStateFlow("")

//    init {
//        loadForecast()
//    }
    init{
        viewModelScope.launch {
            cityFlow
                .debounce (500)// Wait for 500ms of inactivity before processing}
                .collectLatest{ city ->
                if (city.isNotBlank()) {
                    loadForecast()
                }

            }
        }
    }

    fun onCityChanged(newCity: String) {
        _uiState.value = _uiState.value.copy(city = newCity)
        cityFlow.value = newCity
    }

    fun loadForecast(forceRefresh: Boolean = false) {
        val currentCity = _uiState.value.city.trim()
        if (currentCity.isEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, forecast = emptyList()   )

        viewModelScope.launch {
            try {
                val days = repository.getThreeDayForecast(currentCity, forceRefresh)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    forecast = days,
                    errorMessage = null,

                )
            }  catch (e: Exception) {
                val message = when (e) {
                    is retrofit2.HttpException -> when (e.code()) {
                        404 -> "City not found. Please check the city name and try again."
                        400 -> "Bad request. Please check the city name and try again."
                        else -> e.message ?: "Failed to load forecast"
                    }

                    is UnknownHostException -> "No internet connection. Check your network or try on a real device."
                    else -> e.message ?: "Failed to load forecast"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )

            }
        }
    }
}
