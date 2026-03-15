package com.android.weatherforecastapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.weatherforecastapp.data.ForecastDay
import com.android.weatherforecastapp.data.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

data class WeatherUiState(
    val city: String = "London",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val forecast: List<ForecastDay> = emptyList()
)

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState

    init {
        loadForecast()
    }

    fun onCityChanged(newCity: String) {
        _uiState.value = _uiState.value.copy(city = newCity)
    }

    fun loadForecast(forceRefresh: Boolean = false) {
        val currentCity = _uiState.value.city.trim()
        if (currentCity.isEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val days = repository.getThreeDayForecast(currentCity, forceRefresh)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    forecast = days,
                    errorMessage = null
                )
            } catch (e: Exception) {
                val message = when (e) {
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
