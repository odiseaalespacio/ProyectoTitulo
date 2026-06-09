package com.example.cloty_apoderado.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.cloty_apoderado.data.ClotyRepository
import com.example.cloty_apoderado.data.api.Apoderado
import com.example.cloty_apoderado.data.api.ApoderadoRequest
import com.example.cloty_apoderado.data.api.Notificacion
import com.example.cloty_apoderado.data.api.PupiloResumen
import com.example.cloty_apoderado.notification.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ClotyViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ClotyRepository(app)

    val tokenFlow = repo.tokenStore.tokenFlow

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _nombreUsuario = MutableStateFlow<String?>(null)
    val nombreUsuario: StateFlow<String?> = _nombreUsuario.asStateFlow()

    private var idApoderado: Int? = null

    private val _pupilos = MutableStateFlow<List<PupiloResumen>>(emptyList())
    val pupilos: StateFlow<List<PupiloResumen>> = _pupilos.asStateFlow()

    private val _notificaciones = MutableStateFlow<List<Notificacion>>(emptyList())
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    // esta parte es nueva
    private val _apoderado = MutableStateFlow<Apoderado?>(null)
    val apoderado: StateFlow<Apoderado?> = _apoderado.asStateFlow()

    fun login(identificador: String, password: String) = launchTask {
        repo.login(identificador, password)
        cargarPerfil()
        cargarDatos()
        startNotificationPolling()
        _message.value = "Sesión iniciada"
    }

    fun activarCuenta(rut: String, password: String) = launchTask {
        repo.activarCuenta(rut, password)
        cargarPerfil()
        cargarDatos()
        startNotificationPolling()
        _message.value = "Cuenta activada exitosamente"
    }

    fun logout() = launchTask {
        repo.logout()
        stopNotificationPolling()
        idApoderado = null
        _pupilos.value = emptyList()
        _notificaciones.value = emptyList()
        _nombreUsuario.value = null
        _apoderado.value = null
    }

    // esta parte es nueva
    private fun startNotificationPolling() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(getApplication())
            .enqueueUniquePeriodicWork(
                NotificationWorker.TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    private fun stopNotificationPolling() {
        WorkManager.getInstance(getApplication())
            .cancelUniqueWork(NotificationWorker.TAG)
    }

    fun cargarPerfil() = launchTask {
        val me = repo.me()
        idApoderado = me.idApoderado
        _nombreUsuario.value = me.username
        // esta parte es nueva
        me.idApoderado?.let { id ->
            _apoderado.value = repo.obtenerApoderado(id)
        }
    }

    // esta parte es nueva
    fun actualizarMisDatos(email: String, telefono: String, direccion: String) = launchTask {
        val perfil = _apoderado.value ?: throw IllegalStateException("Perfil no cargado")
        val id = idApoderado ?: perfil.idApoderado
        val req = ApoderadoRequest(
            idUsuario = perfil.idUsuario,
            rut = perfil.rut,
            nombres = perfil.nombres,
            apellidos = perfil.apellidos,
            email = email.trim().ifBlank { null },
            telefono = telefono.trim().ifBlank { null },
            direccion = direccion.trim().ifBlank { null }
        )
        _apoderado.value = repo.actualizarApoderado(id, req)
        _message.value = "Datos de contacto actualizados"
    }

    fun cargarDatos() = launchTask {
        _pupilos.value = repo.misPupilos()
        val id = idApoderado ?: repo.me().idApoderado
        idApoderado = id
        if (id != null) {
            _notificaciones.value = repo.notificaciones(id)
        }
    }

    fun marcarLeida(idNotificacion: Int) = launchTask {
        repo.marcarLeida(idNotificacion)
        cargarDatos()
    }

    fun cambiarContrasena(actual: String, nueva: String) = launchTask {
        repo.cambiarContrasena(actual, nueva)
        _message.value = "Contraseña actualizada correctamente"
    }

    fun clearMessages() {
        _error.value = null
        _message.value = null
    }

    // esta parte es nueva
    private fun parseError(e: Exception): String {
        val raw = e.message ?: "Error desconocido"
        val msg = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(raw)?.groupValues?.getOrNull(1)
        return msg ?: when {
            raw.contains("403") || raw.contains("401") -> "Sesión expirada o sin permisos"
            raw.contains("409") -> "No se pudo completar la operación (conflicto de datos)"
            raw.contains("400") -> "Solicitud inválida"
            else -> raw
        }
    }

    private fun launchTask(block: suspend () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                block()
            } catch (e: Exception) {
                _error.value = parseError(e)
            } finally {
                _loading.value = false
            }
        }
    }
}
