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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.cloty_administrador.data.api.Curso
import com.example.cloty_administrador.data.api.CursoRequest
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.components.MessageBanner
import com.example.cloty_administrador.ui.components.NivelSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursosScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val colegios by viewModel.colegios.collectAsState()
    val cursos by viewModel.cursos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var idColegio by rememberSaveable { mutableIntStateOf(0) }
    var editCurso by rememberSaveable { mutableStateOf<Curso?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf<Curso?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarColegios() }
    LaunchedEffect(colegios, idColegio) {
        if (idColegio > 0 && colegios.none { it.idColegio == idColegio }) {
            idColegio = 0
            viewModel.limpiarDatosColegio()
        }
    }
    LaunchedEffect(idColegio) {
        if (idColegio > 0) {
            viewModel.cargarCursos(idColegio)
        } else {
            viewModel.limpiarDatosColegio()
        }
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
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ColegioSelector(colegios, idColegio) { idColegio = it }
            MessageBanner(error, true)
            MessageBanner(message, false)
            if (idColegio > 0 && cursos.isEmpty() && !loading && error == null) {
                Text("No hay cursos. Impórtelos desde Carga CSV → alumnos.")
            }
            LazyColumn {
                items(cursos) { curso ->
                    ListItem(
                        headlineContent = { Text(curso.nombre) },
                        supportingContent = {
                            Text(
                                buildString {
                                    append(curso.nivel ?: curso.nombre)
                                    append(" · ID ${curso.idCurso}")
                                }
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { editCurso = curso }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { confirmDelete = curso }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    editCurso?.let { editing ->
        if (idColegio > 0) {
            var nombre by rememberSaveable(editing.idCurso) { mutableStateOf(editing.nombre) }
            var nivel by rememberSaveable(editing.idCurso) { mutableStateOf(editing.nivel ?: editing.nombre) }
            AlertDialog(
                onDismissRequest = { editCurso = null },
                title = { Text("Editar curso") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NivelSelector(
                            selected = nivel,
                            onSelected = { seleccion ->
                                nivel = seleccion
                                if (nombre.isBlank() || nombre == nivel) {
                                    nombre = seleccion
                                }
                            }
                        )
                        OutlinedTextField(
                            nombre,
                            { nombre = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.actualizarCurso(
                                editing.idCurso,
                                CursoRequest(idColegio, nombre.trim(), nivel.trim(), editing.estado ?: true)
                            )
                            editCurso = null
                        },
                        enabled = !loading && nombre.isNotBlank() && nivel.isNotBlank()
                    ) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { editCurso = null }) { Text("Cancelar") }
                }
            )
        }
    }

    confirmDelete?.let { curso ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Eliminar curso") },
            text = { Text("¿Eliminar ${curso.nombre}? También se eliminarán los alumnos asociados.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.eliminarCurso(curso.idCurso, idColegio)
                    confirmDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") } }
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
