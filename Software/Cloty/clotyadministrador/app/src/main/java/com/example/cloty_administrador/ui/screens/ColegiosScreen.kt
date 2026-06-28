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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cloty_administrador.data.api.Colegio
import com.example.cloty_administrador.data.api.ColegioRequest
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.components.ClotyPullRefresh
import com.example.cloty_administrador.ui.components.EmailTextField
import com.example.cloty_administrador.ui.components.MessageBanner
import com.example.cloty_administrador.ui.components.RutTextField
import com.example.cloty_administrador.ui.components.TelefonoChilenoTextField
import com.example.cloty_administrador.ui.components.UbicacionSelector
import com.example.cloty_administrador.ui.components.ValidationMessageBanner
import com.example.cloty_administrador.util.ChileValidators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColegiosScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val lista by viewModel.colegios.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editColegio by remember { mutableStateOf<Colegio?>(null) }
    var confirmDelete by remember { mutableStateOf<Colegio?>(null) }

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
            FloatingActionButton(onClick = {
                editColegio = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo colegio")
            }
        }
    ) { padding ->
        ClotyPullRefresh(
            refreshing = refreshing,
            onRefresh = { viewModel.refrescarColegios() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            MessageBanner(error, true)
            MessageBanner(message, false, Modifier.padding(top = 8.dp))
            if (lista.isEmpty() && !loading && error == null) {
                Text("No hay colegios registrados.")
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(lista) { c ->
                    ListItem(
                        headlineContent = { Text(c.nombre) },
                        supportingContent = { Text("RUT ${c.rut} · ID ${c.idColegio}") },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    editColegio = c
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { confirmDelete = c }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    )
                }
            }
        }
        }
    }

    if (showDialog) {
        val editing = editColegio
        var rut by rememberSaveable(editing?.idColegio) { mutableStateOf(editing?.rut ?: "") }
        var nombre by rememberSaveable(editing?.idColegio) { mutableStateOf(editing?.nombre ?: "") }
        var email by rememberSaveable(editing?.idColegio) { mutableStateOf(editing?.email ?: "") }
        var telefono by rememberSaveable(editing?.idColegio) { mutableStateOf(editing?.telefono ?: "") }
        var codigoRegion by rememberSaveable(editing?.idColegio) { mutableStateOf<String?>(null) }
        var codigoComuna by rememberSaveable(editing?.idColegio) { mutableStateOf(editing?.codigoComuna) }
        var calleNumero by rememberSaveable(editing?.idColegio) { mutableStateOf(editing?.calleNumero ?: "") }
        var showValidation by rememberSaveable(editing?.idColegio) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editing == null) "Registrar colegio" else "Editar colegio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RutTextField(rut, { rut = it; showValidation = false }, showValidation = showValidation)
                    OutlinedTextField(nombre, { nombre = it; showValidation = false }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    EmailTextField(email, { email = it; showValidation = false }, label = "Correo", obligatorio = true, showValidation = showValidation)
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
                                ChileValidators.mensajeErrorRut(rut, mostrarVacios = true),
                                ChileValidators.mensajeErrorEmail(email, obligatorio = true, mostrarVacios = true),
                                ChileValidators.mensajeErrorTelefono(telefono, mostrarVacios = true),
                                if (nombre.isBlank()) "Debe ingresar el nombre del colegio" else null
                            )
                        } else null
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val err = ChileValidators.primerMensajeError(
                            ChileValidators.mensajeErrorRut(rut, mostrarVacios = true),
                            ChileValidators.mensajeErrorEmail(email, obligatorio = true),
                            ChileValidators.mensajeErrorTelefono(telefono)
                        )
                        if (err != null || nombre.isBlank()) {
                            showValidation = true
                            return@Button
                        }
                        val req = ColegioRequest(
                            rut = ChileValidators.normalizarRutParaApi(rut),
                            nombre = nombre.trim(),
                            email = email.trim(),
                            telefono = telefono.trim().ifBlank { null },
                            codigoComuna = codigoComuna?.trim()?.ifBlank { null },
                            calleNumero = calleNumero.trim().ifBlank { null }
                        )
                        if (editing != null) {
                            viewModel.actualizarColegio(editing.idColegio, req)
                        } else {
                            viewModel.crearColegio(req)
                        }
                        showDialog = false
                    },
                    enabled = !loading
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
        )
    }

    confirmDelete?.let { c ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Eliminar colegio") },
            text = { Text("¿Eliminar ${c.nombre}? Solo es posible si no tiene datos vinculados.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.eliminarColegio(c.idColegio)
                    confirmDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") } }
        )
    }
}
