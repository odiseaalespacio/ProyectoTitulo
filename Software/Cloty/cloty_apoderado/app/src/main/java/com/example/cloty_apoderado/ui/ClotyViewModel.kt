package com.example.cloty_apoderado.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloty_apoderado.data.ClotyRepository
import com.example.cloty_apoderado.data.api.Notificacion
import com.example.cloty_apoderado.data.api.PupiloResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun login(identificador: String, password: String) = launchTask {
        repo.login(identificador, password)
        cargarPerfil()
        cargarDatos()
        _message.value = "Sesión iniciada"
    }

    fun logout() = launchTask {
        repo.logout()
        idApoderado = null
        _pupilos.value = emptyList()
        _notificaciones.value = emptyList()
        _nombreUsuario.value = null
    }

    fun cargarPerfil() = launchTask {
        val me = repo.me()
        idApoderado = me.idApoderado
        _nombreUsuario.value = me.username
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

    private fun launchTask(block: suspend () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                block()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _loading.value = false
            }
        }
    }
}
