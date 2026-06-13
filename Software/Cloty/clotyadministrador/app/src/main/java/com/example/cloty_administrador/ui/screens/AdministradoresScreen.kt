package com.example.cloty_administrador.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import com.example.cloty_administrador.data.api.Administrador
import com.example.cloty_administrador.data.api.AdministradorCompletoRequest
import com.example.cloty_administrador.data.api.AdministradorRequest
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.components.EmailTextField
import com.example.cloty_administrador.ui.components.MessageBanner
import com.example.cloty_administrador.ui.components.PasswordTextField
import com.example.cloty_administrador.ui.components.RutTextField
import com.example.cloty_administrador.ui.components.TelefonoChilenoTextField
import com.example.cloty_administrador.ui.components.ValidationMessageBanner
import com.example.cloty_administrador.util.ChileValidators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministradoresScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val lista by viewModel.administradores.collectAsState()
    val usuariosPorId by viewModel.usuariosPorId.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editAdmin by rememberSaveable { mutableStateOf<Administrador?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf<Administrador?>(null) }

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
            FloatingActionButton(onClick = {
                editAdmin = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            MessageBanner(error, true)
            MessageBanner(message, false, Modifier.padding(top = 8.dp))
            if (lista.isEmpty() && !loading && error == null) {
                Text("No hay administradores registrados.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(lista) { admin ->
                    val username = usuariosPorId[admin.idUsuario]?.username
                    ListItem(
                        headlineContent = { Text("${admin.nombres} ${admin.apellidos}") },
                        supportingContent = {
                            Text(
                                buildString {
                                    username?.let { append("$it · ") }
                                    append("${admin.rut} · ${admin.email}")
                                }
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    editAdmin = admin
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { confirmDelete = admin }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AdminFormDialog(
            editing = editAdmin,
            usernameInicial = editAdmin?.let { usuariosPorId[it.idUsuario]?.username } ?: "",
            loading = loading,
            onDismiss = { showDialog = false },
            onCreate = { req ->
                viewModel.crearAdministrador(req)
                showDialog = false
            },
            onUpdate = { id, req, username, password ->
                viewModel.actualizarAdministrador(id, req, username, password)
                showDialog = false
            }
        )
    }

    confirmDelete?.let { admin ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Eliminar administrador") },
            text = { Text("¿Eliminar ${admin.nombres} ${admin.apellidos}? Se eliminará también su usuario de acceso.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.eliminarAdministrador(admin.idAdministrador)
                    confirmDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun AdminFormDialog(
    editing: Administrador?,
    usernameInicial: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (AdministradorCompletoRequest) -> Unit,
    onUpdate: (Int, AdministradorRequest, String?, String?) -> Unit
) {
    val isEditing = editing != null
    var username by rememberSaveable(editing?.idAdministrador) { mutableStateOf(usernameInicial) }
    var password by rememberSaveable(editing?.idAdministrador) { mutableStateOf("") }
    var confirmPassword by rememberSaveable(editing?.idAdministrador) { mutableStateOf("") }
    var rut by rememberSaveable(editing?.idAdministrador) { mutableStateOf(editing?.rut ?: "") }
    var nombres by rememberSaveable(editing?.idAdministrador) { mutableStateOf(editing?.nombres ?: "") }
    var apellidos by rememberSaveable(editing?.idAdministrador) { mutableStateOf(editing?.apellidos ?: "") }
    var email by rememberSaveable(editing?.idAdministrador) { mutableStateOf(editing?.email ?: "") }
    var telefono by rememberSaveable(editing?.idAdministrador) { mutableStateOf(editing?.telefono ?: "") }
    var showValidation by rememberSaveable(editing?.idAdministrador) { mutableStateOf(false) }

    val passwordsMatch = password == confirmPassword

    fun errorValidacion(mostrarVacios: Boolean) = ChileValidators.primerMensajeError(
        ChileValidators.mensajeErrorRut(rut, mostrarVacios = mostrarVacios),
        ChileValidators.mensajeErrorEmail(email, obligatorio = true, mostrarVacios = mostrarVacios),
        ChileValidators.mensajeErrorTelefono(telefono, mostrarVacios = mostrarVacios),
        if (username.isBlank() && mostrarVacios) "Debe ingresar un usuario" else null,
        if (!isEditing && password.length < 4 && mostrarVacios) "La contraseña debe tener al menos 4 caracteres" else null,
        if (!isEditing && confirmPassword.isNotEmpty() && !passwordsMatch) "Las contraseñas no coinciden" else null,
        if (isEditing && password.isNotBlank() && password.length < 4) "La contraseña debe tener al menos 4 caracteres" else null,
        if (isEditing && password.isNotBlank() && confirmPassword.isNotEmpty() && !passwordsMatch) "Las contraseñas no coinciden" else null,
        if (nombres.isBlank() && mostrarVacios) "Debe ingresar nombres" else null,
        if (apellidos.isBlank() && mostrarVacios) "Debe ingresar apellidos" else null
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar administrador" else "Nuevo administrador") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(username, { username = it; showValidation = false }, label = { Text("Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it; showValidation = false },
                    label = if (isEditing) "Nueva contraseña (opcional)" else "Contraseña"
                )
                if (!isEditing || password.isNotBlank()) {
                    PasswordTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; showValidation = false },
                        label = "Confirmar contraseña",
                        isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                        supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                            { Text("Las contraseñas no coinciden") }
                        } else null
                    )
                }
                RutTextField(rut, { rut = it; showValidation = false }, showValidation = showValidation)
                OutlinedTextField(nombres, { nombres = it; showValidation = false }, label = { Text("Nombres") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(apellidos, { apellidos = it; showValidation = false }, label = { Text("Apellidos") }, singleLine = true, modifier = Modifier.fillMaxWidth())
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
                    val rutNorm = ChileValidators.normalizarRutParaApi(rut)
                    if (isEditing) {
                        onUpdate(
                            editing!!.idAdministrador,
                            AdministradorRequest(
                                idUsuario = editing.idUsuario,
                                rut = rutNorm,
                                nombres = nombres.trim(),
                                apellidos = apellidos.trim(),
                                email = email.trim(),
                                telefono = telefono.trim().ifBlank { null }
                            ),
                            username.trim(),
                            password.takeIf { it.isNotBlank() }
                        )
                    } else {
                        onCreate(
                            AdministradorCompletoRequest(
                                username.trim(), password, rutNorm, nombres.trim(),
                                apellidos.trim(), email.trim(), telefono.trim().ifBlank { null }
                            )
                        )
                    }
                },
                enabled = !loading
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
