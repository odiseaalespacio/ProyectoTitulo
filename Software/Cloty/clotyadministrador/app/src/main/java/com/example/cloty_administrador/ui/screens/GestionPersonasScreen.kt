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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cloty_administrador.data.api.Alumno
import com.example.cloty_administrador.data.api.AlumnoRequest
import com.example.cloty_administrador.data.api.Apoderado
import com.example.cloty_administrador.data.api.ApoderadoRequest
import com.example.cloty_administrador.data.api.Curso
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
fun GestionPersonasScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val colegios by viewModel.colegios.collectAsState()
    val apoderados by viewModel.apoderadosColegio.collectAsState()
    val alumnos by viewModel.alumnosColegio.collectAsState()
    val cursos by viewModel.cursos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var idColegio by rememberSaveable { mutableIntStateOf(0) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showApoderadoDialog by rememberSaveable { mutableStateOf(false) }
    var showAlumnoDialog by rememberSaveable { mutableStateOf(false) }
    var editApoderado by remember { mutableStateOf<Apoderado?>(null) }
    var editAlumno by remember { mutableStateOf<Alumno?>(null) }
    var confirmDeleteApoderado by remember { mutableStateOf<Apoderado?>(null) }
    var confirmDeleteAlumno by remember { mutableStateOf<Alumno?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarColegios() }
    LaunchedEffect(idColegio) {
        if (idColegio > 0) {
            viewModel.cargarApoderadosColegio(idColegio)
            viewModel.cargarAlumnosColegio(idColegio)
            viewModel.cargarCursos(idColegio)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apoderados y alumnos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (idColegio > 0) {
                FloatingActionButton(onClick = {
                    if (tab == 0) {
                        editApoderado = null
                        showApoderadoDialog = true
                    } else {
                        editAlumno = null
                        showAlumnoDialog = true
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                }
            }
        }
    ) { padding ->
        ClotyPullRefresh(
            refreshing = refreshing,
            onRefresh = { viewModel.refrescarDatosColegio(idColegio) },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColegioSelector(colegios, idColegio) { idColegio = it }
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Apoderados") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Alumnos") })
            }
            MessageBanner(error, true)
            MessageBanner(message, false)
            if (idColegio <= 0) {
                Text("Seleccione un colegio para gestionar apoderados y alumnos.")
            } else if (tab == 0) {
                if (apoderados.isEmpty() && !loading && error == null) {
                    Text("No hay apoderados registrados en este colegio.")
                }
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                    items(apoderados) { a ->
                        ListItem(
                            headlineContent = { Text("${a.nombres} ${a.apellidos}") },
                            supportingContent = { Text("RUT ${a.rut}") },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = {
                                        editApoderado = a
                                        showApoderadoDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { confirmDeleteApoderado = a }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                if (alumnos.isEmpty() && !loading && error == null) {
                    Text("No hay alumnos registrados en este colegio.")
                }
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                    items(alumnos) { al ->
                        val cursoNombre = cursos.find { it.idCurso == al.idCurso }?.nombre ?: "Curso ${al.idCurso}"
                        ListItem(
                            headlineContent = { Text("${al.nombres} ${al.apellidos}") },
                            supportingContent = { Text("RUT ${al.rut} · $cursoNombre") },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = {
                                        editAlumno = al
                                        showAlumnoDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { confirmDeleteAlumno = al }) {
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
    }

    if (showApoderadoDialog && idColegio > 0) {
        ApoderadoFormDialog(
            apoderado = editApoderado,
            loading = loading,
            onDismiss = { showApoderadoDialog = false },
            listarRegiones = { viewModel.listarRegiones() },
            listarComunas = { viewModel.listarComunas(it) },
            resolverComuna = { viewModel.obtenerComuna(it) },
            onSave = { req ->
                val edit = editApoderado
                if (edit != null) {
                    viewModel.actualizarApoderado(edit.idApoderado, idColegio, req)
                } else {
                    viewModel.crearApoderado(idColegio, req)
                }
                showApoderadoDialog = false
            }
        )
    }

    if (showAlumnoDialog && idColegio > 0) {
        AlumnoFormDialog(
            alumno = editAlumno,
            idColegio = idColegio,
            apoderados = apoderados,
            cursos = cursos,
            loading = loading,
            onDismiss = { showAlumnoDialog = false },
            onSave = { req ->
                val edit = editAlumno
                if (edit != null) {
                    viewModel.actualizarAlumno(edit.idAlumno, req)
                } else {
                    viewModel.crearAlumno(req)
                }
                showAlumnoDialog = false
            }
        )
    }

    confirmDeleteApoderado?.let { a ->
        AlertDialog(
            onDismissRequest = { confirmDeleteApoderado = null },
            title = { Text("Eliminar apoderado") },
            text = { Text("¿Eliminar a ${a.nombres} ${a.apellidos}? No se puede deshacer.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.eliminarApoderado(a.idApoderado, idColegio)
                    confirmDeleteApoderado = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteApoderado = null }) { Text("Cancelar") } }
        )
    }

    confirmDeleteAlumno?.let { al ->
        AlertDialog(
            onDismissRequest = { confirmDeleteAlumno = null },
            title = { Text("Eliminar alumno") },
            text = { Text("¿Eliminar a ${al.nombres} ${al.apellidos}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.eliminarAlumno(al.idAlumno, idColegio)
                    confirmDeleteAlumno = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteAlumno = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ApoderadoFormDialog(
    apoderado: Apoderado?,
    loading: Boolean,
    onDismiss: () -> Unit,
    listarRegiones: suspend () -> List<com.example.cloty_administrador.data.api.Region>,
    listarComunas: suspend (String) -> List<com.example.cloty_administrador.data.api.Comuna>,
    resolverComuna: suspend (String) -> com.example.cloty_administrador.data.api.Comuna?,
    onSave: (ApoderadoRequest) -> Unit
) {
    var rut by rememberSaveable(apoderado?.idApoderado) { mutableStateOf(apoderado?.rut ?: "") }
    var nombres by rememberSaveable(apoderado?.idApoderado) { mutableStateOf(apoderado?.nombres ?: "") }
    var apellidos by rememberSaveable(apoderado?.idApoderado) { mutableStateOf(apoderado?.apellidos ?: "") }
    var email by rememberSaveable(apoderado?.idApoderado) { mutableStateOf(apoderado?.email ?: "") }
    var telefono by rememberSaveable(apoderado?.idApoderado) { mutableStateOf(apoderado?.telefono ?: "") }
    var codigoRegion by rememberSaveable(apoderado?.idApoderado) { mutableStateOf<String?>(null) }
    var codigoComuna by rememberSaveable(apoderado?.idApoderado) { mutableStateOf(apoderado?.codigoComuna) }
    var calleNumero by rememberSaveable(apoderado?.idApoderado) { mutableStateOf(apoderado?.calleNumero ?: "") }
    var showValidation by rememberSaveable(apoderado?.idApoderado) { mutableStateOf(false) }

    fun errorValidacion(mostrarVacios: Boolean) = ChileValidators.primerMensajeError(
        ChileValidators.mensajeErrorRut(rut, mostrarVacios = mostrarVacios),
        ChileValidators.mensajeErrorEmail(email, obligatorio = true, mostrarVacios = mostrarVacios),
        ChileValidators.mensajeErrorTelefono(telefono, mostrarVacios = mostrarVacios),
        if (nombres.isBlank() && mostrarVacios) "Debe ingresar nombres" else null,
        if (apellidos.isBlank() && mostrarVacios) "Debe ingresar apellidos" else null
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (apoderado == null) "Nuevo apoderado" else "Editar apoderado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RutTextField(rut, { rut = it; showValidation = false }, showValidation = showValidation)
                OutlinedTextField(nombres, { nombres = it; showValidation = false }, label = { Text("Nombres") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(apellidos, { apellidos = it; showValidation = false }, label = { Text("Apellidos") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                EmailTextField(email, { email = it; showValidation = false }, obligatorio = true, showValidation = showValidation)
                TelefonoChilenoTextField(telefono, { telefono = it; showValidation = false }, showValidation = showValidation)
                UbicacionSelector(
                    codigoRegion = codigoRegion,
                    codigoComuna = codigoComuna,
                    calleNumero = calleNumero,
                    onRegionChange = { codigoRegion = it },
                    onComunaChange = { codigoComuna = it },
                    onCalleNumeroChange = { calleNumero = it },
                    listarRegiones = listarRegiones,
                    listarComunas = listarComunas,
                    resolverComuna = resolverComuna
                )
                ValidationMessageBanner(if (showValidation) errorValidacion(true) else null)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val err = errorValidacion(true)
                    if (err != null || nombres.isBlank() || apellidos.isBlank()) {
                        showValidation = true
                        return@Button
                    }
                    onSave(
                        ApoderadoRequest(
                            rut = ChileValidators.normalizarRutParaApi(rut),
                            nombres = nombres.trim(),
                            apellidos = apellidos.trim(),
                            email = email.trim().ifBlank { null },
                            telefono = telefono.trim().ifBlank { null },
                            codigoComuna = codigoComuna?.trim()?.ifBlank { null },
                            calleNumero = calleNumero.trim().ifBlank { null }
                        )
                    )
                },
                enabled = !loading
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlumnoFormDialog(
    alumno: Alumno?,
    idColegio: Int,
    apoderados: List<Apoderado>,
    cursos: List<Curso>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSave: (AlumnoRequest) -> Unit
) {
    var rut by rememberSaveable(alumno?.idAlumno) { mutableStateOf(alumno?.rut ?: "") }
    var nombres by rememberSaveable(alumno?.idAlumno) { mutableStateOf(alumno?.nombres ?: "") }
    var apellidos by rememberSaveable(alumno?.idAlumno) { mutableStateOf(alumno?.apellidos ?: "") }
    var idApoderado by rememberSaveable(alumno?.idAlumno) { mutableIntStateOf(alumno?.idApoderado ?: 0) }
    var idCurso by rememberSaveable(alumno?.idAlumno) { mutableIntStateOf(alumno?.idCurso ?: 0) }
    var apoderadoExpanded by rememberSaveable { mutableStateOf(false) }
    var cursoExpanded by rememberSaveable { mutableStateOf(false) }
    var showValidation by rememberSaveable(alumno?.idAlumno) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (alumno == null) "Nuevo alumno" else "Editar alumno") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RutTextField(rut, { rut = it; showValidation = false }, showValidation = showValidation)
                OutlinedTextField(nombres, { nombres = it; showValidation = false }, label = { Text("Nombres") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(apellidos, { apellidos = it; showValidation = false }, label = { Text("Apellidos") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = apoderadoExpanded, onExpandedChange = { apoderadoExpanded = it }) {
                    val apSel = apoderados.find { it.idApoderado == idApoderado }
                    OutlinedTextField(
                        value = apSel?.let { "${it.nombres} ${it.apellidos}" } ?: "Seleccione apoderado",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Apoderado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(apoderadoExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = apoderadoExpanded, onDismissRequest = { apoderadoExpanded = false }) {
                        apoderados.forEach { a ->
                            DropdownMenuItem(
                                text = { Text("${a.nombres} ${a.apellidos}") },
                                onClick = {
                                    idApoderado = a.idApoderado
                                    apoderadoExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = cursoExpanded, onExpandedChange = { cursoExpanded = it }) {
                    val curSel = cursos.find { it.idCurso == idCurso }
                    OutlinedTextField(
                        value = curSel?.nombre ?: "Seleccione curso",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Curso") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(cursoExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = cursoExpanded, onDismissRequest = { cursoExpanded = false }) {
                        cursos.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.nombre) },
                                onClick = {
                                    idCurso = c.idCurso
                                    cursoExpanded = false
                                }
                            )
                        }
                    }
                }
                ValidationMessageBanner(
                    if (showValidation) {
                        ChileValidators.primerMensajeError(
                            ChileValidators.mensajeErrorRut(rut, mostrarVacios = true),
                            if (nombres.isBlank()) "Debe ingresar nombres" else null,
                            if (apellidos.isBlank()) "Debe ingresar apellidos" else null,
                            if (idApoderado <= 0) "Debe seleccionar un apoderado" else null,
                            if (idCurso <= 0) "Debe seleccionar un curso" else null
                        )
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val err = ChileValidators.mensajeErrorRut(rut, mostrarVacios = true)
                    if (err != null || nombres.isBlank() || apellidos.isBlank() || idApoderado <= 0 || idCurso <= 0) {
                        showValidation = true
                        return@Button
                    }
                    onSave(
                        AlumnoRequest(
                            idColegio = idColegio,
                            idApoderado = idApoderado,
                            idCurso = idCurso,
                            rut = ChileValidators.normalizarRutParaApi(rut),
                            nombres = nombres.trim(),
                            apellidos = apellidos.trim(),
                            estado = alumno?.estado ?: true
                        )
                    )
                },
                enabled = !loading
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
