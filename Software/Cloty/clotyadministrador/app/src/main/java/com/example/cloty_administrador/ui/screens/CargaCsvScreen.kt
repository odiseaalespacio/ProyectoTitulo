package com.example.cloty_administrador.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cloty_administrador.ui.ClotyViewModel
import com.example.cloty_administrador.ui.components.MessageBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargaCsvScreen(viewModel: ClotyViewModel, onBack: () -> Unit) {
    val colegios by viewModel.colegios.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    val ultimaCarga by viewModel.ultimaCarga.collectAsState()
    var idColegio by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.cargarColegios() }

  val launcherApoderados = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && idColegio > 0) viewModel.importarApoderados(idColegio, uri)
    }
    val launcherAlumnos = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && idColegio > 0) viewModel.importarAlumnos(idColegio, uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carga masiva CSV") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColegioSelector(colegios, idColegio) { idColegio = it }
            Text(
                "1) Apoderados: rut, nombres, apellidos, email, telefono, direccion\n" +
                    "2) Alumnos: rut_alumno, nombres, apellidos, nombre_curso, rut_apoderado, estado",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = { launcherApoderados.launch("text/*") },
                enabled = idColegio > 0,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Importar apoderados (.csv)") }
            Button(
                onClick = { launcherAlumnos.launch("text/*") },
                enabled = idColegio > 0,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Importar alumnos y cursos (.csv)") }
            MessageBanner(error, true)
            MessageBanner(message, false)
            ultimaCarga?.mensajes?.takeLast(15)?.forEach { linea ->
                Text("• $linea", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
