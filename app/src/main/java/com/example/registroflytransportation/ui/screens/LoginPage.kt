package com.example.registroflytransportation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.registroflytransportation.R
import com.example.registroflytransportation.model.LoginRequest
import com.example.registroflytransportation.ui.theme.*
import com.example.registroflytransportation.viewModel.LoginState
import com.example.registroflytransportation.viewModel.UserViewModel

@Composable
fun LoginPage(
    viewModel: UserViewModel,
    onNavigateToRegister: () -> Unit // onLoginSuccess ya no es necesario
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()

    // Ya no se necesita el LaunchedEffect para navegar, MainActivity lo hace.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(BlueStart, PurpleEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier.size(180.dp).background(White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flyt),
                    contentDescription = "Logo FLY T",
                    modifier = Modifier.size(160.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Campo Correo
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo Electrónico") },
                placeholder = { Text("admin@flyt.com") },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("123456") },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White),
                shape = RoundedCornerShape(8.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // Mensaje de error y estado de carga
            when (val state = loginState) {
                is LoginState.Loading -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(color = White)
                }
                is LoginState.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Iniciar Sesión
            Button(
                onClick = {
                    if (correo.isNotBlank() && password.isNotBlank()) {
                        viewModel.loginUser(LoginRequest(correo, password))
                    }
                },
                modifier = Modifier.width(200.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Black),
                shape = RoundedCornerShape(8.dp),
                enabled = loginState !is LoginState.Loading
            ) {
                Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Registrarse
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.width(200.dp).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
            ) {
                Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}