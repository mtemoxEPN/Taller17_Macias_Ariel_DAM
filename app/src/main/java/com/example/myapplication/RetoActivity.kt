package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RetoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                RetoScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RetoScreen() {
    val context = LocalContext.current
    
    // Form fields
    var tipoAccidente by remember { mutableStateOf("Choque") }
    var expandedDropdown by remember { mutableStateOf(false) }
    val tipos = listOf("Choque", "Colisión", "Atropello")
    
    val fechaFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var fecha by remember { mutableStateOf(fechaFormat.format(Date())) }
    var matricula by remember { mutableStateOf("") }
    var nombreConductor by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    // DatePicker Logic
    val calendar = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            fecha = fechaFormat.format(selectedDate.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Camera
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let { uri ->
                capturedBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    android.graphics.BitmapFactory.decodeStream(inputStream)
                }
            }
        }
    }
    fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }

    // Location
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    isLoadingLocation = false
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun requestLocation() {
        if (locationPermissions.allPermissionsGranted) {
            isLoadingLocation = true
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).apply {
                setMinUpdateIntervalMillis(5000L)
                setMaxUpdates(1)
            }.build()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }
    DisposableEffect(Unit) {
        onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }
    }

    // Vibrator function
    fun vibrarTelefono() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(5000)
            }
        }
    }

    // UI Structure
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF6200EA))) { // Fondo Morado Oscuro
        // Encabezado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "Agente de Tránsito",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Registro de Siniestro",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        // Sábana / Cortina de contenido
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Tipo de accidente Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = tipoAccidente,
                        onValueChange = {},
                        label = { Text("Tipo de accidente") },
                        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = "Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        tipos.forEach { seleccion ->
                            DropdownMenuItem(
                                text = { Text(seleccion) },
                                onClick = {
                                    tipoAccidente = seleccion
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // DatePicker TextField
                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha del siniestro") },
                    leadingIcon = { Icon(Icons.Default.List, contentDescription = "Fecha") },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Cambiar Fecha")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = matricula, 
                    onValueChange = { matricula = it }, 
                    label = { Text("Matrícula del auto") }, 
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = "Matricula") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = nombreConductor, 
                    onValueChange = { nombreConductor = it }, 
                    label = { Text("Nombre del conductor") }, 
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = cedula, 
                    onValueChange = { cedula = it }, 
                    label = { Text("Cédula del conductor") }, 
                    leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = "Cedula") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = observaciones, 
                    onValueChange = { observaciones = it }, 
                    label = { Text("Observaciones") }, 
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Observaciones") },
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))

                // Boton Foto
                OutlinedButton(
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            val newUri = createImageFile()
                            imageUri = newUri
                            takePictureLauncher.launch(newUri)
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Camara")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adjuntar Fotografía")
                }
                
                capturedBitmap?.let { bitmap ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(), 
                            contentDescription = "Foto capturada", 
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Boton GPS
                OutlinedButton(
                    onClick = {
                        if (locationPermissions.allPermissionsGranted) {
                            requestLocation()
                        } else {
                            locationPermissions.launchMultiplePermissionRequest()
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth().height(50.dp), 
                    enabled = !isLoadingLocation,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Ubicación")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isLoadingLocation) "Obteniendo Ubicación..." else "Registrar Ubicación GPS")
                }
                
                currentLocation?.let { location ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Place, contentDescription = "Place", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Ubicación guardada", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("Lat: ${location.latitude}", style = MaterialTheme.typography.bodySmall)
                                Text("Lon: ${location.longitude}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                // Boton Guardar
                Button(
                    onClick = {
                        vibrarTelefono()
                        Toast.makeText(context, "Accidente guardado exitosamente", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA)) // Same as header
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Registro", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
