package com.example.cloty_colegio.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.cloty_colegio.ui.theme.clotyFieldColors
import com.example.cloty_colegio.util.ChileValidators

// esto es nuevo
@Composable
fun ValidationMessageBanner(message: String?) {
    MessageBanner(message, isError = true)
}

// esto es nuevo
@Composable
fun RutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    label: String = "RUT",
    obligatorio: Boolean = true,
    showValidation: Boolean = false
) {
    val errorMsg = ChileValidators.mensajeErrorRut(value, obligatorio, showValidation)
    val error = errorMsg != null
    val fieldColors = clotyFieldColors()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        colors = fieldColors,
        isError = error,
        supportingText = {
            Text(
                text = errorMsg ?: ChileValidators.HINT_RUT,
                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

// esto es nuevo
@Composable
fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    label: String = "Email",
    obligatorio: Boolean = false,
    showValidation: Boolean = false
) {
    val errorMsg = ChileValidators.mensajeErrorEmail(value, obligatorio, showValidation)
    val error = errorMsg != null
    val fieldColors = clotyFieldColors()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        colors = fieldColors,
        isError = error,
        supportingText = if (error) {
            {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}

// esto es nuevo
@Composable
fun TelefonoChilenoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    label: String = "Teléfono",
    obligatorio: Boolean = false,
    showValidation: Boolean = false
) {
    val errorMsg = ChileValidators.mensajeErrorTelefono(value, obligatorio, showValidation)
    val error = errorMsg != null
    val fieldColors = clotyFieldColors()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        colors = fieldColors,
        isError = error,
        supportingText = {
            Text(
                text = errorMsg ?: ChileValidators.HINT_TELEFONO,
                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
}
