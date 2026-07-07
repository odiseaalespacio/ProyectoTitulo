package com.example.cloty_apoderado.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloty_apoderado.data.api.PupiloResumen
import com.example.cloty_apoderado.ui.ClotyViewModel
import com.example.cloty_apoderado.ui.components.ClotyPullRefresh

@Composable
fun PupilosScreen(viewModel: ClotyViewModel, contentPadding: PaddingValues) {
    val pupilos by viewModel.pupilos.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()

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
            Text("Mis pupilos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("${pupilos.size} registrado(s)", style = MaterialTheme.typography.bodySmall)
        }
        if (pupilos.isEmpty()) {
            item { Text("No hay pupilos asociados a su cuenta.") }
        } else {
            items(pupilos) { pupilo -> PupiloCard(pupilo) }
        }
    }
    }
}

@Composable
private fun PupiloCard(p: PupiloResumen) {
    Card(Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${p.nombres} ${p.apellidos}", fontWeight = FontWeight.SemiBold)
            Text("RUT ${p.rut}", style = MaterialTheme.typography.bodySmall)
            p.nombreCurso?.let { Text("Curso: $it", style = MaterialTheme.typography.bodySmall) }
            p.nombreColegio?.let { Text("Colegio: $it", style = MaterialTheme.typography.bodySmall) }
            val estado = if (p.estado == true) "Activo" else "Inactivo"
            Text(estado, style = MaterialTheme.typography.labelMedium)
        }
    }
}
