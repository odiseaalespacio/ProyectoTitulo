package com.example.cloty_colegio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cloty_colegio.ui.ClotyViewModel
import com.example.cloty_colegio.ui.components.MessageBanner

@Composable
fun EscanearScreen(
    viewModel: ClotyViewModel,
    contentPadding: PaddingValues,
    ultimoUid: String?
) {
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val ultima by viewModel.ultimaOperacion.collectAsState()
    var ubicacion by rememberSaveable { mutableStateOf(viewModel.ubicacionEscaneo) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(64.dp))
                } else {
                    Icon(Icons.Default.Nfc, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    "Acerque la tarjeta NFC",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "1.ª lectura: prenda encontrada → notifica al apoderado\n" +
                        "2.ª lectura (misma tarjeta): marca como entregada",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                ultimoUid?.let {
                    Text("UID detectado: $it", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        OutlinedTextField(
            value = ubicacion,
            onValueChange = {
                ubicacion = it
                viewModel.ubicacionEscaneo = it
            },
            label = { Text("Ubicación (ej. Secretaría, Pérdidas)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        MessageBanner(error, true)

        ultima?.let { op ->
            val color = when (op.accion) {
                "ENTREGADA" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = color)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (op.accion == "ENTREGADA") "Prenda entregada" else "Prenda encontrada",
                        fontWeight = FontWeight.Bold
                    )
                    op.nombreAlumno?.let { Text(it) }
                    op.nombreCurso?.let { Text("Curso: $it", style = MaterialTheme.typography.bodySmall) }
                    op.nombreApoderado?.let { Text("Apoderado: $it", style = MaterialTheme.typography.bodySmall) }
                    op.mensaje?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}
