package com.example.registroflytransportation.model

import com.google.gson.annotations.SerializedName

// --- Modelos para el CRUD de Vuelos, inspirados en el frontend de JS --- //

/**
 * Representa una Aerolínea individual para el dropdown.
 */
data class Aerolinea(
    @SerializedName("idAerolinea")
    val id: Int,
    val nombre: String,
    val codigo: String
)

/**
 * Representa un Aeropuerto (Lugar) para el autocompletado.
 */
data class Aeropuerto(
    @SerializedName("idAeropuerto")
    val id: Int,
    @SerializedName("codigoIata")
    val codigo: String,
    val ciudad: String,
    val pais: String,
    val nombre: String
)

// --- PAYLOADS PARA ENVIAR DATOS --- //

/**
 * Wrapper para enviar el ID de la aerolínea con la clave JSON correcta.
 */
data class AerolineaIdPayload(@SerializedName("idAerolinea") val id: Int)

/**
 * Wrapper para enviar el ID del aeropuerto con la clave JSON correcta.
 */
data class AeropuertoIdPayload(@SerializedName("idAeropuerto") val id: Int)

/**
 * Wrapper para enviar el ID del vuelo con la clave JSON correcta.
 */
data class VueloIdPayload(@SerializedName("idVuelo") val id: Int)

/**
 * Payload para CREAR o ACTUALIZAR un Vuelo base.
 */
data class VueloBasePayload(
    val idVuelo: Int? = null,
    val codigoVuelo: String,
    val aerolinea: AerolineaIdPayload,
    val origen: AeropuertoIdPayload,
    val destino: AeropuertoIdPayload,
    val duracionMin: Int
)

/**
 * Respuesta esperada al crear/actualizar un Vuelo base.
 */
data class VueloBaseResponse(
    @SerializedName("idVuelo")
    val id: Int,
    val codigoVuelo: String
)

/**
 * Payload para CREAR o ACTUALIZAR un VueloProgramado.
 */
data class VueloProgramadoPayload(
    val vuelo: VueloIdPayload,
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

/**
 * Data class para manejar el estado del formulario de edición de vuelos.
 */
data class VueloFormData(
    val codigoVuelo: String = "",
    val idAerolinea: Int? = null,
    val idOrigen: Int? = null,
    val origenText: String = "",
    val idDestino: Int? = null,
    val destinoText: String = "",
    val fechaSalida: String = "",
    val horaSalida: String = "",
    val fechaLlegada: String = "",
    val horaLlegada: String = "",
    val duracionMin: Int? = null,
    val precio: Double? = null,
    val asientosDisponibles: Int? = null,
    val asientosTotales: Int? = null,
    val numeroEscalas: Int? = null
)
