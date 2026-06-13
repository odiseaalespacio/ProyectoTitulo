package com.example.cloty_colegio.ui.screens

// esta parte es nueva

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cloty_colegio.data.api.ColegioRequest
import com.example.cloty_colegio.ui.ClotyViewModel
import com.example.cloty_colegio.ui.components.EmailTextField
import com.example.cloty_colegio.ui.components.MessageBanner
import com.example.cloty_colegio.ui.components.RutTextField
import com.example.cloty_colegio.ui.components.TelefonoChilenoTextField
import com.example.cloty_colegio.ui.components.ValidationMessageBanner
import com.example.cloty_colegio.util.ChileValidators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarColegioScreen(
    viewModel: ClotyViewModel,
    contentPadding: PaddingValues,
    onBack: () -> Unit
) {
    val colegio by viewModel.colegio.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    var nombre by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    // esto es nuevo
    var showValidation by rememberSaveable { mutableStateOf(false) }

    fun errorValidacion() = ChileValidators.primerMensajeError(
        if (nombre.isBlank()) "Debe ingresar el nombre del colegio" else null,
        ChileValidators.mensajeErrorEmail(email, obligatorio = true),
        ChileValidators.mensajeErrorTelefono(telefono)
    )

    LaunchedEffect(Unit) { viewModel.cargarColegio() }

    LaunchedEffect(colegio) {
        colegio?.let { c ->
            nombre = c.nombre
            rut = c.rut
            email = c.email.orEmpty()
            telefono = c.telefono.orEmpty()
            direccion = c.direccion.orEmpty()
        }
    }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        topBar = {
            TopAppBar(
                title = { Text("Mi establecimiento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MessageBanner(error, true)
            MessageBanner(message, false)
            OutlinedTextField(rut, {}, label = { Text("RUT") }, readOnly = true, modifier = Modifier.fillMaxWidth(), singleLine = true)
            // esto es nuevo
            OutlinedTextField(nombre, { nombre = it; showValidation = false }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            EmailTextField(email, { email = it; showValidation = false }, label = "Correo", obligatorio = true, showValidation = showValidation)
            TelefonoChilenoTextField(telefono, { telefono = it; showValidation = false }, showValidation = showValidation)
            OutlinedTextField(direccion, { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            // esto es nuevo
            ValidationMessageBanner(if (showValidation) errorValidacion() else null)
            Button(
                onClick = {
                    val err = errorValidacion()
                    if (err != null) {
                        showValidation = true
                        return@Button
                    }
                    viewModel.actualizarColegio(
                        ColegioRequest(
                            rut = ChileValidators.normalizarRutParaApi(rut),
                            nombre = nombre.trim(),
                            email = email.trim(),
                            telefono = telefono.trim().ifBlank { null },
                            direccion = direccion.trim().ifBlank { null }
                        )
                    )
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar cambios") }
        }
    }
}
