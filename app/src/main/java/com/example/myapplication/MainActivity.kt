package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComponentesBasicos(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComponentesBasicos(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Estado para los componentes
    var texto by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var switchActivo by remember { mutableStateOf(false) }
    var contador by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Text - Texto simple y estilizado
        Text(
            text = "Componentes Básicos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 2. TEXTFIELD - Campo de entrada
        TextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Escribe tu nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Mostrar lo que se escribió
        if (texto.isNotEmpty()) {
            Text(text = "Hola, $texto!", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 3. BUTTON - Botón
        Button(onClick = { contador++ }) {
            Text("Presionado: $contador veces")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón con borde (Outlined)
        OutlinedButton(onClick = {contador = 0}) {
            Text("Reiniciar contador")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 4. ROW - Fila horizontal con Checkbox y Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
                Text("Acepto")
            }

            // SWITCH
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Modo Oscuro")
                Switch(
                    checked = switchActivo,
                    onCheckedChange = { switchActivo = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. CARD - Tarjeta
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Esta es una Card", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Las card son útiles para agrupar contenido relacionado.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            context.startActivity(Intent(context, CameraActivity::class.java))
        }) {
            Text("Abrir Cámara")
        }

        // 6. BOX - Superposición
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {}
            Text("Centrado", fontWeight = FontWeight.Bold)

        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Texto con color condicional
        Text(
            text = if (switchActivo) "Switch está ON" else "Switch está OFF",
            color = if (switchActivo) Color.Green else Color.Red,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // BOTÓN DEL RETO
        Button(
            onClick = {
                context.startActivity(Intent(context, RetoActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Agente de Tránsito", fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        ComponentesBasicos()
    }
}