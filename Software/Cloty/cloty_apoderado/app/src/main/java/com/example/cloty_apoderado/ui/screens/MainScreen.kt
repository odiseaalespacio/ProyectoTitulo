package com.example.cloty_apoderado.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cloty_apoderado.ui.ClotyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ClotyViewModel, onLogout: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val nombre by viewModel.nombreUsuario.collectAsState()
    val noLeidas by viewModel.notificaciones.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarDatos() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(nombre ?: "Cloty Apoderado") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.FamilyRestroom, null) },
                    label = { Text("Pupilos") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1; viewModel.cargarDatos() },
                    icon = { Icon(Icons.Default.Notifications, null) },
                    label = {
                        val count = noLeidas.count { it.leida != true }
                        Text(if (count > 0) "Alertas ($count)" else "Alertas")
                    }
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
            0 -> PupilosScreen(viewModel, padding)
            1 -> NotificacionesScreen(viewModel, padding)
            2 -> CuentaScreen(viewModel, padding, onLogout)
        }
    }
}
