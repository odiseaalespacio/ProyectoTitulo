package com.example.cloty_administrador.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.cloty_administrador.util.ChileValidators

// esto es nuevo
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
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
    val errorMsg = if (showValidation) {
        ChileValidators.mensajeErrorTelefono(value, obligatorio, true)
    } else {
        null
    }
    val error = errorMsg != null
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            val filtrado = raw.filter { it.isDigit() || it == '+' }.take(12)
            onValueChange(filtrado)
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
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
