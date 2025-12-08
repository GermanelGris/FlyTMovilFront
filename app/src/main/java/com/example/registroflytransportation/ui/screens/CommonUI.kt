package com.example.registroflytransportation.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.example.registroflytransportation.model.Aeropuerto

/**
 * Un OutlinedTextField que muestra una lista de sugerencias de aeropuertos
 * y permite al usuario seleccionar uno.
 *
 * @param value El texto actual del campo.
 * @param onValueChange Callback que se ejecuta cuando el texto cambia.
 * @param label El Composable que se muestra como etiqueta del campo.
 * @param suggestions La lista de aeropuertos a mostrar como sugerencias.
 * @param onSuggestionSelected Callback que se ejecuta cuando se selecciona un aeropuerto.
 * @param isLoading Indica si se debe mostrar un indicador de carga.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    suggestions: List<Aeropuerto>,
    onSuggestionSelected: (Aeropuerto) -> Unit,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    // Este LaunchedEffect asegura que el menú se muestre cuando lleguen las sugerencias
    LaunchedEffect(suggestions) {
        if (suggestions.isNotEmpty()) {
            expanded = true
        }
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = Modifier.menuAnchor().onFocusChanged { 
                if(it.isFocused) expanded = suggestions.isNotEmpty()
            },
            trailingIcon = { if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
        )
        if (suggestions.isNotEmpty()) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                suggestions.forEach { airport ->
                    DropdownMenuItem(
                        text = { Text("${airport.ciudad}, ${airport.pais} (${airport.codigo})") }, 
                        onClick = { 
                            onSuggestionSelected(airport)
                            expanded = false 
                        }
                    )
                }
            }
        }
    }
}