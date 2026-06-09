package com.example.cloty_apoderado.ui.screens

// esta parte es nueva

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cloty_apoderado.ui.ClotyViewModel
import com.example.cloty_apoderado.ui.components.MessageBanner
import com.example.cloty_apoderado.ui.components.RutTextField
import com.example.cloty_apoderado.ui.components.ValidationMessageBanner
import com.example.cloty_apoderado.util.ChileValidators

@Composable
fun ActivarCuentaScreen(viewModel: ClotyViewModel, onBackToLogin: () -> Unit) {
    var rut by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    // esto es nuevo
    var showValidation by rememberSaveable { mutableStateOf(false) }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val passwordsMatch = password == confirmPassword

    fun errorValidacion(mostrarVacios: Boolean) = ChileValidators.primerMensajeError(
        ChileValidators.mensajeErrorRut(rut, mostrarVacios = mostrarVacios),
        if (password.length < 4 && mostrarVacios) "La contraseña debe tener al menos 4 caracteres" else null,
        if (confirmPassword.isNotEmpty() && !passwordsMatch) "Las contraseñas no coinciden" else null
    )

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Activar cuenta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Ingrese el RUT con el que fue registrado por su colegio",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))
        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary
        )
        // esto es nuevo
        RutTextField(
            rut,
            { rut = it; showValidation = false },
            label = "RUT (ej: 12.345.678-9)",
            showValidation = showValidation
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            password, { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = fieldColors,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            confirmPassword, { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = fieldColors,
            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                { Text("Las contraseñas no coinciden") }
            } else null
        )
        Spacer(Modifier.height(16.dp))
        // esto es nuevo
        ValidationMessageBanner(if (showValidation) errorValidacion(true) else null)
        MessageBanner(error, true)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val err = errorValidacion(true)
                if (err != null) {
                    showValidation = true
                    return@Button
                }
                viewModel.activarCuenta(ChileValidators.normalizarRutParaApi(rut), password)
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (loading) "Activando…" else "Activar cuenta") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBackToLogin) {
            Text("Volver al inicio de sesión")
        }
    }
}
