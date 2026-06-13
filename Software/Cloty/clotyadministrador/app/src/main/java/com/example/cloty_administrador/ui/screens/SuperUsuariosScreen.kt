package com.example.cloty_administrador.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cloty_administrador.data.api.Usuario
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.components.EmailTextField
import com.example.cloty_administrador.ui.components.MessageBanner
import com.example.cloty_administrador.ui.components.PasswordTextField
import com.example.cloty_administrador.ui.components.RutTextField
import com.example.cloty_administrador.ui.components.ValidationMessageBanner
import com.example.cloty_administrador.util.ChileValidators
import com.example.cloty_administrador.util.SuperRootUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperUsuariosScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val lista by viewModel.superUsuarios.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editUsuario by rememberSaveable { mutableStateOf<Usuario?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarSuperUsuarios() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Super Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editUsuario = null
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
                Text("No hay super usuarios registrados.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(lista) { usuario ->
                    ListItem(
                        headlineContent = { Text(usuario.username) },
                        supportingContent = {
                            Text(
                                buildString {
                                    usuario.rut?.let { append(it) }
                                    append(" · ")
                                    append(if (usuario.estado == true) "Activo" else "Inactivo")
                                }
                            )
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("SUPER", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 4.dp))
                                IconButton(onClick = {
                                    editUsuario = usuario
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                if (!SuperRootUser.esRoot(usuario)) {
                                    IconButton(onClick = { confirmDelete = usuario }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        SuperUsuarioFormDialog(
            editing = editUsuario,
            loading = loading,
            onDismiss = { showDialog = false },
            onCreate = { username, rut, email, password ->
                viewModel.crearSuperUsuario(username, rut, email, password)
                showDialog = false
            },
            onUpdate = { id, username, rut, password, activo ->
                viewModel.actualizarSuperUsuario(id, username, rut, password, activo)
                showDialog = false
            }
        )
    }

    confirmDelete?.let { usuario ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Eliminar super usuario") },
            text = { Text("¿Eliminar ${usuario.username}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.eliminarSuperUsuario(usuario.idUsuario)
                    confirmDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun SuperUsuarioFormDialog(
    editing: Usuario?,
    loading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit,
    onUpdate: (Int, String, String, String?, Boolean) -> Unit
) {
    var username by rememberSaveable(editing?.idUsuario) { mutableStateOf(editing?.username ?: "") }
    var rut by rememberSaveable(editing?.idUsuario) { mutableStateOf(editing?.rut ?: "") }
    var email by rememberSaveable(editing?.idUsuario) { mutableStateOf("") }
    var password by rememberSaveable(editing?.idUsuario) { mutableStateOf("") }
    var confirmPassword by rememberSaveable(editing?.idUsuario) { mutableStateOf("") }
    var activo by rememberSaveable(editing?.idUsuario) { mutableStateOf(editing?.estado ?: true) }
    var showValidation by rememberSaveable(editing?.idUsuario) { mutableStateOf(false) }

    val isEditing = editing != null
    val passwordsMatch = password == confirmPassword

    fun errorValidacion(mostrarVacios: Boolean) = ChileValidators.primerMensajeError(
        if (username.isBlank() && mostrarVacios) "Debe ingresar un nombre de usuario" else null,
        ChileValidators.mensajeErrorRut(rut, mostrarVacios = mostrarVacios),
        if (!isEditing) ChileValidators.mensajeErrorEmail(email, obligatorio = true, mostrarVacios = mostrarVacios) else null,
        if (!isEditing && password.length < 4 && mostrarVacios) "La contraseña debe tener al menos 4 caracteres" else null,
        if (!isEditing && confirmPassword.isNotEmpty() && !passwordsMatch) "Las contraseñas no coinciden" else null,
        if (isEditing && password.isNotBlank() && password.length < 4) "La contraseña debe tener al menos 4 caracteres" else null,
        if (isEditing && password.isNotBlank() && confirmPassword.isNotEmpty() && !passwordsMatch) "Las contraseñas no coinciden" else null
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar super usuario" else "Nuevo super usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    username, { username = it; showValidation = false },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                RutTextField(rut, { rut = it; showValidation = false }, showValidation = showValidation)
                if (!isEditing) {
                    EmailTextField(email, { email = it; showValidation = false }, label = "Correo", obligatorio = true, showValidation = showValidation)
                }
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it; showValidation = false },
                    label = if (isEditing) "Nueva contraseña (opcional)" else "Contraseña"
                )
                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; showValidation = false },
                    label = if (isEditing) "Confirmar nueva contraseña" else "Confirmar contraseña",
                    isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                    supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        { Text("Las contraseñas no coinciden") }
                    } else null
                )
                if (isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Activo")
                        Switch(checked = activo, onCheckedChange = { activo = it })
                    }
                }
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
                            editing!!.idUsuario,
                            username.trim(),
                            rutNorm,
                            password.takeIf { it.isNotBlank() },
                            activo
                        )
                    } else {
                        onCreate(username.trim(), rutNorm, email.trim(), password)
                    }
                },
                enabled = !loading
            ) { Text(if (isEditing) "Guardar" else "Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
