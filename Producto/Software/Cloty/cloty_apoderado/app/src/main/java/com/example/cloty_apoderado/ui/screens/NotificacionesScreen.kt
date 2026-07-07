package com.example.cloty_apoderado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloty_apoderado.data.api.Notificacion
import com.example.cloty_apoderado.ui.ClotyViewModel
import com.example.cloty_apoderado.ui.components.ClotyPullRefresh

@Composable
fun NotificacionesScreen(viewModel: ClotyViewModel, contentPadding: PaddingValues) {
    val notificaciones by viewModel.notificaciones.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val noLeidas = notificaciones.count { it.leida != true }

    ClotyPullRefresh(
        refreshing = refreshing,
        onRefresh = { viewModel.refrescarDatos() },
        modifier = Modifier.fillMaxSize().padding(contentPadding)
    ) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Notificaciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (noLeidas > 0) {
                Text("$noLeidas sin leer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (notificaciones.isEmpty()) {
            item {
                Text(
                    "Cuando el colegio encuentre una prenda con tarjeta NFC de su pupilo, aparecerá aquí.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(notificaciones, key = { it.idNotificacion ?: 0 }) { n ->
                NotificacionCard(n) {
                    n.idNotificacion?.let { viewModel.marcarLeida(it) }
                }
            }
        }
    }
    }
}

@Composable
private fun NotificacionCard(n: Notificacion, onTap: () -> Unit) {
    val leida = n.leida == true
    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (leida) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        androidx.compose.foundation.layout.Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(n.titulo ?: "Notificación", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (!leida) Badge { Text("Nueva") }
            }
            Text(n.mensaje ?: "", style = MaterialTheme.typography.bodyMedium)
            n.fechaEnvio?.let {
                Text(it.take(16).replace('T', ' '), style = MaterialTheme.typography.labelSmall)
            }
            n.estado?.let { Text("Estado: $it", style = MaterialTheme.typography.labelSmall) }
        }
    }
}
