package com.example.registroflytransportation.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para la petición de búsqueda de vuelos.
 * Esta es la definición única y correcta.
 */
data class BusquedaVueloRequest(
    val origen: String,
    val destino: String,
    val fechaSalida: String,
    val fechaRegreso: String?,
    val numAdultos: Int,
    val numNinos: Int
)

/**
 * Modelo para la respuesta de la búsqueda de vuelos.
 * Esta es la definición única y correcta.
 */
data class Vuelo(
    @SerializedName("id_vuelo_programado")
    val id: Int,
    val aerolinea: String,
    val origen: String,
    val destino: String,
    val fechaSalida: String,
    val fechaLlegada: String,
    val precio: Double,
    val asientosDisponibles: Int
)
