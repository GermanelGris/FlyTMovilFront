package com.example.registroflytransportation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.registroflytransportation.ui.screens.HomePage
import com.example.registroflytransportation.ui.screens.LoginPage
import com.example.registroflytransportation.ui.screens.RegisterPage
import com.example.registroflytransportation.ui.screens.VuelosAdminPage // Importamos la nueva pantalla
import com.example.registroflytransportation.ui.theme.RegistroFlyTransportationTheme
import com.example.registroflytransportation.viewModel.FlightViewModel
import com.example.registroflytransportation.viewModel.UserViewModel
import com.example.registroflytransportation.viewModel.VueloProgramadoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistroFlyTransportationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FlyTApp()
                }
            }
        }
    }
}

@Composable
fun FlyTApp() {
    val userViewModel: UserViewModel = viewModel()
    val flightViewModel: FlightViewModel = viewModel()
    val vueloProgramadoViewModel: VueloProgramadoViewModel = viewModel() // Creamos el nuevo ViewModel

    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    LaunchedEffect(isLoggedIn) {
        currentScreen = if (isLoggedIn) Screen.Home else Screen.Login
    }

    when (currentScreen) {
        is Screen.Login -> {
            LoginPage(
                viewModel = userViewModel,
                onNavigateToRegister = { currentScreen = Screen.Register }
            )
        }

        is Screen.Register -> {
            RegisterPage(
                viewModel = userViewModel,
                onBackToLogin = { currentScreen = Screen.Login }
            )
        }

        is Screen.Home -> {
            if (currentUser != null) {
                HomePage(
                    userProfile = currentUser!!,
                    onLogout = { userViewModel.logout() },
                    flightViewModel = flightViewModel,
                    onNavigateToAdmin = { currentScreen = Screen.AdminVuelos } // Nueva navegación
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        
        is Screen.AdminVuelos -> {
            VuelosAdminPage(
                viewModel = vueloProgramadoViewModel,
                onBack = { currentScreen = Screen.Home } // Para volver a la Home
            )
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Home : Screen()
    object AdminVuelos : Screen() // Nueva pantalla
}