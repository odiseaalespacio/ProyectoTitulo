package com.example.cloty_colegio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cloty_colegio.ui.ClotyViewModel
import com.example.cloty_colegio.ui.components.MessageBanner

@Composable
fun ActivarCuentaScreen(viewModel: ClotyViewModel, onBackToLogin: () -> Unit) {
    var rut by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    val passwordsMatch = password == confirmPassword
    val formValid = rut.isNotBlank() && email.isNotBlank() && telefono.isNotBlank()
            && password.length >= 4 && passwordsMatch

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Activar cuenta", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Ingrese el RUT del colegio registrado por el administrador",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            rut, { rut = it },
            label = { Text("RUT del colegio") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            email, { email = it },
            label = { Text("Email del colegio") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            telefono, { telefono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            password, { password = it },
            label = { Text("Contraseña (mín. 4 caracteres)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            confirmPassword, { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPassword.isNotEmpty() && !passwordsMatch
        )
        if (confirmPassword.isNotEmpty() && !passwordsMatch) {
            Text(
                "Las contraseñas no coinciden",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(16.dp))
        MessageBanner(error, true)
        MessageBanner(message, false, Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.activarCuenta(rut.trim(), email.trim(), telefono.trim(), password) },
            enabled = !loading && formValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Activando…" else "Activar cuenta")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBackToLogin) {
            Text("Ya tengo cuenta — Iniciar sesión")
        }
    }
}
