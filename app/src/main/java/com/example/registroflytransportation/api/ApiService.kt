package com.example.registroflytransportation.api

import com.example.registroflytransportation.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Autenticación ---
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMyProfile(): Response<UserProfile>

    // --- Búsqueda Pública de Vuelos ---
    @GET("api/vuelos-programados/search") // ¡RUTA CORREGIDA!
    suspend fun buscarVuelos(
        @Query("origen") origen: String,
        @Query("destino") destino: String?, // Destino es opcional
        @Query("fechaSalida") fechaSalida: String
    ): Response<List<VueloProgramadoDto>> // Devuelve el objeto completo

    // --- Administración de Vuelos (CRUD VueloProgramado) ---
    @GET("api/vuelos-programados")
    suspend fun getAllVuelosProgramados(): Response<List<VueloProgramadoDto>>

    @POST("api/vuelos-programados")
    suspend fun createVueloProgramado(@Body payload: VueloProgramadoPayload): Response<VueloProgramadoDto>

    @PUT("api/vuelos-programados/{id}")
    suspend fun updateVueloProgramado(@Path("id") id: Int, @Body payload: VueloProgramadoPayload): Response<VueloProgramadoDto>

    @DELETE("api/vuelos-programados/{id}")
    suspend fun deleteVueloProgramado(@Path("id") id: Int): Response<Void>

    // --- Administración de Vuelos (CRUD Vuelo Base) ---
    @GET("api/vuelos/{id:\\d+}") // Regex para no confundir con otras rutas
    suspend fun getVueloBaseById(@Path("id") id: Int): Response<VueloDetalleDto>

    @POST("api/vuelos")
    suspend fun createVueloBase(@Body payload: VueloBasePayload): Response<VueloDetalleDto>

    @PUT("api/vuelos/{id}")
    suspend fun updateVueloBase(@Path("id") id: Int, @Body payload: VueloBasePayload): Response<VueloDetalleDto>

    // --- APIs de Soporte para el Formulario de Admin ---
    @GET("api/aerolineas")
    suspend fun getAerolineas(): Response<List<Aerolinea>>

    @GET("api/lugares/buscar")
    suspend fun searchAeropuertos(@Query("q") query: String): Response<List<Aeropuerto>>
}
