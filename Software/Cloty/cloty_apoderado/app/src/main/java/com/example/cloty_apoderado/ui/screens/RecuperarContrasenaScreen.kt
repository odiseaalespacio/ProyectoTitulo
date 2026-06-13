package com.example.cloty_apoderado.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cloty_apoderado.ui.ClotyViewModel
import com.example.cloty_apoderado.ui.components.MessageBanner
import com.example.cloty_apoderado.ui.components.RutTextField
import com.example.cloty_apoderado.ui.components.ValidationMessageBanner
import com.example.cloty_apoderado.util.ChileValidators

private enum class PasoRecuperacion { RUT, CODIGO_ENVIADO, NUEVA_CONTRASENA }

@Composable
fun RecuperarContrasenaScreen(viewModel: ClotyViewModel, onBackToLogin: () -> Unit) {
    var paso by rememberSaveable { mutableStateOf(PasoRecuperacion.RUT) }
    var rut by rememberSaveable { mutableStateOf("") }
    var codigo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showValidation by rememberSaveable { mutableStateOf(false) }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val correo by viewModel.correoActivacion.collectAsState()

    val passwordsMatch = password == confirmPassword
    val rutNormalizado = ChileValidators.normalizarRutParaApi(rut)

    DisposableEffect(Unit) {
        onDispose { viewModel.limpiarRecuperacion() }
    }

    fun errorRut(mostrarVacios: Boolean) =
        ChileValidators.mensajeErrorRut(rut, mostrarVacios = mostrarVacios)

    fun errorContrasena(mostrarVacios: Boolean) = ChileValidators.primerMensajeError(
        if (codigo.length != 6 && mostrarVacios) "Ingrese el código de 6 dígitos enviado a su correo" else null,
        if (password.length < 4 && mostrarVacios) "La contraseña debe tener al menos 4 caracteres" else null,
        if (confirmPassword.isNotEmpty() && !passwordsMatch) "Las contraseñas no coinciden" else null
    )

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Recuperar contraseña",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        when (paso) {
            PasoRecuperacion.RUT -> {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Ingrese su RUT para recibir un código en el correo registrado",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                RutTextField(
                    rut,
                    { rut = it; showValidation = false },
                    label = "RUT (ej: 12.345.678-9)",
                    showValidation = showValidation
                )
                Spacer(Modifier.height(16.dp))
                ValidationMessageBanner(if (showValidation) errorRut(true) else null)
                MessageBanner(error, true)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val err = errorRut(true)
                        if (err != null) {
                            showValidation = true
                            return@Button
                        }
                        viewModel.solicitarRecuperacionContrasena(rutNormalizado) {
                            paso = PasoRecuperacion.CODIGO_ENVIADO
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (loading) "Enviando código…" else "Enviar código") }
            }

            PasoRecuperacion.CODIGO_ENVIADO -> {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Se envió un código de recuperación al correo registrado",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    correo ?: "su correo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Revise su bandeja de entrada. El código es válido por 30 minutos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                MessageBanner(error, true)
                Button(
                    onClick = { paso = PasoRecuperacion.NUEVA_CONTRASENA },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Ingresar código") }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { viewModel.solicitarRecuperacionContrasena(rutNormalizado) { } },
                    enabled = !loading
                ) { Text(if (loading) "Reenviando…" else "Reenviar código") }
                TextButton(onClick = { paso = PasoRecuperacion.RUT }) {
                    Text("Cambiar RUT")
                }
            }

            PasoRecuperacion.NUEVA_CONTRASENA -> {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Ingrese el código recibido y su nueva contraseña",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    codigo,
                    { v -> if (v.length <= 6 && v.all { it.isDigit() }) codigo = v; showValidation = false },
                    label = { Text("Código de recuperación") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = fieldColors
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    password, { password = it },
                    label = { Text("Nueva contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = fieldColors,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
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
                    isError = confirmPassword.isNotEmpty() && !passwordsMatch
                )
                Spacer(Modifier.height(16.dp))
                ValidationMessageBanner(if (showValidation) errorContrasena(true) else null)
                MessageBanner(error, true)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val err = errorContrasena(true)
                        if (err != null) {
                            showValidation = true
                            return@Button
                        }
                        viewModel.restablecerContrasena(rutNormalizado, codigo.trim(), password, onBackToLogin)
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (loading) "Guardando…" else "Restablecer contraseña") }
                TextButton(onClick = { paso = PasoRecuperacion.CODIGO_ENVIADO }) {
                    Text("Volver")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBackToLogin) {
            Text("Volver al inicio de sesión")
        }
    }
}
