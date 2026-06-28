package com.example.cloty_administrador.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import com.example.cloty_administrador.data.api.Comuna
import com.example.cloty_administrador.data.api.Region

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbicacionSelector(
    codigoRegion: String?,
    codigoComuna: String?,
    calleNumero: String,
    onRegionChange: (String?) -> Unit,
    onComunaChange: (String?) -> Unit,
    onCalleNumeroChange: (String) -> Unit,
    listarRegiones: suspend () -> List<Region>,
    listarComunas: suspend (String) -> List<Comuna>,
    resolverComuna: suspend (String) -> Comuna?,
    modifier: Modifier = Modifier
) {
    var regiones by remember { mutableStateOf<List<Region>>(emptyList()) }
    var comunas by remember { mutableStateOf<List<Comuna>>(emptyList()) }
    var regionExpanded by remember { mutableStateOf(false) }
    var comunaExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        regiones = listarRegiones()
    }

    LaunchedEffect(codigoRegion) {
        if (codigoRegion.isNullOrBlank()) {
            comunas = emptyList()
        } else {
            comunas = listarComunas(codigoRegion)
        }
    }

    LaunchedEffect(codigoComuna) {
        if (!codigoComuna.isNullOrBlank() && codigoRegion.isNullOrBlank()) {
            val comuna = resolverComuna(codigoComuna)
            if (comuna != null) {
                onRegionChange(comuna.codigoRegion)
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = regionExpanded,
            onExpandedChange = { regionExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            val regionSel = regiones.find { it.codigoRegion == codigoRegion }
            OutlinedTextField(
                value = regionSel?.nombre ?: "Seleccione región",
                onValueChange = {},
                readOnly = true,
                label = { Text("Región") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(regionExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = regionExpanded, onDismissRequest = { regionExpanded = false }) {
                regiones.forEach { region ->
                    DropdownMenuItem(
                        text = { Text(region.nombre) },
                        onClick = {
                            onRegionChange(region.codigoRegion)
                            onComunaChange(null)
                            regionExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = comunaExpanded,
            onExpandedChange = { if (!codigoRegion.isNullOrBlank()) comunaExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            val comunaSel = comunas.find { it.codigoComuna == codigoComuna }
            OutlinedTextField(
                value = comunaSel?.nombre ?: if (codigoRegion.isNullOrBlank()) "Seleccione región primero" else "Seleccione comuna",
                onValueChange = {},
                readOnly = true,
                enabled = !codigoRegion.isNullOrBlank(),
                label = { Text("Comuna") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(comunaExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = comunaExpanded, onDismissRequest = { comunaExpanded = false }) {
                comunas.forEach { comuna ->
                    DropdownMenuItem(
                        text = { Text(comuna.nombre) },
                        onClick = {
                            onComunaChange(comuna.codigoComuna)
                            comunaExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = calleNumero,
            onValueChange = onCalleNumeroChange,
            label = { Text("Calle y número") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
