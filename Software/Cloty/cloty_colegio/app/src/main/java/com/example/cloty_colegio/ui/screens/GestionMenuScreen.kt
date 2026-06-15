package com.example.cloty_colegio.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun GestionMenuScreen(
    contentPadding: PaddingValues,
    onPersonas: () -> Unit,
    onCursos: () -> Unit,
    onEstablecimiento: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(contentPadding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("GestiÃ³n", style = MaterialTheme.typography.titleLarge)
        Text(
            "Administre apoderados, alumnos, cursos y datos del establecimiento.",
            style = MaterialTheme.typography.bodyMedium
        )
        GestionCard("Apoderados y alumnos", Icons.Default.Groups, onPersonas)
        GestionCard("Cursos", Icons.Default.Class, onCursos)
        GestionCard("Mi establecimiento", Icons.Default.School, onEstablecimiento)
    }
}

@Composable
private fun GestionCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
    }
}
