package com.example.registroflytransportation.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos DEFINITIVO para la respuesta de /api/vuelos-programados.
 * Refleja 100% la estructura anidada del JSON del backend.
 */
data class VueloProgramadoDto(
    @SerializedName("idVueloProg")
    val id: Int,
    val vuelo: VueloDetalleDto,
    val fechaSalida: String,
    val horaSalida: String,
    val fechaLlegada: String,
    val horaLlegada: String,
    val precio: Double,
    @SerializedName("asientosDisp")
    val asientosDisponibles: Int,
    val asientosTotales: Int,
    val numeroEscalas: Int
)

data class VueloDetalleDto(
    @SerializedName("idVuelo")
    val id: Int,
    val codigoVuelo: String,
    val aerolinea: AerolineaDto,
    val origen: AeropuertoDto,
    val destino: AeropuertoDto,
    val duracionMin: Int
)

data class AerolineaDto(
    @SerializedName("idAerolinea")
    val id: Int,
    val nombre: String,
    val codigo: String
)

data class AeropuertoDto(
    @SerializedName("idAeropuerto")
    val id: Int,
    @SerializedName("codigoIata")
    val codigo: String,
    val nombre: String,
    val ciudad: String,
    val pais: String
)
