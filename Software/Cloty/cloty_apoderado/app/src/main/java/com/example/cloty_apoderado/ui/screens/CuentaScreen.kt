package com.example.cloty_apoderado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.cloty_apoderado.ui.components.EmailTextField
import com.example.cloty_apoderado.ui.components.MessageBanner
import com.example.cloty_apoderado.ui.components.TelefonoChilenoTextField
import com.example.cloty_apoderado.ui.components.UbicacionSelector
import com.example.cloty_apoderado.ui.components.ValidationMessageBanner
import com.example.cloty_apoderado.util.ChileValidators

@Composable
fun CuentaScreen(viewModel: ClotyViewModel, contentPadding: PaddingValues, onLogout: () -> Unit) {
    var actual by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var codigoRegion by rememberSaveable { mutableStateOf<String?>(null) }
    var codigoComuna by rememberSaveable { mutableStateOf<String?>(null) }
    var calleNumero by rememberSaveable { mutableStateOf("") }
    var showValidation by rememberSaveable { mutableStateOf(false) }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    val nombre by viewModel.nombreUsuario.collectAsState()
    val apoderado by viewModel.apoderado.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarPerfil() }

    LaunchedEffect(apoderado) {
        apoderado?.let { a ->
            email = a.email.orEmpty()
            telefono = a.telefono.orEmpty()
            codigoComuna = a.codigoComuna
            calleNumero = a.calleNumero.orEmpty()
        }
    }

    Column(
        Modifier.fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Mi cuenta", style = MaterialTheme.typography.titleLarge)
        nombre?.let { Text("Usuario: $it", style = MaterialTheme.typography.bodyMedium) }

        Text("Mis datos", style = MaterialTheme.typography.titleMedium)
        apoderado?.let { a ->
            OutlinedTextField(
                "${a.nombres} ${a.apellidos}",
                {},
                label = { Text("Nombre") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                a.rut,
                {},
                label = { Text("RUT") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        EmailTextField(email, { email = it; showValidation = false }, label = "Correo", showValidation = showValidation)
        TelefonoChilenoTextField(telefono, { telefono = it; showValidation = false }, showValidation = showValidation)
        UbicacionSelector(
            codigoRegion = codigoRegion,
            codigoComuna = codigoComuna,
            calleNumero = calleNumero,
            onRegionChange = { codigoRegion = it },
            onComunaChange = { codigoComuna = it },
            onCalleNumeroChange = { calleNumero = it },
            listarRegiones = { viewModel.listarRegiones() },
            listarComunas = { viewModel.listarComunas(it) },
            resolverComuna = { viewModel.obtenerComuna(it) }
        )
        ValidationMessageBanner(
            if (showValidation) {
                ChileValidators.primerMensajeError(
                    ChileValidators.mensajeErrorEmail(email),
                    ChileValidators.mensajeErrorTelefono(telefono)
                )
            } else null
        )
        Button(
            onClick = {
                val err = ChileValidators.primerMensajeError(
                    ChileValidators.mensajeErrorEmail(email),
                    ChileValidators.mensajeErrorTelefono(telefono)
                )
                if (err != null) {
                    showValidation = true
                    return@Button
                }
                viewModel.actualizarMisDatos(email, telefono, codigoComuna, calleNumero)
            },
            enabled = !loading && apoderado != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar mis datos") }

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
