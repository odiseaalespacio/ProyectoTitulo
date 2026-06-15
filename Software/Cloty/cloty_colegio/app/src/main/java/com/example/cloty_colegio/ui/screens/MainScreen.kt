package com.example.cloty_colegio.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ManageAccounts
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
    onLogout: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var gestionScreen by rememberSaveable { mutableStateOf<String?>(null) }
    val nombreColegio by viewModel.nombreColegio.collectAsState()
    val ultimoUid by viewModel.ultimoUidNfc.collectAsState()
    val scanCount by viewModel.nfcScanCount.collectAsState()
    var lastProcessedScan by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
        viewModel.cargarDashboard()
    }

    LaunchedEffect(tab, gestionScreen) {
        viewModel.clearMessage()
    }

    LaunchedEffect(scanCount) {
        if (!ultimoUid.isNullOrBlank() && scanCount > 0 && scanCount != lastProcessedScan) {
            lastProcessedScan = scanCount
            tab = 1
            gestionScreen = null
            viewModel.procesarEscaneo(ultimoUid!!)
        }
    }

    when (gestionScreen) {
        "personas" -> GestionPersonasScreen(viewModel, PaddingValues()) { gestionScreen = null }
        "cursos" -> GestionCursosScreen(viewModel, PaddingValues()) { gestionScreen = null }
        "colegio" -> EditarColegioScreen(viewModel, PaddingValues()) { gestionScreen = null }
        else -> Scaffold(
            topBar = {
                TopAppBar(title = { Text(nombreColegio ?: "Cloty Colegio") })
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0; gestionScreen = null },
                        icon = { Icon(Icons.Default.Dashboard, null) },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1; gestionScreen = null },
                        icon = { Icon(Icons.Default.Nfc, null) },
                        label = { Text("Escanear") }
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2; gestionScreen = null },
                        icon = { Icon(Icons.Default.ManageAccounts, null) },
                        label = { Text("GestiÃ³n") }
                    )
                    NavigationBarItem(
                        selected = tab == 3,
                        onClick = { tab = 3; gestionScreen = null },
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Cuenta") }
                    )
                }
            }
        ) { padding ->
            when (tab) {
                0 -> DashboardScreen(viewModel, padding)
                1 -> EscanearScreen(viewModel, padding, ultimoUid)
                2 -> GestionMenuScreen(
                    contentPadding = padding,
                    onPersonas = { gestionScreen = "personas" },
                    onCursos = { gestionScreen = "cursos" },
                    onEstablecimiento = { gestionScreen = "colegio" }
                )
                3 -> CuentaScreen(viewModel, padding, onLogout)
            }
        }
    }
}
