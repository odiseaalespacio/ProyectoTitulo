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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloty_colegio.data.api.ActividadReciente
import com.example.cloty_colegio.data.api.ColegioDashboard
import com.example.cloty_colegio.ui.ClotyViewModel

@Composable
fun DashboardScreen(viewModel: ClotyViewModel, contentPadding: PaddingValues) {
    val dashboard by viewModel.dashboard.collectAsState()
    val nombreColegio by viewModel.nombreColegio.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                nombreColegio ?: "Mi colegio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Resumen general", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            if (dashboard != null) {
                StatsGrid(dashboard!!)
            }
        }
        item {
            Text("Últimas acciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        if (dashboard?.ultimasAcciones.isNullOrEmpty()) {
            item { Text("Sin actividad reciente", style = MaterialTheme.typography.bodySmall) }
        } else {
            items(dashboard!!.ultimasAcciones!!) { actividad ->
                ActividadCard(actividad)
            }
        }
    }
}

@Composable
private fun StatsGrid(d: ColegioDashboard) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Encontradas hoy", d.prendasEncontradasHoy.toString(), Icons.Default.Search, Modifier.weight(1f))
            StatCard("Entregadas hoy", d.prendasEntregadasHoy.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Total encontradas", d.prendasEncontradasTotal.toString(), Icons.Default.Search, Modifier.weight(1f))
            StatCard("Total entregadas", d.prendasEntregadasTotal.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Tarjetas activas", d.tarjetasActivas.toString(), Icons.Default.CreditCard, Modifier.weight(1f))
            StatCard("Notificaciones", d.notificacionesEnviadas.toString(), Icons.Default.Notifications, Modifier.weight(1f))
        }
        StatCard(
            "Alumnos con tarjeta NFC",
            d.alumnosConTarjeta.toString(),
            Icons.Default.CreditCard,
            Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ActividadCard(a: ActividadReciente) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(a.accion ?: a.tipoEvento ?: "Evento", fontWeight = FontWeight.SemiBold)
                Text(a.fecha?.take(16)?.replace('T', ' ') ?: "", style = MaterialTheme.typography.labelSmall)
            }
            a.nombreAlumno?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            val detalle = listOfNotNull(a.nombreCurso, a.tipoPrenda, a.uidNfc?.let { "UID $it" })
                .joinToString(" · ")
            if (detalle.isNotBlank()) {
                Text(detalle, style = MaterialTheme.typography.bodySmall)
            }
            a.descripcion?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
