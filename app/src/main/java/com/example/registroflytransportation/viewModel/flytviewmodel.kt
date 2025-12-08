package com.example.registroflytransportation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroflytransportation.model.Users
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlyTViewModel : ViewModel() {

    // Estado de autenticación
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Usuario actual
    private val _currentUser = MutableStateFlow<Users?>(null)
    val currentUser: StateFlow<Users?> = _currentUser.asStateFlow()

    // Lista de usuarios registrados
    private val _registeredUsers = MutableStateFlow<List<Users>>(emptyList())
    val registeredUsers: StateFlow<List<Users>> = _registeredUsers.asStateFlow()

    // Estado del formulario de búsqueda de vuelos
    private val _searchFlightState = MutableStateFlow(SearchFlightForm())
    val searchFlightState: StateFlow<SearchFlightForm> = _searchFlightState.asStateFlow()

    // Mensaje de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Estado de registro exitoso
    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    // Inicializar con usuario admin por defecto
    init {
        _registeredUsers.value = listOf(
            Users(
                name = "Admin",
                lastName = "Sistema",
                email = "admin@flyt.com",
                password = "123456",
                phoneNumber = "",
                birthday = null
            )
        )
    }

    // Validar email
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    // Login
    fun login(username: String, password: String): Boolean {
        // Buscar usuario por nombre (name) en la lista de registrados
        val user = _registeredUsers.value.find {
            it.name.equals(username, ignoreCase = true) && it.password == password
        }

        // También permitir login con username "admin"
        val isAdmin = username.equals("admin", ignoreCase = true) && password == "123456"

        return if (user != null || isAdmin) {
            _isLoggedIn.value = true
            _currentUser.value = user ?: _registeredUsers.value.first()
            _errorMessage.value = null
            true
        } else {
            _errorMessage.value = "Usuario o contraseña incorrectos"
            false
        }
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            _isLoggedIn.value = false
            _currentUser.value = null
            _searchFlightState.value = SearchFlightForm()
            _errorMessage.value = null
        }
    }

    // Validar y registrar nuevo usuario
    fun validateAndRegisterUser(
        name: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        birthday: String,
        password: String,
        confirmPassword: String,
        photoUri: String
    ): Boolean {
        // Validaciones
        when {
            name.isBlank() -> {
                _errorMessage.value = "El nombre es obligatorio"
                return false
            }
            lastName.isBlank() -> {
                _errorMessage.value = "El apellido es obligatorio"
                return false
            }
            email.isBlank() -> {
                _errorMessage.value = "El correo electrónico es obligatorio"
                return false
            }
            !isValidEmail(email) -> {
                _errorMessage.value = "El correo electrónico no es válido. Ejemplo: usuario@dominio.com"
                return false
            }
            _registeredUsers.value.any { it.email.equals(email, ignoreCase = true) } -> {
                _errorMessage.value = "El correo electrónico ya está registrado"
                return false
            }
            phoneNumber.isBlank() -> {
                _errorMessage.value = "El teléfono es obligatorio"
                return false
            }
            birthday.isBlank() -> {
                _errorMessage.value = "La fecha de nacimiento es obligatoria"
                return false
            }
            password.isBlank() -> {
                _errorMessage.value = "La contraseña es obligatoria"
                return false
            }
            password.length < 6 -> {
                _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
                return false
            }
            password != confirmPassword -> {
                _errorMessage.value = "Las contraseñas no coinciden"
                return false
            }
        }

        // Intentar parsear la fecha
        val parsedDate: Date? = try {
            // Asumiendo formato dd/MM/yyyy que suele venir del UI viejo o input manual
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(birthday)
        } catch (e: Exception) {
             try {
                 // Intento fallback por si viene en formato API
                 SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(birthday)
             } catch (e2: Exception) {
                 null
             }
        }

        // Si todas las validaciones pasan, crear el usuario
        val newUser = Users(
            name = name,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            birthday = parsedDate,
            password = password,
            photoUri = photoUri
        )

        viewModelScope.launch {
            _registeredUsers.value = _registeredUsers.value.toMutableList().apply {
                add(newUser)
            }
            _errorMessage.value = null
            _registrationSuccess.value = true
        }
        return true
    }

    // Registrar nuevo usuario (método antiguo mantenido para compatibilidad)
    fun registerUser(user: Users): Boolean {
        // Validar que el email no esté registrado
        if (_registeredUsers.value.any { it.email == user.email }) {
            _errorMessage.value = "El correo electrónico ya está registrado"
            return false
        }

        // Validar campos obligatorios
        if (user.name.isBlank() || user.lastName.isBlank() ||
            user.email.isBlank() || user.password.isBlank()) {
            _errorMessage.value = "Por favor completa todos los campos obligatorios"
            return false
        }

        viewModelScope.launch {
            _registeredUsers.value = _registeredUsers.value.toMutableList().apply {
                add(user)
            }
            _errorMessage.value = null
            _registrationSuccess.value = true
        }
        return true
    }

    // Actualizar campo del formulario de búsqueda
    fun updateSearchField(field: String, value: String) {
        _searchFlightState.value = when (field) {
            "origen" -> _searchFlightState.value.copy(origen = value)
            "destino" -> _searchFlightState.value.copy(destino = value)
            "fechaSalida" -> _searchFlightState.value.copy(fechaSalida = value)
            "fechaRegreso" -> _searchFlightState.value.copy(fechaRegreso = value)
            "numAdultos" -> _searchFlightState.value.copy(numAdultos = value)
            "numNinos" -> _searchFlightState.value.copy(numNinos = value)
            else -> _searchFlightState.value
        }
    }

    // Buscar vuelos
    fun searchFlights(): Boolean {
        val form = _searchFlightState.value

        if (form.origen.isBlank() || form.destino.isBlank() ||
            form.fechaSalida.isBlank()) {
            _errorMessage.value = "Por favor completa origen, destino y fecha de salida"
            return false
        }

        // Aquí implementarías la lógica de búsqueda de vuelos
        // Por ahora solo validamos
        _errorMessage.value = null
        return true
    }

    // Limpiar mensaje de error
    fun clearError() {
        _errorMessage.value = null
    }

    // Limpiar estado de registro exitoso
    fun clearRegistrationSuccess() {
        _registrationSuccess.value = false
    }

    // Limpiar formulario de búsqueda
    fun clearSearchForm() {
        _searchFlightState.value = SearchFlightForm()
    }
}

// Data class para el formulario de búsqueda de vuelos
data class SearchFlightForm(
    val origen: String = "",
    val destino: String = "",
    val fechaSalida: String = "",
    val fechaRegreso: String = "",
    val numAdultos: String = "1",
    val numNinos: String = "0"
)