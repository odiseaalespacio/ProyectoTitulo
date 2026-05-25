package com.example.cloty_colegio.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloty_colegio.data.ClotyRepository
import com.example.cloty_colegio.data.api.ColegioDashboard
import com.example.cloty_colegio.data.api.OperacionPrendaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ClotyViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ClotyRepository(app)

    val tokenFlow = repo.tokenStore.tokenFlow

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _nombreColegio = MutableStateFlow<String?>(null)
    val nombreColegio: StateFlow<String?> = _nombreColegio.asStateFlow()

    private val _dashboard = MutableStateFlow<ColegioDashboard?>(null)
    val dashboard: StateFlow<ColegioDashboard?> = _dashboard.asStateFlow()

    private val _ultimaOperacion = MutableStateFlow<OperacionPrendaResponse?>(null)
    val ultimaOperacion: StateFlow<OperacionPrendaResponse?> = _ultimaOperacion.asStateFlow()

    private val _ultimoUidNfc = MutableStateFlow<String?>(null)
    val ultimoUidNfc: StateFlow<String?> = _ultimoUidNfc.asStateFlow()

    private val _nfcScanCount = MutableStateFlow(0)
    val nfcScanCount: StateFlow<Int> = _nfcScanCount.asStateFlow()

    private val scanMutex = Mutex()

    var ubicacionEscaneo: String = "Secretaría"

    fun onNfcTagDetected(uid: String) {
        _ultimoUidNfc.value = uid
        _nfcScanCount.value++
    }

    fun login(identificador: String, password: String) = launchTask {
        repo.login(identificador, password)
        cargarPerfil()
        cargarDashboard()
    }

    fun activarCuenta(rut: String, email: String, telefono: String, password: String) = launchTask {
        repo.activarCuenta(rut, email, telefono, password)
        cargarPerfil()
        cargarDashboard()
        _message.value = "Cuenta activada exitosamente"
    }

    fun logout() = launchTask {
        repo.logout()
        _dashboard.value = null
        _nombreColegio.value = null
        _ultimaOperacion.value = null
    }

    fun cargarPerfil() = launchTask {
        val me = repo.me()
        _nombreColegio.value = me.username
    }

    fun cargarDashboard() = launchTask {
        _dashboard.value = repo.dashboard()
        _nombreColegio.value = _dashboard.value?.nombreColegio ?: _nombreColegio.value
    }

    fun procesarEscaneo(uid: String) {
        viewModelScope.launch {
            if (!scanMutex.tryLock()) return@launch
            try {
                _loading.value = true
                _error.value = null
                val resultado = repo.escanear(uid, ubicacionEscaneo.ifBlank { null })
                _ultimaOperacion.value = resultado
                _message.value = resultado.mensaje
            } catch (e: Exception) {
                _error.value = parseError(e)
            } finally {
                _loading.value = false
                scanMutex.unlock()
            }
            try { _dashboard.value = repo.dashboard() } catch (_: Exception) {}
        }
    }

    fun cambiarContrasena(actual: String, nueva: String) = launchTask {
        repo.cambiarContrasena(actual, nueva)
        _message.value = "Contraseña actualizada correctamente"
    }

    fun clearError() {
        _error.value = null
    }

    fun clearMessage() {
        _message.value = null
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

    private fun parseError(e: Exception): String {
        val raw = e.message ?: "Error desconocido"
        return when {
            raw.contains("401") || raw.contains("403") -> "Sesión expirada o sin permisos"
            raw.contains("404") -> "Tarjeta no registrada en el sistema"
            raw.contains("400") -> raw.substringAfter("400").ifBlank { "Solicitud inválida" }
            else -> raw
        }
    }
}
