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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cloty_administrador.data.api.CursoRequest
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.components.MessageBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursosScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val colegios by viewModel.colegios.collectAsState()
    val cursos by viewModel.cursos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var idColegio by rememberSaveable { mutableIntStateOf(0) }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargarColegios() }
    LaunchedEffect(idColegio) {
        if (idColegio > 0) viewModel.cargarCursos(idColegio)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cursos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (idColegio > 0) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo curso")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ColegioSelector(colegios, idColegio) { idColegio = it }
            MessageBanner(error, true)
            MessageBanner(message, false)
            LazyColumn {
                items(cursos) { curso ->
                    ListItem(
                        headlineContent = { Text(curso.nombre) },
                        supportingContent = { Text("ID ${curso.idCurso}") }
                    )
                }
            }
        }
    }

    if (showDialog && idColegio > 0) {
        var nombre by rememberSaveable { mutableStateOf("") }
        var nivel by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nuevo curso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(nivel, { nivel = it }, label = { Text("Nivel (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.crearCurso(CursoRequest(idColegio, nombre.trim(), nivel.trim().ifBlank { null }))
                        showDialog = false
                    },
                    enabled = !loading && nombre.isNotBlank()
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColegioSelector(
    colegios: List<com.example.cloty_administrador.data.api.Colegio>,
    selectedId: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selected = colegios.find { it.idColegio == selectedId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.nombre ?: "Seleccione colegio",
            onValueChange = {},
            readOnly = true,
            label = { Text("Colegio") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            colegios.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.nombre) },
                    onClick = {
                        onSelected(c.idColegio)
                        expanded = false
                    }
                )
            }
        }
    }
}
