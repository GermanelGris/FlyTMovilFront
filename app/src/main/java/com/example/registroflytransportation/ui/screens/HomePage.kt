package com.example.registroflytransportation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.registroflytransportation.model.UserProfile
import com.example.registroflytransportation.model.VueloProgramadoDto
import com.example.registroflytransportation.ui.theme.*
import com.example.registroflytransportation.viewModel.FlightSearchState
import com.example.registroflytransportation.viewModel.FlightViewModel
import com.example.registroflytransportation.viewModel.SearchFormState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HomePage(
    userProfile: UserProfile,
    onLogout: () -> Unit,
    flightViewModel: FlightViewModel,
    onNavigateToAdmin: () -> Unit
) {
    val searchFormState by flightViewModel.searchFormState.collectAsState()
    val flightSearchState by flightViewModel.flightSearchState.collectAsState()

    // ¡NUEVO! Resetea el estado cuando el perfil del usuario cambia.
    LaunchedEffect(userProfile) {
        flightViewModel.resetState()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(BlueStart, PurpleEnd)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Header(userProfile = userProfile, onLogout = onLogout)
            Spacer(modifier = Modifier.height(16.dp))
            MainTitle()
            Spacer(modifier = Modifier.height(32.dp))
            SearchCard(viewModel = flightViewModel, formState = searchFormState)
            Spacer(modifier = Modifier.height(24.dp))

            if (userProfile.roles?.trim().equals("ADMIN", ignoreCase = true)) {
                AdminButton(onClick = onNavigateToAdmin)
                Spacer(modifier = Modifier.height(24.dp))
            }

            SearchResults(state = flightSearchState)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AdminButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Settings, contentDescription = "Administrar")
        Spacer(modifier = Modifier.width(8.dp))
        Text("ADMINISTRAR VUELOS", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Header(userProfile: UserProfile, onLogout: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!userProfile.fotoPerfil.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(model = userProfile.fotoPerfil),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.size(56.dp).clip(CircleShape).border(2.dp, White, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(White).border(2.dp, PrimaryBlue, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Usuario", modifier = Modifier.size(32.dp), tint = PrimaryBlue)
                }
            }
            Column {
                Text("FLY T", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
                Text("Hola, ${userProfile.nombre ?: "Usuario"}", fontSize = 14.sp, color = White.copy(alpha = 0.9f))
            }
        }
        OutlinedButton(onClick = onLogout, colors = ButtonDefaults.outlinedButtonColors(contentColor = White), border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)) {
            Text("Salir")
        }
    }
}

@Composable
private fun MainTitle() {
     Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Vuela hacia tus sueños", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Descubre el mundo con Fly Transportation. Ofertas exclusivas y el mejor servicio te esperan.", fontSize = 16.sp, color = White.copy(alpha = 0.95f), textAlign = TextAlign.Center, lineHeight = 22.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchCard(viewModel: FlightViewModel, formState: SearchFormState) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        viewModel.onDateChanged(formatter.format(Date(millis)))
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Buscar Vuelos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)

            AutoCompleteTextField(
                value = formState.origenText,
                onValueChange = { viewModel.onOrigenChanged(it) },
                label = { Text("Origen") },
                suggestions = formState.origenSuggestions,
                onSuggestionSelected = { viewModel.onSuggestionSelected(it, "origen") },
                isLoading = formState.isLoadingOrigen
            )

            AutoCompleteTextField(
                value = formState.destinoText,
                onValueChange = { viewModel.onDestinoChanged(it) },
                label = { Text("Destino (Opcional)") },
                suggestions = formState.destinoSuggestions,
                onSuggestionSelected = { viewModel.onSuggestionSelected(it, "destino") },
                isLoading = formState.isLoadingDestino
            )
            
            OutlinedTextField(
                value = formState.fechaSalida,
                onValueChange = { viewModel.onDateChanged(it) },
                label = { Text("Desde la Fecha") },
                placeholder = { Text("dd/MM/yyyy") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { 
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Abrir calendario") 
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { viewModel.searchFlights() }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar Vuelos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SearchResults(state: FlightSearchState) {
     Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        when (state) {
            is FlightSearchState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = White)
                }
            }
            is FlightSearchState.Error -> {
                Card(colors = CardDefaults.cardColors(containerColor = ErrorRed), modifier = Modifier.fillMaxWidth()) {
                    Text(state.message, color = White, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                }
            }
            is FlightSearchState.Success -> {
                Text("Resultados de la Búsqueda", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White, modifier = Modifier.padding(bottom = 16.dp))
                Box(modifier = Modifier.heightIn(max = 600.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.vuelos) { vueloDto ->
                            FlightItem(vueloDto)
                        }
                    }
                }
            }
            is FlightSearchState.Idle -> {}
        }
    }
}

@Composable
private fun FlightItem(vueloDto: VueloProgramadoDto) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${vueloDto.vuelo.origen.ciudad} -> ${vueloDto.vuelo.destino.ciudad}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(vueloDto.vuelo.aerolinea.nombre, style = MaterialTheme.typography.bodyLarge, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Salida: ${vueloDto.fechaSalida} ${vueloDto.horaSalida.substring(0,5)}", style = MaterialTheme.typography.bodySmall)
                Text("Llegada: ${vueloDto.fechaLlegada} ${vueloDto.horaLlegada.substring(0,5)}", style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Asientos: ${vueloDto.asientosDisponibles}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(currencyFormat.format(vueloDto.precio), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
            }
        }
    }
}