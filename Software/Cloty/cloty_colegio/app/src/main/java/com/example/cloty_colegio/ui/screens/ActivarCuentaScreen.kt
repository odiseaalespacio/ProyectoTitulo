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
import com.example.cloty_colegio.ui.components.EmailTextField
import com.example.cloty_colegio.ui.components.MessageBanner
import com.example.cloty_colegio.ui.components.RutTextField
import com.example.cloty_colegio.ui.components.TelefonoChilenoTextField
import com.example.cloty_colegio.ui.components.ValidationMessageBanner
import com.example.cloty_colegio.util.ChileValidators

@Composable
fun ActivarCuentaScreen(viewModel: ClotyViewModel, onBackToLogin: () -> Unit) {
    var rut by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    // esto es nuevo
    var showValidation by rememberSaveable { mutableStateOf(false) }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    val passwordsMatch = password == confirmPassword

    fun errorValidacion(mostrarVacios: Boolean) = ChileValidators.primerMensajeError(
        ChileValidators.mensajeErrorRut(rut, mostrarVacios = mostrarVacios),
        ChileValidators.mensajeErrorEmail(email, obligatorio = true, mostrarVacios = mostrarVacios),
        ChileValidators.mensajeErrorTelefono(telefono, obligatorio = true, mostrarVacios = mostrarVacios),
        if (password.length < 4 && mostrarVacios) "La contraseña debe tener al menos 4 caracteres" else null,
        if (confirmPassword.isNotEmpty() && !passwordsMatch) "Las contraseñas no coinciden" else null
    )

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
        // esto es nuevo
        RutTextField(rut, { rut = it; showValidation = false }, label = "RUT del colegio", showValidation = showValidation)
        Spacer(Modifier.height(10.dp))
        EmailTextField(email, { email = it; showValidation = false }, label = "Email del colegio", obligatorio = true, showValidation = showValidation)
        Spacer(Modifier.height(10.dp))
        TelefonoChilenoTextField(telefono, { telefono = it; showValidation = false }, obligatorio = true, showValidation = showValidation)
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
        // esto es nuevo
        ValidationMessageBanner(if (showValidation) errorValidacion(true) else null)
        MessageBanner(error, true)
        MessageBanner(message, false, Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val err = errorValidacion(true)
                if (err != null) {
                    showValidation = true
                    return@Button
                }
                viewModel.activarCuenta(
                    ChileValidators.normalizarRutParaApi(rut),
                    email.trim(),
                    telefono.trim(),
                    password
                )
            },
            enabled = !loading,
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
