package com.example.registroflytransportation.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * Representa la entidad VueloProgramado del backend.
 * Se asumen los campos más comunes para un vuelo.
 */
data class VueloProgramado(
    val id: Int? = null,
    val aerolinea: String,
    val origen: String,
    val destino: String,
    
    @SerializedName("fecha_salida") // Aseguramos que coincida con el JSON si usa snake_case
    val fechaSalida: String, // Formato "YYYY-MM-DDTHH:mm:ss"
    
    @SerializedName("fecha_llegada")
    val fechaLlegada: String,
    
    val precio: Double,
    
    @SerializedName("asientos_disponibles")
    val asientosDisponibles: Int
)
