package com.example.registroflytransportation.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroflytransportation.api.RetrofitClient
import com.example.registroflytransportation.api.SessionManager
import com.example.registroflytransportation.model.AuthResponse
import com.example.registroflytransportation.model.ErrorResponse
import com.example.registroflytransportation.model.LoginRequest
import com.example.registroflytransportation.model.RegisterRequest
import com.example.registroflytransportation.model.UserProfile
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: AuthResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getInstance(application.applicationContext)
    private val sessionManager = SessionManager(application.applicationContext)
    private val gson = Gson()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val token = sessionManager.getAuthToken()
            if (token != null) {
                if (!fetchMyProfile()) {
                    logout()
                }
            }
        }
    }

    private suspend fun fetchMyProfile(): Boolean {
        return try {
            val response = apiService.getMyProfile()
            if (response.isSuccessful && response.body() != null) {
                _currentUser.value = response.body()
                _isLoggedIn.value = true
                true
            } else {
                Log.e("UserViewModel", "Error al obtener el perfil. Código: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Fallo de conexión al obtener el perfil: ${e.message}")
            false
        }
    }

    fun registerNewUser(
        nombre: String, apellido: String, email: String, fono: String,
        fechaNacimiento: String, password: String, rol: String, imageUri: Uri?
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val imagePath = imageUri?.toString()
            
            // ¡CORREGIDO! Envolvemos el rol en una lista para que coincida con el modelo.
            val request = RegisterRequest(nombre, apellido, email, fono, fechaNacimiento, password, listOf(rol), imagePath)

            try {
                val response = apiService.registerUser(request)
                if (response.isSuccessful) {
                    _registerState.value = RegisterState.Success
                } else {
                    val errorBody = response.errorBody()?.string()
                    val statusCode = response.code()
                    val errorMsg = if (!errorBody.isNullOrBlank()) parseErrorMessage(errorBody) else "Error del servidor (Código: $statusCode)"
                    _registerState.value = RegisterState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Excepción desconocida")
            }
        }
    }

    fun loginUser(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = apiService.loginUser(request)
                if (response.isSuccessful && response.body() != null) {
                    sessionManager.saveAuthToken(response.body()!!.token)
                    if (fetchMyProfile()) {
                        _loginState.value = LoginState.Success(response.body()!!)
                    } else {
                        logout()
                        _loginState.value = LoginState.Error("Login correcto, pero no se pudo cargar el perfil.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val statusCode = response.code()
                    val errorMsg = if (!errorBody.isNullOrBlank()) parseErrorMessage(errorBody) else "Error del servidor (Código: $statusCode)"
                    _loginState.value = LoginState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Excepción desconocida")
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _isLoggedIn.value = false
            _currentUser.value = null
        }
    }

    private fun parseErrorMessage(errorBody: String): String {
        return try {
            gson.fromJson(errorBody, ErrorResponse::class.java).message ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}