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
import com.example.cloty_apoderado.util.ApiErrorParser
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ClotyViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ClotyRepository(app)

    val tokenFlow = repo.tokenStore.tokenFlow

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

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

    private val _apoderado = MutableStateFlow<Apoderado?>(null)
    val apoderado: StateFlow<Apoderado?> = _apoderado.asStateFlow()

    fun login(identificador: String, password: String) = launchTask {
        repo.login(identificador, password)
        cargarPerfil()
        cargarDatos()
        startNotificationPolling()
    }

    private val _correoActivacion = MutableStateFlow<String?>(null)
    val correoActivacion: StateFlow<String?> = _correoActivacion.asStateFlow()

    fun limpiarActivacion() {
        _correoActivacion.value = null
        _error.value = null
        _message.value = null
    }

    fun limpiarRecuperacion() {
        _correoActivacion.value = null
        _error.value = null
        _message.value = null
    }

    fun solicitarRecuperacionContrasena(rut: String, onExito: () -> Unit) = launchTask {
        val resp = repo.solicitarRecuperacionContrasena(rut)
        _correoActivacion.value = resp.correoEnmascarado
        onExito()
    }

    fun restablecerContrasena(rut: String, codigo: String, password: String, onExito: () -> Unit) = launchTask {
        repo.restablecerContrasena(rut, codigo, password)
        _message.value = "Contraseña actualizada. Ya puede iniciar sesión."
        onExito()
    }

    fun solicitarCodigoActivacion(rut: String, onExito: () -> Unit) = launchTask {
        val resp = repo.solicitarCodigoActivacion(rut)
        _correoActivacion.value = resp.correoEnmascarado
        _message.value = resp.mensaje ?: "Código enviado al correo registrado"
        onExito()
    }

    fun validarCodigoActivacion(rut: String, codigo: String, onExito: () -> Unit) = launchTask {
        repo.validarCodigoActivacion(rut, codigo)
        onExito()
    }

    fun activarCuenta(rut: String, codigo: String, password: String) = launchTask {
        repo.activarCuenta(rut, codigo, password)
        cargarPerfil()
        cargarDatos()
        startNotificationPolling()
    }

    fun logout() = launchTask {
        repo.logout()
        clearMessages()
        stopNotificationPolling()
        idApoderado = null
        _pupilos.value = emptyList()
        _notificaciones.value = emptyList()
        _nombreUsuario.value = null
        _apoderado.value = null
    }

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
        me.idApoderado?.let { id ->
            _apoderado.value = repo.obtenerApoderado(id)
        }
    }

    fun actualizarMisDatos(
        email: String,
        telefono: String,
        codigoComuna: String?,
        calleNumero: String
    ) = launchTask {
        val perfil = _apoderado.value ?: throw IllegalStateException("Perfil no cargado")
        val id = idApoderado ?: perfil.idApoderado
        val req = ApoderadoRequest(
            idUsuario = perfil.idUsuario,
            rut = perfil.rut,
            nombres = perfil.nombres,
            apellidos = perfil.apellidos,
            email = email.trim().ifBlank { null },
            telefono = telefono.trim().ifBlank { null },
            codigoComuna = codigoComuna?.trim()?.ifBlank { null },
            calleNumero = calleNumero.trim().ifBlank { null }
        )
        _apoderado.value = repo.actualizarApoderado(id, req)
        _message.value = "Datos de contacto actualizados"
    }

    fun cargarDatos() = launchTask { cargarDatosInternal() }

    fun refrescarDatos() = launchRefresh { cargarDatosInternal() }

    private suspend fun cargarDatosInternal() {
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
                _error.value = ApiErrorParser.mensaje(e)
            } finally {
                _loading.value = false
            }
        }
    }

    private fun launchRefresh(block: suspend () -> Unit) {
        viewModelScope.launch {
            if (_refreshing.value) return@launch
            _refreshing.value = true
            _error.value = null
            try {
                block()
            } catch (e: Exception) {
                _error.value = ApiErrorParser.mensaje(e)
            } finally {
                _refreshing.value = false
            }
        }
    }

    suspend fun listarRegiones() = repo.listarRegiones()

    suspend fun listarComunas(codigoRegion: String) = repo.listarComunas(codigoRegion)

    suspend fun obtenerComuna(codigoComuna: String) = repo.obtenerComuna(codigoComuna)
}
