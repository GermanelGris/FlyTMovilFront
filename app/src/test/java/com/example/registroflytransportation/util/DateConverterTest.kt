package com.example.registroflytransportation.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateConverterTest {

    @Test
    fun `convertToApiFormat con fecha válida devuelve el formato correcto`() {
        // Preparación (Arrange)
        val displayDate = "07/12/2025"
        val expectedApiDate = "2025-12-07"

        // Actuación (Act)
        val result = DateConverter.convertToApiFormat(displayDate)

        // Afirmación (Assert)
        assertEquals(expectedApiDate, result)
    }

    @Test
    fun `convertToApiFormat con fecha inválida devuelve null`() {
        // Preparación
        val invalidDate = "esto-no-es-una-fecha"

        // Actuación
        val result = DateConverter.convertToApiFormat(invalidDate)

        // Afirmación
        assertNull(result)
    }

    @Test
    fun `convertToApiFormat con cadena vacía devuelve null`() {
        // Preparación
        val emptyDate = ""

        // Actuación
        val result = DateConverter.convertToApiFormat(emptyDate)

        // Afirmación
        assertNull(result)
    }

    @Test
    fun `convertToApiFormat con formato incorrecto devuelve null`() {
        // Preparación
        val wrongFormatDate = "2025-12-07"

        // Actuación
        val result = DateConverter.convertToApiFormat(wrongFormatDate)

        // Afirmación
        assertNull(result)
    }
}
