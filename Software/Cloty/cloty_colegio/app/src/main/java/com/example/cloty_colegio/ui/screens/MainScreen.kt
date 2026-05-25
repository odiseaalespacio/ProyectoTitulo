package com.example.cloty_colegio.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import com.example.cloty_colegio.ui.ClotyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ClotyViewModel,
    onLogout: () -> Unit,
    ultimoUid: String?
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val nombreColegio by viewModel.nombreColegio.collectAsState()
    var ultimoProcesado by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarDashboard()
    }

    LaunchedEffect(ultimoUid) {
        if (!ultimoUid.isNullOrBlank() && ultimoUid != ultimoProcesado) {
            ultimoProcesado = ultimoUid
            tab = 1
            viewModel.procesarEscaneo(ultimoUid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreColegio ?: "Cloty Colegio") },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Nfc, null) },
                    label = { Text("Escanear") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Cuenta") }
                )
            }
        }
    ) { padding ->
        when (tab) {
            0 -> DashboardScreen(viewModel, padding)
            1 -> EscanearScreen(viewModel, padding, ultimoUid)
            2 -> CuentaScreen(viewModel, padding, onLogout)
        }
    }
}
