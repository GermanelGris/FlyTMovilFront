package com.example.registroflytransportation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.registroflytransportation.model.*
import com.example.registroflytransportation.viewModel.AdminFormState
import com.example.registroflytransportation.viewModel.VueloProgramadoViewModel
import com.example.registroflytransportation.viewModel.VuelosAdminState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VuelosAdminPage(
    viewModel: VueloProgramadoViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.vuelosState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<VueloProgramadoDto?>(null) }
    var selectedVueloDto by remember { mutableStateOf<VueloProgramadoDto?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Administrar Vuelos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                selectedVueloDto = null
                showEditDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Vuelo")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val currentState = state) {
                is VuelosAdminState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is VuelosAdminState.Error -> Text(currentState.message, color = Color.Red, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                is VuelosAdminState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(currentState.vuelos) { vueloDto ->
                            VueloProgramadoItem(
                                vueloDto = vueloDto,
                                onEdit = { 
                                    selectedVueloDto = it
                                    showEditDialog = true
                                },
                                onDelete = { showDeleteDialog = it }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        VueloEditDialog(
            vueloDto = selectedVueloDto,
            formState = formState,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            onConfirm = { isEditing, formData ->
                viewModel.saveVueloProgramado(
                    isEditing = isEditing,
                    vueloDto = selectedVueloDto,
                    formData = formData
                ) { success, message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message ?: if (success) "Operación exitosa" else "Ocurrió un error")
                    }
                }
                showEditDialog = false
            }
        )
    }
    
    showDeleteDialog?.let { vuelo ->
        DeleteConfirmationDialog(
            vueloDto = vuelo,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteVuelo(vuelo.id) { success, message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message ?: if (success) "Vuelo eliminado" else "Error al eliminar")
                    }
                }
                showDeleteDialog = null
            }
        )
    }
}

@Composable
private fun VueloProgramadoItem(vueloDto: VueloProgramadoDto, onEdit: (VueloProgramadoDto) -> Unit, onDelete: (VueloProgramadoDto) -> Unit) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${vueloDto.vuelo.origen.ciudad} -> ${vueloDto.vuelo.destino.ciudad}", style = MaterialTheme.typography.titleLarge)
            Text(vueloDto.vuelo.aerolinea.nombre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Salida: ${vueloDto.fechaSalida} ${vueloDto.horaSalida}", style = MaterialTheme.typography.bodySmall)
            Text("Llegada: ${vueloDto.fechaLlegada} ${vueloDto.horaLlegada}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Precio: ${currencyFormat.format(vueloDto.precio)}", fontWeight = FontWeight.Bold)
                Text("Asientos: ${vueloDto.asientosDisponibles}")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { onEdit(vueloDto) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onDelete(vueloDto) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(vueloDto: VueloProgramadoDto, onDismiss: () -> Unit, onConfirm: () -> Unit) {
     AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar el vuelo de ${vueloDto.vuelo.origen.ciudad} a ${vueloDto.vuelo.destino.ciudad}? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VueloEditDialog(
    vueloDto: VueloProgramadoDto?,
    formState: AdminFormState,
    viewModel: VueloProgramadoViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, VueloFormData) -> Unit
) {
    var formData by remember {
        mutableStateOf(vueloDto?.let { 
            VueloFormData(
                codigoVuelo = it.vuelo.codigoVuelo,
                idAerolinea = it.vuelo.aerolinea.id,
                idOrigen = it.vuelo.origen.id,
                origenText = "${it.vuelo.origen.ciudad}, ${it.vuelo.origen.pais} (${it.vuelo.origen.codigo})",
                idDestino = it.vuelo.destino.id,
                destinoText = "${it.vuelo.destino.ciudad}, ${it.vuelo.destino.pais} (${it.vuelo.destino.codigo})",
                fechaSalida = it.fechaSalida,
                horaSalida = it.horaSalida,
                fechaLlegada = it.fechaLlegada,
                horaLlegada = it.horaLlegada,
                duracionMin = it.vuelo.duracionMin,
                precio = it.precio,
                asientosDisponibles = it.asientosDisponibles,
                asientosTotales = it.asientosTotales,
                numeroEscalas = it.numeroEscalas
            )
        } ?: VueloFormData())
    }

    var aerolineaExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf<Boolean?>(null) } // true: Salida, false: Llegada
    val datePickerState = rememberDatePickerState()

    if (showDatePicker != null) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        val dateStr = formatter.format(Date(millis))
                        if (showDatePicker == true) {
                            formData = formData.copy(fechaSalida = dateStr)
                        } else {
                            formData = formData.copy(fechaLlegada = dateStr)
                        }
                    }
                    showDatePicker = null
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = null }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(vertical = 32.dp)) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(if (vueloDto == null) "Nuevo Vuelo" else "Editar Vuelo", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(value = formData.codigoVuelo, onValueChange = { formData = formData.copy(codigoVuelo = it) }, label = { Text("Código de Vuelo") })

                ExposedDropdownMenuBox(expanded = aerolineaExpanded, onExpandedChange = { aerolineaExpanded = !aerolineaExpanded }) {
                     OutlinedTextField(
                        value = formState.aerolineas.find { it.id == formData.idAerolinea }?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Aerolínea") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aerolineaExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = aerolineaExpanded, onDismissRequest = { aerolineaExpanded = false }) {
                        formState.aerolineas.forEach { aerolinea ->
                            DropdownMenuItem(
                                text = { Text(aerolinea.nombre) }, 
                                onClick = { 
                                    formData = formData.copy(idAerolinea = aerolinea.id)
                                    aerolineaExpanded = false
                                }
                            )
                        }
                    }
                }

                AutoCompleteTextField(
                    value = formData.origenText,
                    onValueChange = { 
                        formData = formData.copy(origenText = it, idOrigen = null)
                        viewModel.searchAirports(it, "origen")
                    },
                    label = { Text("Origen") },
                    suggestions = formState.origenSuggestions,
                    onSuggestionSelected = { airport ->
                        formData = formData.copy(idOrigen = airport.id, origenText = "${airport.ciudad}, ${airport.pais} (${airport.codigo})")
                        viewModel.clearSuggestions("origen")
                    },
                    isLoading = formState.isLoadingOrigen
                )

                 AutoCompleteTextField(
                    value = formData.destinoText,
                    onValueChange = { 
                        formData = formData.copy(destinoText = it, idDestino = null)
                        viewModel.searchAirports(it, "destino")
                    },
                    label = { Text("Destino") },
                    suggestions = formState.destinoSuggestions,
                    onSuggestionSelected = { airport ->
                        formData = formData.copy(idDestino = airport.id, destinoText = "${airport.ciudad}, ${airport.pais} (${airport.codigo})")
                        viewModel.clearSuggestions("destino")
                    },
                    isLoading = formState.isLoadingDestino
                )
                
                OutlinedTextField(
                    value = formData.fechaSalida, 
                    onValueChange = {},
                    label = { Text("Fecha Salida") },
                    readOnly = true,
                    trailingIcon = { 
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar Fecha de Salida")
                        }
                    }
                )
                OutlinedTextField(value = formData.horaSalida, onValueChange = { formData = formData.copy(horaSalida = it) }, label = { Text("Hora Salida (HH:mm:ss)") })
                OutlinedTextField(
                    value = formData.fechaLlegada, 
                    onValueChange = {},
                    label = { Text("Fecha Llegada") },
                    readOnly = true,
                    trailingIcon = { 
                        IconButton(onClick = { showDatePicker = false }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar Fecha de Llegada")
                        }
                    }
                )
                OutlinedTextField(value = formData.horaLlegada, onValueChange = { formData = formData.copy(horaLlegada = it) }, label = { Text("Hora Llegada (HH:mm:ss)") })
                OutlinedTextField(value = formData.duracionMin?.toString() ?: "", onValueChange = { formData = formData.copy(duracionMin = it.toIntOrNull()) }, label = { Text("Duración (minutos)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = formData.precio?.toString() ?: "", onValueChange = { formData = formData.copy(precio = it.toDoubleOrNull()) }, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = formData.asientosDisponibles?.toString() ?: "", onValueChange = { formData = formData.copy(asientosDisponibles = it.toIntOrNull()) }, label = { Text("Asientos Disponibles") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = formData.asientosTotales?.toString() ?: "", onValueChange = { formData = formData.copy(asientosTotales = it.toIntOrNull()) }, label = { Text("Asientos Totales") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = formData.numeroEscalas?.toString() ?: "", onValueChange = { formData = formData.copy(numeroEscalas = it.toIntOrNull()) }, label = { Text("Nº de Escalas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = { onConfirm(vueloDto != null, formData) }) { Text("Guardar") }
                }
            }
        }
    }
}
