package com.example.registroflytransportation.model

// VERSIÓN FINAL - El campo 'roles' vuelve a ser una Lista.

/**
 * Petición de registro.
 */
data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val email: String,
    val fono: String,
    val fechaNacimiento: String?,
    val password: String,
    val roles: List<String>,
    val fotoPerfil: String?
)

/**
 * Petición de login.
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Respuesta de autenticación.
 */
data class AuthResponse(
    val message: String,
    val token: String,
    val id: Long,
    val email: String,
    val nombre: String,
    val apellido: String
)

/**
 * Modelo para parsear las respuestas de error del backend.
 */
data class ErrorResponse(
    val message: String?
)

/**
 * Modelo para el perfil de usuario completo.
 */
data class UserProfile(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val email: String,
    val telefono: String?,
    val fechaNacimiento: String?,
    val roles: String?,
    val creadoEn: String?,
    val actualizadoEn: String?,
    val fotoPerfil: String?
)
