package com.example.cloty_administrador.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.cloty_administrador.data.api.AdministradorCompletoRequest
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.components.EmailTextField
import com.example.cloty_administrador.ui.components.MessageBanner
import com.example.cloty_administrador.ui.components.RutTextField
import com.example.cloty_administrador.ui.components.TelefonoChilenoTextField
import com.example.cloty_administrador.ui.components.ValidationMessageBanner
import com.example.cloty_administrador.util.ChileValidators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministradoresScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val lista by viewModel.administradores.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargarAdministradores() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administradores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            MessageBanner(error, true)
            MessageBanner(message, false, Modifier.padding(top = 8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(lista) { admin ->
                    ListItem(
                        headlineContent = { Text("${admin.nombres} ${admin.apellidos}") },
                        supportingContent = { Text("${admin.rut} · ${admin.email}") }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AdminFormDialog(
            loading = loading,
            onDismiss = { showDialog = false },
            onConfirm = { req ->
                viewModel.crearAdministrador(req)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AdminFormDialog(
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (AdministradorCompletoRequest) -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var nombres by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    // esto es nuevo
    var showValidation by rememberSaveable { mutableStateOf(false) }

    fun errorValidacion(mostrarVacios: Boolean) = ChileValidators.primerMensajeError(
        ChileValidators.mensajeErrorRut(rut, mostrarVacios = mostrarVacios),
        ChileValidators.mensajeErrorEmail(email, obligatorio = true, mostrarVacios = mostrarVacios),
        ChileValidators.mensajeErrorTelefono(telefono, mostrarVacios = mostrarVacios),
        if (username.isBlank() && mostrarVacios) "Debe ingresar un usuario" else null,
        if (password.length < 4 && mostrarVacios) "La contraseña debe tener al menos 4 caracteres" else null
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo administrador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(username, { username = it }, label = { Text("Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                // esto es nuevo
                RutTextField(rut, { rut = it; showValidation = false }, showValidation = showValidation)
                OutlinedTextField(nombres, { nombres = it }, label = { Text("Nombres") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(apellidos, { apellidos = it }, label = { Text("Apellidos") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                EmailTextField(email, { email = it; showValidation = false }, obligatorio = true, showValidation = showValidation)
                TelefonoChilenoTextField(telefono, { telefono = it; showValidation = false }, showValidation = showValidation)
                ValidationMessageBanner(if (showValidation) errorValidacion(true) else null)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val err = errorValidacion(true)
                    if (err != null) {
                        showValidation = true
                        return@Button
                    }
                    onConfirm(
                        AdministradorCompletoRequest(
                            username.trim(), password, ChileValidators.normalizarRutParaApi(rut), nombres.trim(),
                            apellidos.trim(), email.trim(), telefono.trim().ifBlank { null }
                        )
                    )
                },
                enabled = !loading
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
