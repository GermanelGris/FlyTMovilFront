package com.example.registroflytransportation.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroflytransportation.api.RetrofitClient
import com.example.registroflytransportation.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- Estados de la UI --- //
sealed class VuelosAdminState {
    object Loading : VuelosAdminState()
    data class Success(val vuelos: List<VueloProgramadoDto>) : VuelosAdminState()
    data class Error(val message: String) : VuelosAdminState()
}

data class AdminFormState(
    val aerolineas: List<Aerolinea> = emptyList(),
    val origenSuggestions: List<Aeropuerto> = emptyList(),
    val destinoSuggestions: List<Aeropuerto> = emptyList(),
    val isLoadingAirlines: Boolean = true,
    val isLoadingOrigen: Boolean = false,
    val isLoadingDestino: Boolean = false
)

class VueloProgramadoViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getInstance(application.applicationContext)
    private val gson = Gson()
    private var searchJob: Job? = null

    private val _vuelosState = MutableStateFlow<VuelosAdminState>(VuelosAdminState.Loading)
    val vuelosState = _vuelosState.asStateFlow()

    private val _formState = MutableStateFlow(AdminFormState())
    val formState = _formState.asStateFlow()

    // ¡ELIMINADO! El bloque init que causaba el crash en los tests.
    /*
    init {
        loadInitialData()
    }
    */

    // ¡NUEVO! Ahora esta función es pública para ser llamada desde la UI.
    fun loadInitialData() {
        loadVuelos()
        loadAirlines()
    }

    fun loadVuelos() {
        viewModelScope.launch {
            _vuelosState.value = VuelosAdminState.Loading
            try {
                val response = apiService.getAllVuelosProgramados()
                if (response.isSuccessful && response.body() != null) {
                    _vuelosState.value = VuelosAdminState.Success(response.body()!!)
                } else {
                    _vuelosState.value = VuelosAdminState.Error(parseErrorMessage(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _vuelosState.value = VuelosAdminState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    private fun loadAirlines() {
        viewModelScope.launch {
            _formState.update { it.copy(isLoadingAirlines = true) }
            try {
                val response = apiService.getAerolineas()
                if (response.isSuccessful && response.body() != null) {
                    _formState.update { it.copy(aerolineas = response.body()!!) }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error cargando aerolíneas: ${e.message}")
            } finally {
                _formState.update { it.copy(isLoadingAirlines = false) }
            }
        }
    }

    fun searchAirports(query: String, field: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            clearSuggestions(field)
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _formState.update { if (field == "origen") it.copy(isLoadingOrigen = true) else it.copy(isLoadingDestino = true) }
            try {
                val response = apiService.searchAeropuertos(query)
                if (response.isSuccessful && response.body() != null) {
                    _formState.update { 
                        if (field == "origen") it.copy(origenSuggestions = response.body()!!) 
                        else it.copy(destinoSuggestions = response.body()!!)
                    }
                }
            } finally {
                 _formState.update { if (field == "origen") it.copy(isLoadingOrigen = false) else it.copy(isLoadingDestino = false) }
            }
        }
    }

    fun clearSuggestions(field: String) {
        if (field == "origen") _formState.update { it.copy(origenSuggestions = emptyList()) }
        if (field == "destino") _formState.update { it.copy(destinoSuggestions = emptyList()) }
    }
    
    fun saveVueloProgramado(
        isEditing: Boolean,
        vueloDto: VueloProgramadoDto?,
        formData: VueloFormData,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (formData.idAerolinea == null || formData.idOrigen == null || formData.idDestino == null) {
                    throw Exception("Aerolínea, Origen y Destino son obligatorios.")
                }

                val vueloBasePayload = VueloBasePayload(
                    idVuelo = if (isEditing) vueloDto?.vuelo?.id else null,
                    codigoVuelo = formData.codigoVuelo,
                    aerolinea = AerolineaIdPayload(formData.idAerolinea!!),
                    origen = AeropuertoIdPayload(formData.idOrigen!!),
                    destino = AeropuertoIdPayload(formData.idDestino!!),
                    duracionMin = formData.duracionMin ?: 0
                )

                val vueloBaseResponse = if (isEditing && vueloDto?.vuelo?.id != null) {
                    apiService.updateVueloBase(vueloDto.vuelo.id, vueloBasePayload)
                } else {
                    apiService.createVueloBase(vueloBasePayload)
                }

                if (!vueloBaseResponse.isSuccessful || vueloBaseResponse.body() == null) {
                    throw Exception("Error al guardar Vuelo base: ${parseErrorMessage(vueloBaseResponse.errorBody()?.string())}")
                }
                val savedVueloId = vueloBaseResponse.body()!!.id

                val vueloProgramadoPayload = VueloProgramadoPayload(
                    vuelo = VueloIdPayload(savedVueloId),
                    fechaSalida = formData.fechaSalida,
                    horaSalida = formData.horaSalida,
                    fechaLlegada = formData.fechaLlegada,
                    horaLlegada = formData.horaLlegada,
                    precio = formData.precio ?: 0.0,
                    asientosDisponibles = formData.asientosDisponibles ?: 0,
                    asientosTotales = formData.asientosTotales ?: 0,
                    numeroEscalas = formData.numeroEscalas ?: 0
                )

                val finalResponse = if (isEditing && vueloDto != null) {
                    apiService.updateVueloProgramado(vueloDto.id, vueloProgramadoPayload)
                } else {
                    apiService.createVueloProgramado(vueloProgramadoPayload)
                }

                if (finalResponse.isSuccessful) {
                    onResult(true, if (isEditing) "Vuelo actualizado" else "Vuelo creado")
                    loadVuelos()
                } else {
                    throw Exception("Error al guardar Vuelo Programado: ${parseErrorMessage(finalResponse.errorBody()?.string())}")
                }

            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun deleteVuelo(id: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteVueloProgramado(id)
                if (response.isSuccessful) {
                    onResult(true, "Vuelo eliminado con éxito")
                    loadVuelos()
                } else {
                    onResult(false, parseErrorMessage(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error de conexión")
            }
        }
    }
    
    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Error desconocido del servidor."
        return try {
            gson.fromJson(errorBody, ErrorResponse::class.java).message ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}