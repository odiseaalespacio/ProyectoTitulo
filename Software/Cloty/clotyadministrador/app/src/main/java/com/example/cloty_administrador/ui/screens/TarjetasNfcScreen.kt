package com.example.cloty_administrador.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.ClotyViewModel.Companion.TARJETAS_POR_ALUMNO
import com.example.cloty_administrador.ui.components.MessageBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetasNfcScreen(
    viewModel: ClotyViewModel,
    onBack: () -> Unit,
    ultimoUid: String?
) {
    val colegios by viewModel.colegios.collectAsState()
    val cursos by viewModel.cursos.collectAsState()
    val pendientes by viewModel.alumnosPendientes.collectAsState()
    val tarjetasDelActual by viewModel.tarjetasDelActual.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    var idColegio by rememberSaveable { mutableIntStateOf(0) }
    var idCurso by rememberSaveable { mutableIntStateOf(0) }
    var modoLote by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargarColegios() }
    LaunchedEffect(idColegio) {
        if (idColegio > 0) viewModel.cargarCursos(idColegio)
        idCurso = 0
        modoLote = false
    }

    var ultimoProcesado by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(ultimoUid) {
        if (modoLote && !ultimoUid.isNullOrBlank() && ultimoUid != ultimoProcesado) {
            ultimoProcesado = ultimoUid
            viewModel.registrarUidNfc(ultimoUid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tarjetas NFC") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColegioSelector(colegios, idColegio) { idColegio = it }
            if (idColegio > 0) {
                CursoSelector(cursos, idCurso) { idCurso = it }
            }
            Button(
                onClick = {
                    if (idCurso > 0) {
                        viewModel.prepararCargaNfc(idCurso)
                        modoLote = true
                    }
                },
                enabled = idCurso > 0,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Iniciar carga por lote") }

            val actual = viewModel.alumnoActualNfc()
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Alumno actual", fontWeight = FontWeight.Bold)
                    if (actual != null) {
                        Text("${actual.nombres} ${actual.apellidos}")
                        Text("RUT ${actual.rut}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Tarjeta ${tarjetasDelActual + 1} de $TARJETAS_POR_ALUMNO",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text("Seleccione curso e inicie el lote", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Alumnos pendientes: ${pendientes.size}")
                    if (!ultimoUid.isNullOrBlank()) {
                        Text("Último UID: $ultimoUid", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Text(
                "Se asignan $TARJETAS_POR_ALUMNO tarjetas por alumno. Acerque cada tarjeta NFC al dispositivo.",
                style = MaterialTheme.typography.bodySmall
            )
            MessageBanner(error, true)
            MessageBanner(message, false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoSelector(
    cursos: List<com.example.cloty_administrador.data.api.Curso>,
    selectedId: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selected = cursos.find { it.idCurso == selectedId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.nombre ?: "Seleccione curso",
            onValueChange = {},
            readOnly = true,
            label = { Text("Curso") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cursos.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.nombre) },
                    onClick = {
                        onSelected(c.idCurso)
                        expanded = false
                    }
                )
            }
        }
    }
}
