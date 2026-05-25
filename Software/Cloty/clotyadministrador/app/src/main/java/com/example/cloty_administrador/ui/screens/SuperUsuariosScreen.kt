package com.example.cloty_administrador.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.components.MessageBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperUsuariosScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val lista by viewModel.superUsuarios.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }

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
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            MessageBanner(error, true)
            MessageBanner(message, false, Modifier.padding(top = 8.dp))
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
                            Text("SUPER", style = MaterialTheme.typography.labelSmall)
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        SuperUsuarioFormDialog(
            loading = loading,
            onDismiss = { showDialog = false },
            onConfirm = { username, rut, password ->
                viewModel.crearSuperUsuario(username, rut, password)
                showDialog = false
            }
        )
    }
}

@Composable
private fun SuperUsuarioFormDialog(
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val passwordsMatch = password == confirmPassword
    val formValid = username.isNotBlank() && rut.isNotBlank() && password.length >= 4 && passwordsMatch

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo super usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    username, { username = it },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    rut, { rut = it },
                    label = { Text("RUT") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    password, { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    confirmPassword, { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                    supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        { Text("Las contraseñas no coinciden") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(username.trim(), rut.trim(), password) },
                enabled = !loading && formValid
            ) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
