package com.example.cloty_colegio.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloty_colegio.data.ClotyRepository
import com.example.cloty_colegio.data.api.ActividadReciente
import com.example.cloty_colegio.data.api.Alumno
import com.example.cloty_colegio.data.api.AlumnoRequest
import com.example.cloty_colegio.data.api.Apoderado
import com.example.cloty_colegio.data.api.ApoderadoRequest
import com.example.cloty_colegio.data.api.Colegio
import com.example.cloty_colegio.data.api.ColegioDashboard
import com.example.cloty_colegio.data.api.ColegioRequest
import com.example.cloty_colegio.data.api.Curso
import com.example.cloty_colegio.data.api.CursoRequest
import com.example.cloty_colegio.data.api.DashboardComunidadDetalle
import com.example.cloty_colegio.data.api.DashboardCursoDetalle
import com.example.cloty_colegio.data.api.DashboardNotificacionesDetalle
import com.example.cloty_colegio.data.api.DashboardPrendasDetalle
import com.example.cloty_colegio.data.api.DashboardTarjetasDetalle
import com.example.cloty_colegio.data.api.OperacionPrendaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.cloty_colegio.util.ApiErrorParser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    private val _nombreColegio = MutableStateFlow<String?>(null)
    val nombreColegio: StateFlow<String?> = _nombreColegio.asStateFlow()

    private val _idColegio = MutableStateFlow<Int?>(null)
    val idColegio: StateFlow<Int?> = _idColegio.asStateFlow()

    private val _colegio = MutableStateFlow<Colegio?>(null)
    val colegio: StateFlow<Colegio?> = _colegio.asStateFlow()

    private val _dashboard = MutableStateFlow<ColegioDashboard?>(null)
    val dashboard: StateFlow<ColegioDashboard?> = _dashboard.asStateFlow()

    private val _dashboardComunidad = MutableStateFlow<DashboardComunidadDetalle?>(null)
    val dashboardComunidad: StateFlow<DashboardComunidadDetalle?> = _dashboardComunidad.asStateFlow()

    private val _dashboardTarjetas = MutableStateFlow<DashboardTarjetasDetalle?>(null)
    val dashboardTarjetas: StateFlow<DashboardTarjetasDetalle?> = _dashboardTarjetas.asStateFlow()

    private val _dashboardPrendas = MutableStateFlow<DashboardPrendasDetalle?>(null)
    val dashboardPrendas: StateFlow<DashboardPrendasDetalle?> = _dashboardPrendas.asStateFlow()

    private val _dashboardNotificaciones = MutableStateFlow<DashboardNotificacionesDetalle?>(null)
    val dashboardNotificaciones: StateFlow<DashboardNotificacionesDetalle?> = _dashboardNotificaciones.asStateFlow()

    private val _dashboardCursos = MutableStateFlow<List<DashboardCursoDetalle>>(emptyList())
    val dashboardCursos: StateFlow<List<DashboardCursoDetalle>> = _dashboardCursos.asStateFlow()

    private val _dashboardCursoDetalle = MutableStateFlow<DashboardCursoDetalle?>(null)
    val dashboardCursoDetalle: StateFlow<DashboardCursoDetalle?> = _dashboardCursoDetalle.asStateFlow()

    private val _dashboardActividad = MutableStateFlow<List<ActividadReciente>>(emptyList())
    val dashboardActividad: StateFlow<List<ActividadReciente>> = _dashboardActividad.asStateFlow()

    private val _ultimaOperacion = MutableStateFlow<OperacionPrendaResponse?>(null)
    val ultimaOperacion: StateFlow<OperacionPrendaResponse?> = _ultimaOperacion.asStateFlow()

    private val _ultimoUidNfc = MutableStateFlow<String?>(null)
    val ultimoUidNfc: StateFlow<String?> = _ultimoUidNfc.asStateFlow()

    private val _nfcScanCount = MutableStateFlow(0)
    val nfcScanCount: StateFlow<Int> = _nfcScanCount.asStateFlow()

    private val _apoderados = MutableStateFlow<List<Apoderado>>(emptyList())
    val apoderados: StateFlow<List<Apoderado>> = _apoderados.asStateFlow()

    private val _alumnos = MutableStateFlow<List<Alumno>>(emptyList())
    val alumnos: StateFlow<List<Alumno>> = _alumnos.asStateFlow()

    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos: StateFlow<List<Curso>> = _cursos.asStateFlow()

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
        onExito()
    }

    fun validarCodigoActivacion(rut: String, codigo: String, onExito: () -> Unit) = launchTask {
        repo.validarCodigoActivacion(rut, codigo)
        onExito()
    }

    fun activarCuenta(rut: String, codigo: String, password: String) = launchTask {
        repo.activarCuenta(rut, codigo, password)
        cargarPerfil()
        cargarDashboard()
    }

    fun logout() = launchTask {
        repo.logout()
        _message.value = null
        _error.value = null
        _dashboard.value = null
        _nombreColegio.value = null
        _idColegio.value = null
        _colegio.value = null
        _ultimaOperacion.value = null
        _apoderados.value = emptyList()
        _alumnos.value = emptyList()
        _cursos.value = emptyList()
    }

    fun cargarPerfil() = launchTask {
        val me = repo.me()
        _idColegio.value = me.idColegio
        _nombreColegio.value = me.username
        me.idColegio?.let { id ->
            _colegio.value = repo.obtenerColegio(id)
            _nombreColegio.value = _colegio.value?.nombre ?: me.username
        }
    }

    fun cargarDashboard() = launchTask { cargarDashboardInternal() }

    fun refrescarDashboard() = launchRefresh { cargarDashboardInternal() }

    private suspend fun cargarDashboardInternal() {
        _dashboard.value = repo.dashboard()
        _nombreColegio.value = _dashboard.value?.nombreColegio ?: _nombreColegio.value
        _idColegio.value = _dashboard.value?.idColegio ?: _idColegio.value
        cargarDashboardDetalleInternal()
    }

    private suspend fun cargarDashboardDetalleInternal() {
        _dashboardComunidad.value = repo.dashboardComunidad()
        _dashboardTarjetas.value = repo.dashboardTarjetas()
        _dashboardPrendas.value = repo.dashboardPrendas()
        _dashboardNotificaciones.value = repo.dashboardNotificaciones()
        _dashboardCursos.value = repo.dashboardCursos()
        _dashboardActividad.value = repo.dashboardActividad()
    }

    fun cargarDashboardComunidad() = launchTask {
        _dashboardComunidad.value = repo.dashboardComunidad()
    }

    fun refrescarDashboardComunidad() = launchRefresh {
        _dashboardComunidad.value = repo.dashboardComunidad()
    }

    fun cargarDashboardTarjetas() = launchTask {
        _dashboardTarjetas.value = repo.dashboardTarjetas()
    }

    fun refrescarDashboardTarjetas() = launchRefresh {
        _dashboardTarjetas.value = repo.dashboardTarjetas()
    }

    fun cargarDashboardPrendas() = launchTask {
        _dashboardPrendas.value = repo.dashboardPrendas()
    }

    fun refrescarDashboardPrendas() = launchRefresh {
        _dashboardPrendas.value = repo.dashboardPrendas()
    }

    fun cargarDashboardNotificaciones() = launchTask {
        _dashboardNotificaciones.value = repo.dashboardNotificaciones()
    }

    fun refrescarDashboardNotificaciones() = launchRefresh {
        _dashboardNotificaciones.value = repo.dashboardNotificaciones()
    }

    fun cargarDashboardCursos() = launchTask {
        _dashboardCursos.value = repo.dashboardCursos()
    }

    fun refrescarDashboardCursos() = launchRefresh {
        _dashboardCursos.value = repo.dashboardCursos()
    }

    fun cargarDashboardCurso(idCurso: Int) = launchTask {
        _dashboardCursoDetalle.value = repo.dashboardCurso(idCurso)
    }

    fun refrescarDashboardCurso(idCurso: Int) = launchRefresh {
        _dashboardCursoDetalle.value = repo.dashboardCurso(idCurso)
    }

    fun cargarDashboardActividad() = launchTask {
        _dashboardActividad.value = repo.dashboardActividad()
    }

    fun refrescarDashboardActividad() = launchRefresh {
        _dashboardActividad.value = repo.dashboardActividad()
    }

    fun cargarGestion() = launchTask {
        val id = requireColegioId()
        cargarGestionInternal(id)
    }

    fun refrescarGestion() = launchRefresh {
        val id = requireColegioId()
        cargarGestionInternal(id)
    }

    fun cargarColegio() = launchTask {
        val id = requireColegioId()
        _colegio.value = repo.obtenerColegio(id)
        _nombreColegio.value = _colegio.value?.nombre
    }

    fun refrescarColegio() = launchRefresh {
        val id = requireColegioId()
        _colegio.value = repo.obtenerColegio(id)
        _nombreColegio.value = _colegio.value?.nombre
    }

    fun actualizarColegio(req: ColegioRequest) = launchTask {
        val id = requireColegioId()
        _colegio.value = repo.actualizarColegio(id, req)
        _nombreColegio.value = _colegio.value?.nombre
        _message.value = "Datos del establecimiento actualizados"
    }

    fun crearApoderado(req: ApoderadoRequest) = launchTask {
        val id = requireColegioId()
        repo.crearApoderadoEnColegio(id, req)
        _message.value = "Apoderado registrado"
        cargarGestionInternal(id)
    }

    fun actualizarApoderado(id: Int, req: ApoderadoRequest) = launchTask {
        val idColegio = requireColegioId()
        repo.actualizarApoderado(id, req)
        _message.value = "Apoderado actualizado"
        cargarGestionInternal(idColegio)
    }

    fun eliminarApoderado(id: Int) = launchTask {
        val idColegio = requireColegioId()
        repo.eliminarApoderado(id)
        _message.value = "Apoderado eliminado"
        cargarGestionInternal(idColegio)
    }

    fun crearAlumno(req: AlumnoRequest) = launchTask {
        val idColegio = requireColegioId()
        repo.crearAlumno(req)
        _message.value = "Alumno registrado"
        cargarGestionInternal(idColegio)
    }

    fun actualizarAlumno(id: Int, req: AlumnoRequest) = launchTask {
        val idColegio = requireColegioId()
        repo.actualizarAlumno(id, req)
        _message.value = "Alumno actualizado"
        cargarGestionInternal(idColegio)
    }

    fun eliminarAlumno(id: Int) = launchTask {
        val idColegio = requireColegioId()
        repo.eliminarAlumno(id)
        _message.value = "Alumno eliminado"
        cargarGestionInternal(idColegio)
    }

    fun crearCurso(req: CursoRequest) = launchTask {
        val idColegio = requireColegioId()
        repo.crearCurso(req)
        _message.value = "Curso creado"
        cargarGestionInternal(idColegio)
    }

    fun actualizarCurso(id: Int, req: CursoRequest) = launchTask {
        val idColegio = requireColegioId()
        repo.actualizarCurso(id, req)
        _message.value = "Curso actualizado"
        cargarGestionInternal(idColegio)
    }

    fun eliminarCurso(id: Int) = launchTask {
        val idColegio = requireColegioId()
        repo.eliminarCurso(id)
        _message.value = "Curso eliminado"
        cargarGestionInternal(idColegio)
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
                _error.value = ApiErrorParser.mensaje(e)
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

    fun clearMessages() {
        _error.value = null
        _message.value = null
    }

    private suspend fun cargarGestionInternal(idColegio: Int) {
        _apoderados.value = repo.listarApoderadosPorColegio(idColegio)
        _alumnos.value = repo.listarAlumnosPorColegio(idColegio)
        _cursos.value = repo.listarCursos(idColegio)
    }

    private fun requireColegioId(): Int {
        val id = _idColegio.value
        if (id == null || id <= 0) {
            throw IllegalStateException("No se pudo identificar el colegio de la sesión")
        }
        return id
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
