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
import com.example.cloty_administrador.data.api.ColegioRequest
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.components.MessageBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColegiosScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val lista by viewModel.colegios.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargarColegios() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Colegios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo colegio")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            MessageBanner(error, true)
            MessageBanner(message, false, Modifier.padding(top = 8.dp))
            LazyColumn {
                items(lista) { c ->
                    ListItem(
                        headlineContent = { Text(c.nombre) },
                        supportingContent = { Text("RUT ${c.rut} · ID ${c.idColegio}") }
                    )
                }
            }
        }
    }

    if (showDialog) {
        var rut by rememberSaveable { mutableStateOf("") }
        var nombre by rememberSaveable { mutableStateOf("") }
        var telefono by rememberSaveable { mutableStateOf("") }
        var direccion by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Registrar colegio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(rut, { rut = it }, label = { Text("RUT") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(telefono, { telefono = it }, label = { Text("Teléfono") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(direccion, { direccion = it }, label = { Text("Dirección") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.crearColegio(
                            ColegioRequest(
                                rut = rut.trim(),
                                nombre = nombre.trim(),
                                telefono = telefono.trim().ifBlank { null },
                                direccion = direccion.trim().ifBlank { null }
                            )
                        )
                        showDialog = false
                    },
                    enabled = !loading && rut.isNotBlank() && nombre.isNotBlank()
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
        )
    }
}
