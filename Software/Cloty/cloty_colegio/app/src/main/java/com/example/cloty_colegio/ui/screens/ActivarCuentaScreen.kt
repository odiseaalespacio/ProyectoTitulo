package com.example.cloty_colegio.ui.screens

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
import com.example.cloty_colegio.ui.ClotyViewModel
import com.example.cloty_colegio.ui.components.MessageBanner
import com.example.cloty_colegio.ui.components.RutTextField
import com.example.cloty_colegio.ui.components.ValidationMessageBanner
import com.example.cloty_colegio.ui.theme.clotyFieldColors
import com.example.cloty_colegio.util.ChileValidators

private enum class PasoActivacion { RUT, CODIGO_ENVIADO, INGRESAR_CODIGO, CREAR_CONTRASENA }

@Composable
fun ActivarCuentaScreen(viewModel: ClotyViewModel, onBackToLogin: () -> Unit) {
    var paso by rememberSaveable { mutableStateOf(PasoActivacion.RUT) }
    var rut by rememberSaveable { mutableStateOf("") }
    var codigo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showValidation by rememberSaveable { mutableStateOf(false) }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val correoActivacion by viewModel.correoActivacion.collectAsState()

    val passwordsMatch = password == confirmPassword
    val rutNormalizado = ChileValidators.normalizarRutParaApi(rut)
    val fieldColors = clotyFieldColors()

    DisposableEffect(Unit) {
        onDispose { viewModel.limpiarActivacion() }
    }

    fun errorRut(mostrarVacios: Boolean) =
        ChileValidators.mensajeErrorRut(rut, mostrarVacios = mostrarVacios)

    fun errorCodigo(mostrarVacios: Boolean) =
        if (codigo.length != 6 && mostrarVacios) "Ingrese el código de 6 dígitos enviado a su correo" else null

    fun errorContrasena(mostrarVacios: Boolean) = ChileValidators.primerMensajeError(
        if (password.length < 4 && mostrarVacios) "La contraseña debe tener al menos 4 caracteres" else null,
        if (confirmPassword.isNotEmpty() && !passwordsMatch) "Las contraseñas no coinciden" else null
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
            "Activar cuenta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        when (paso) {
            PasoActivacion.RUT -> {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Ingrese el RUT del colegio para iniciar la activación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                RutTextField(
                    rut,
                    { rut = it; showValidation = false },
                    label = "RUT del colegio",
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
                        viewModel.solicitarCodigoActivacion(rutNormalizado) {
                            paso = PasoActivacion.CODIGO_ENVIADO
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (loading) "Enviando código…" else "Continuar") }
            }

            PasoActivacion.CODIGO_ENVIADO -> {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Se envió un código de 6 dígitos al correo registrado",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    correoActivacion ?: "su correo",
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
                    onClick = { paso = PasoActivacion.INGRESAR_CODIGO },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Ingresar código") }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { viewModel.solicitarCodigoActivacion(rutNormalizado) { } },
                    enabled = !loading
                ) { Text(if (loading) "Reenviando…" else "Reenviar código") }
                TextButton(onClick = { paso = PasoActivacion.RUT }) {
                    Text("Cambiar RUT")
                }
            }

            PasoActivacion.INGRESAR_CODIGO -> {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Ingrese el código recibido en su correo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    codigo,
                    { v -> if (v.length <= 6 && v.all { it.isDigit() }) codigo = v; showValidation = false },
                    label = { Text("Código de activación") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = fieldColors
                )
                Spacer(Modifier.height(16.dp))
                ValidationMessageBanner(if (showValidation) errorCodigo(true) else null)
                MessageBanner(error, true)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val err = errorCodigo(true)
                        if (err != null) {
                            showValidation = true
                            return@Button
                        }
                        viewModel.validarCodigoActivacion(rutNormalizado, codigo.trim()) {
                            paso = PasoActivacion.CREAR_CONTRASENA
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (loading) "Verificando…" else "Continuar") }
                TextButton(onClick = { paso = PasoActivacion.CODIGO_ENVIADO }) {
                    Text("Volver")
                }
            }

            PasoActivacion.CREAR_CONTRASENA -> {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Código verificado. Cree su contraseña",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    password, { password = it },
                    label = { Text("Contraseña (mín. 4 caracteres)") },
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
                        viewModel.activarCuenta(rutNormalizado, codigo.trim(), password)
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (loading) "Activando…" else "Activar cuenta") }
                TextButton(onClick = { paso = PasoActivacion.INGRESAR_CODIGO }) {
                    Text("Volver")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBackToLogin) {
            Text("Ya tengo cuenta — Iniciar sesión")
        }
    }
}
