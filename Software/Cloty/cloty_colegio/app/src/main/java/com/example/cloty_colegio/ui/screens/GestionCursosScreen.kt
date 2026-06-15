package com.example.cloty_colegio.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.cloty_colegio.data.api.Curso
import com.example.cloty_colegio.data.api.CursoRequest
import com.example.cloty_colegio.ui.ClotyViewModel
import com.example.cloty_colegio.ui.components.MessageBanner
import com.example.cloty_colegio.ui.components.NivelSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCursosScreen(
    viewModel: ClotyViewModel,
    contentPadding: PaddingValues,
    onBack: () -> Unit
) {
    val idColegio by viewModel.idColegio.collectAsState()
    val cursos by viewModel.cursos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editCurso by rememberSaveable { mutableStateOf<Curso?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf<Curso?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarGestion() }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
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
            if (idColegio != null) {
                FloatingActionButton(onClick = {
                    editCurso = null
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo curso")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MessageBanner(error, true)
            MessageBanner(message, false)
            if (cursos.isEmpty() && !loading && error == null) {
                Text("No hay cursos registrados.")
            }
            LazyColumn {
                items(cursos) { curso ->
                    ListItem(
                        headlineContent = { Text(curso.nombre) },
                        supportingContent = {
                            Text(
                                buildString {
                                    append(curso.nivel ?: curso.nombre)
                                    append(" Â· ID ${curso.idCurso}")
                                }
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    editCurso = curso
                                    showDialog = true
                                }) {
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

    val colegioId = idColegio
    if (showDialog && colegioId != null) {
        CursoFormDialog(
            curso = editCurso,
            idColegio = colegioId,
            loading = loading,
            onDismiss = { showDialog = false },
            onSave = { req ->
                val edit = editCurso
                if (edit != null) {
                    viewModel.actualizarCurso(edit.idCurso, req)
                } else {
                    viewModel.crearCurso(req)
                }
                showDialog = false
            }
        )
    }

    confirmDelete?.let { c ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Eliminar curso") },
            text = { Text("Â¿Eliminar el curso ${c.nombre}? Solo es posible si no tiene alumnos vinculados.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.eliminarCurso(c.idCurso)
                    confirmDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CursoFormDialog(
    curso: Curso?,
    idColegio: Int,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSave: (CursoRequest) -> Unit
) {
    var nombre by rememberSaveable(curso?.idCurso) { mutableStateOf(curso?.nombre ?: "") }
    var nivel by rememberSaveable(curso?.idCurso) { mutableStateOf(curso?.nivel ?: curso?.nombre ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (curso == null) "Nuevo curso" else "Editar curso") },
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
                    onSave(
                        CursoRequest(
                            idColegio = idColegio,
                            nombre = nombre.trim(),
                            nivel = nivel.trim(),
                            estado = curso?.estado ?: true
                        )
                    )
                },
                enabled = !loading && nombre.isNotBlank() && nivel.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
