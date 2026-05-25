package com.example.cloty_apoderado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cloty_apoderado.ui.ClotyViewModel
import com.example.cloty_apoderado.ui.components.MessageBanner

@Composable
fun CuentaScreen(viewModel: ClotyViewModel, contentPadding: PaddingValues, onLogout: () -> Unit) {
    var actual by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    val nombre by viewModel.nombreUsuario.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(contentPadding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Mi cuenta", style = MaterialTheme.typography.titleLarge)
        nombre?.let { Text("Usuario: $it", style = MaterialTheme.typography.bodyMedium) }

        Text("Cambiar contraseña", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            actual, { actual = it },
            label = { Text("Contraseña actual") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            nueva, { nueva = it },
            label = { Text("Nueva contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        OutlinedTextField(
            confirmar, { confirmar = it },
            label = { Text("Confirmar nueva") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        MessageBanner(error, true)
        MessageBanner(message, false)
        Button(
            onClick = {
                if (nueva != confirmar) return@Button
                viewModel.cambiarContrasena(actual, nueva)
                actual = ""
                nueva = ""
                confirmar = ""
            },
            enabled = !loading && actual.isNotBlank() && nueva.length >= 4 && nueva == confirmar,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar contraseña") }
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
    }
}
