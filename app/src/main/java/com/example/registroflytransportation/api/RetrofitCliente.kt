package com.example.registroflytransportation.api

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Interceptor que lee el token desde DataStore (de forma asíncrona) y lo añade
 * a las cabeceras de las peticiones.
 */
class AuthInterceptor(private val context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        // runBlocking es necesario porque el interceptor es síncrono,
        // pero la lectura de DataStore es asíncrona.
        val token = runBlocking {
            sessionManager.getAuthToken() // Usamos la función del SessionManager
        }

        val requestBuilder = chain.request().newBuilder()

        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}

// El objeto RetrofitClient no necesita cambios, ya que usa el AuthInterceptor actualizado.
object RetrofitClient {
    // CORREGIDO: Se usa 10.0.2.2 para que el emulador pueda conectarse al localhost del ordenador.
    private const val BASE_URL = "http://192.168.1.134:8090/"
    private var retrofitInstance: ApiService? = null

    fun getInstance(context: Context): ApiService {
        return retrofitInstance ?: synchronized(this) {
            retrofitInstance ?: buildApiService(context).also {
                retrofitInstance = it
            }
        }
    }

    private fun buildApiService(context: Context): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthInterceptor(context.applicationContext)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}