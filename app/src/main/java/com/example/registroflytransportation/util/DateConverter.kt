package com.example.registroflytransportation.util

import java.text.SimpleDateFormat
import java.util.Locale

object DateConverter {

    /**
     * Convierte una fecha en formato de visualización (ej: "dd/MM/yyyy") 
     * al formato que espera la API (ej: "yyyy-MM-dd").
     * 
     * @param displayDate La fecha en formato "dd/MM/yyyy".
     * @return La fecha en formato "yyyy-MM-dd" o null si el formato de entrada es inválido.
     */
    fun convertToApiFormat(displayDate: String): String? {
        if (displayDate.isBlank()) return null
        return try {
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = displayFormat.parse(displayDate)
            apiFormat.format(date!!)
        } catch (e: Exception) {
            null
        }
    }
}
