package com.example.cloty_administrador.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class MenuItem(val title: String, val route: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    esSuperUsuario: Boolean,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val itemsOperativos = listOf(
        MenuItem("Colegios", "colegios", Icons.Default.School),
        // esta parte es nueva
        MenuItem("Apoderados y alumnos", "gestion_personas", Icons.Default.Groups),
        MenuItem("Cursos", "cursos", Icons.Default.Class),
        MenuItem("Carga CSV", "carga_csv", Icons.Default.Upload),
        MenuItem("Tarjetas NFC", "tarjetas_nfc", Icons.Default.Nfc),
    )
    val items = buildList {
        if (esSuperUsuario) {
            add(MenuItem("Super Usuarios", "super_usuarios", Icons.Default.SupervisedUserCircle))
            add(MenuItem("Administradores", "administradores", Icons.Default.AdminPanelSettings))
        }
        addAll(itemsOperativos)
        add(MenuItem("Mi cuenta", "cuenta", Icons.Default.Person))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (esSuperUsuario) "Panel Cloty — Super" else "Panel Cloty")
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(item.title, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}
