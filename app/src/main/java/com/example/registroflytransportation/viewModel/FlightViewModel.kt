package com.example.registroflytransportation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroflytransportation.api.RetrofitClient
import com.example.registroflytransportation.model.Aeropuerto
import com.example.registroflytransportation.model.ErrorResponse
import com.example.registroflytransportation.model.VueloProgramadoDto
import com.example.registroflytransportation.util.DateConverter
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- Estados de la UI --- //
data class SearchFormState(
    val origenText: String = "",
    val destinoText: String = "",
    val fechaSalida: String = "",
    val origenSuggestions: List<Aeropuerto> = emptyList(),
    val destinoSuggestions: List<Aeropuerto> = emptyList(),
    val isLoadingOrigen: Boolean = false,
    val isLoadingDestino: Boolean = false
)

sealed class FlightSearchState {
    object Idle : FlightSearchState()
    object Loading : FlightSearchState()
    data class Success(val vuelos: List<VueloProgramadoDto>) : FlightSearchState()
    data class Error(val message: String) : FlightSearchState()
}

class FlightViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getInstance(application.applicationContext)
    private val gson = Gson()
    private var searchJob: Job? = null

    private val _searchFormState = MutableStateFlow(SearchFormState())
    val searchFormState = _searchFormState.asStateFlow()

    private val _flightSearchState = MutableStateFlow<FlightSearchState>(FlightSearchState.Idle)
    val flightSearchState = _flightSearchState.asStateFlow()

    fun onOrigenChanged(text: String) {
        _searchFormState.update { it.copy(origenText = text) }
        searchAirports(text, "origen")
    }

    fun onDestinoChanged(text: String) {
        _searchFormState.update { it.copy(destinoText = text) }
        searchAirports(text, "destino")
    }
    
    fun onSuggestionSelected(airport: Aeropuerto, field: String) {
        val airportLabel = "${airport.ciudad}, ${airport.pais} (${airport.codigo})"
        if (field == "origen") {
            _searchFormState.update { it.copy(origenText = airportLabel) }
            clearSuggestions("origen")
        } else {
             _searchFormState.update { it.copy(destinoText = airportLabel) }
            clearSuggestions("destino")
        }
    }
    
    fun onDateChanged(date: String) {
        _searchFormState.update { it.copy(fechaSalida = date) }
    }

    fun resetState() {
        _searchFormState.value = SearchFormState()
        _flightSearchState.value = FlightSearchState.Idle
    }

    private fun searchAirports(query: String, field: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            clearSuggestions(field)
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _searchFormState.update { if (field == "origen") it.copy(isLoadingOrigen = true) else it.copy(isLoadingDestino = true) }
            try {
                val response = apiService.searchAeropuertos(query)
                if (response.isSuccessful && response.body() != null) {
                    _searchFormState.update { 
                        if (field == "origen") it.copy(origenSuggestions = response.body()!!) 
                        else it.copy(destinoSuggestions = response.body()!!)
                    }
                }
            } finally {
                 _searchFormState.update { if (field == "origen") it.copy(isLoadingOrigen = false) else it.copy(isLoadingDestino = false) }
            }
        }
    }

    private fun clearSuggestions(field: String) {
        if (field == "origen") _searchFormState.update { it.copy(origenSuggestions = emptyList()) }
        if (field == "destino") _searchFormState.update { it.copy(destinoSuggestions = emptyList()) }
    }

    fun searchFlights() {
        viewModelScope.launch {
            val form = _searchFormState.value
            if (form.origenText.isBlank()) {
                _flightSearchState.value = FlightSearchState.Error("Debes introducir un origen.")
                return@launch
            }

            val fechaSalidaApi = DateConverter.convertToApiFormat(form.fechaSalida)
            if (fechaSalidaApi == null) {
                _flightSearchState.value = FlightSearchState.Error("Debes seleccionar una fecha de salida válida.")
                return@launch
            }

            _flightSearchState.value = FlightSearchState.Loading
            try {
                val response = apiService.buscarVuelos(
                    origen = form.origenText.split(",")[0].trim(),
                    destino = if (form.destinoText.isBlank()) null else form.destinoText.split(",")[0].trim(),
                    fechaSalida = fechaSalidaApi
                )

                if (response.isSuccessful && response.body() != null) {
                    if(response.body()!!.isEmpty()){
                        _flightSearchState.value = FlightSearchState.Error("No se encontraron vuelos para esta búsqueda.")
                    } else {
                        _flightSearchState.value = FlightSearchState.Success(response.body()!!)
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    _flightSearchState.value = FlightSearchState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _flightSearchState.value = FlightSearchState.Error(e.message ?: "Error de conexión")
            }
        }
    }
    
    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Error desconocido."
        return try {
            gson.fromJson(errorBody, ErrorResponse::class.java).message ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}