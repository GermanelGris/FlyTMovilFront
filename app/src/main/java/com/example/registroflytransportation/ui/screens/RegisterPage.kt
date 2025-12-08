package com.example.registroflytransportation.ui.screens

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.registroflytransportation.ui.theme.*
import com.example.registroflytransportation.viewModel.RegisterState
import com.example.registroflytransportation.viewModel.UserViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(
    viewModel: UserViewModel,
    onBackToLogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    var fechaNacimiento by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var savedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("CLIENTE", "ADMIN")
    var selectedRole by remember { mutableStateOf(roles[0]) }

    val context = LocalContext.current
    val registerState by viewModel.registerState.collectAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.resetRegisterState()
    }

    fun convertToApiFormat(displayDate: String): String? {
        return try {
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = displayFormat.parse(displayDate)
            apiFormat.format(date!!)
        } catch (e: Exception) {
            null
        }
    }

    fun createImageUriInGallery(): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "FLYT_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FlyT")
                }
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } catch (e: Exception) {
            Log.e("RegisterPage", "Error creating gallery URI", e)
            null
        }
    }

    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success && tempUri != null) {
            photoUri = tempUri
            savedPhotoUri = tempUri
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            tempUri = createImageUriInGallery()
            if (tempUri != null) {
                cameraLauncher.launch(tempUri!!)
            }
        } else {
            errorMessage = "Se necesitan permisos de cámara y almacenamiento para la foto."
        }
    }

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Error) {
            errorMessage = (registerState as RegisterState.Error).message
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        fechaNacimiento = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (registerState is RegisterState.Success) {
        SuccessDialog(onDismiss = onBackToLogin)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(BlueStart, PurpleEnd)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Crear Cuenta", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = White)
            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                    val permissionsToRequest = remember {
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                                arrayOf(Manifest.permission.CAMERA)
                            else ->
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }

                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.size(140.dp).clip(CircleShape).border(4.dp, White, CircleShape).clickable {
                                permissionLauncher.launch(permissionsToRequest)
                            },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(140.dp).clip(CircleShape).background(White).border(4.dp, PrimaryBlue, CircleShape).clickable {
                                permissionLauncher.launch(permissionsToRequest)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = "Tomar foto", modifier = Modifier.size(48.dp), tint = PrimaryBlue)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Tomar Foto", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PrimaryBlue)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (photoUri == null) "Toca para tomar tu foto" else "Foto guardada en galería", fontSize = 12.sp, color = White.copy(alpha = 0.9f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it; errorMessage = null }, label = { Text("Nombre") }, modifier = Modifier.weight(1f).height(60.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White), shape = RoundedCornerShape(8.dp), singleLine = true)
                OutlinedTextField(value = apellido, onValueChange = { apellido = it; errorMessage = null }, label = { Text("Apellido") }, modifier = Modifier.weight(1f).height(60.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White), shape = RoundedCornerShape(8.dp), singleLine = true)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it; errorMessage = null }, label = { Text("Correo Electrónico") }, placeholder = { Text("ejemplo@correo.com") }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White), shape = RoundedCornerShape(8.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = telefono, onValueChange = { telefono = it; errorMessage = null }, label = { Text("Teléfono") }, modifier = Modifier.weight(1f).height(60.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White), shape = RoundedCornerShape(8.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                OutlinedTextField(value = fechaNacimiento, onValueChange = { }, readOnly = true, label = { Text("Fecha Nac.") }, placeholder = { Text("DD/MM/YYYY") }, trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = PrimaryBlue) } }, modifier = Modifier.weight(1f).height(60.dp).clickable { showDatePicker = true }, enabled = false, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White, disabledContainerColor = White, disabledBorderColor = White, disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant), shape = RoundedCornerShape(8.dp), singleLine = true)
            }
            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = selectedRole, onValueChange = {}, readOnly = true, label = { Text("Rol de usuario") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth().height(60.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White), shape = RoundedCornerShape(8.dp))
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(White)) {
                    roles.forEach { role ->
                        DropdownMenuItem(text = { Text(text = role) }, onClick = { selectedRole = role; expanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = password, onValueChange = { password = it; errorMessage = null }, label = { Text("Contraseña") }, modifier = Modifier.weight(1f).height(60.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White), shape = RoundedCornerShape(8.dp), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true)
                OutlinedTextField(value = confirmarPassword, onValueChange = { confirmarPassword = it; errorMessage = null }, label = { Text("Confirmar") }, modifier = Modifier.weight(1f).height(60.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = White), shape = RoundedCornerShape(8.dp), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true)
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.9f)), shape = RoundedCornerShape(8.dp)) {
                    Text(text = errorMessage ?: "", color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onBackToLogin, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = White), border = BorderStroke(1.dp, White)) {
                    Text("Volver", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (nombre.isBlank() || apellido.isBlank() || email.isBlank() || telefono.isBlank() || fechaNacimiento.isBlank() || password.isBlank()) {
                            errorMessage = "Todos los campos son obligatorios"
                            return@Button
                        }
                        if (password != confirmarPassword) {
                            errorMessage = "Las contraseñas no coinciden"
                            return@Button
                        }
                        val fechaApi = convertToApiFormat(fechaNacimiento)
                        if (fechaApi == null) {
                            errorMessage = "Formato de fecha inválido"
                            return@Button
                        }
                        
                        viewModel.registerNewUser(nombre, apellido, email, telefono, fechaApi, password, selectedRole, savedPhotoUri)
                    },
                    enabled = registerState !is RegisterState.Loading,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (registerState is RegisterState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Black)
                    } else {
                        Text(text = "Crear Cuenta", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(initialValue = 0f, targetValue = 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { value, _ -> scale = value }
        delay(2000)
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp).scale(scale), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Éxito", modifier = Modifier.size(80.dp), tint = SuccessGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "¡Registro Exitoso!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Tu cuenta ha sido creada correctamente", fontSize = 14.sp, color = DarkGray, textAlign = TextAlign.Center)
            }
        }
    }
}