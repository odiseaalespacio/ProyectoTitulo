package com.example.cloty_administrador.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloty_administrador.data.ClotyRepository
import com.example.cloty_administrador.data.TokenStore
import com.example.cloty_administrador.data.api.Administrador
import com.example.cloty_administrador.data.api.AdministradorCompletoRequest
import com.example.cloty_administrador.data.api.Alumno
import com.example.cloty_administrador.data.api.CargaMasivaResult
import com.example.cloty_administrador.data.api.Colegio
import com.example.cloty_administrador.data.api.ColegioRequest
import com.example.cloty_administrador.data.api.Curso
import com.example.cloty_administrador.data.api.CursoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClotyViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ClotyRepository(app)

    val tokenFlow = repo.tokenStore.tokenFlow
    val rolFlow = repo.tokenStore.rolFlow

    val esSuperUsuario: Boolean
        get() = _rolActual.value == TokenStore.ROL_SUPER_USUARIO

    private val _rolActual = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repo.refrescarRolSesion()?.let { _rolActual.value = it }
        }
    }

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _administradores = MutableStateFlow<List<Administrador>>(emptyList())
    val administradores = _administradores.asStateFlow()

    private val _colegios = MutableStateFlow<List<Colegio>>(emptyList())
    val colegios = _colegios.asStateFlow()

    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos = _cursos.asStateFlow()

    private val _alumnosPendientes = MutableStateFlow<List<Alumno>>(emptyList())
    val alumnosPendientes = _alumnosPendientes.asStateFlow()

    private val _ultimaCarga = MutableStateFlow<CargaMasivaResult?>(null)
    val ultimaCarga = _ultimaCarga.asStateFlow()

    fun cambiarContrasena(actual: String, nueva: String) = launchTask {
        repo.cambiarContrasena(actual, nueva)
        _message.value = "Contraseña actualizada correctamente"
    }

    fun clearMessages() {
        _error.value = null
        _message.value = null
    }

    fun login(identificador: String, password: String) = launchTask {
        val rol = repo.login(identificador, password)
        _rolActual.value = rol
        _message.value = if (rol == TokenStore.ROL_SUPER_USUARIO) {
            "Sesión iniciada (super usuario)"
        } else {
            "Sesión iniciada"
        }
    }

    fun logout() = launchTask {
        repo.logout()
        _rolActual.value = null
        _administradores.value = emptyList()
        _colegios.value = emptyList()
        _cursos.value = emptyList()
        _alumnosPendientes.value = emptyList()
    }

    fun cargarAdministradores() = launchTask {
        _administradores.value = repo.listarAdministradores()
    }

    fun crearAdministrador(req: AdministradorCompletoRequest) = launchTask {
        repo.crearAdministrador(req)
        _message.value = "Administrador registrado"
        cargarAdministradores()
    }

    fun cargarColegios() = launchTask {
        _colegios.value = repo.listarColegios()
    }

    fun crearColegio(req: ColegioRequest) = launchTask {
        repo.crearColegio(req)
        _message.value = "Colegio registrado"
        cargarColegios()
    }

    fun cargarCursos(idColegio: Int) = launchTask {
        _cursos.value = repo.listarCursos(idColegio)
    }

    fun crearCurso(req: CursoRequest) = launchTask {
        repo.crearCurso(req)
        _message.value = "Curso creado"
        cargarCursos(req.idColegio)
    }

    fun importarApoderados(idColegio: Int, uri: Uri) = launchTask {
        _ultimaCarga.value = repo.importarApoderados(idColegio, uri, getApplication())
        _message.value = resumenCarga(_ultimaCarga.value)
    }

    fun importarAlumnos(idColegio: Int, uri: Uri) = launchTask {
        _ultimaCarga.value = repo.importarAlumnos(idColegio, uri, getApplication())
        _message.value = resumenCarga(_ultimaCarga.value)
    }

    fun prepararCargaNfc(idCurso: Int) = launchTask {
        val alumnos = repo.listarAlumnosPorCurso(idCurso)
        val pendientes = alumnos.filter { !repo.alumnoTieneTarjeta(it.idAlumno) }
        _alumnosPendientes.value = pendientes
        _message.value = if (pendientes.isEmpty()) {
            "Todos los alumnos del curso ya tienen tarjeta"
        } else {
            "Listo: ${pendientes.size} alumnos pendientes. Acerque la tarjeta NFC."
        }
    }

    fun alumnoActualNfc(): Alumno? = _alumnosPendientes.value.firstOrNull()

    fun registrarUidNfc(uid: String) = launchTask {
        val alumno = alumnoActualNfc()
            ?: throw IllegalStateException("No hay alumnos pendientes de tarjeta")
        repo.asignarTarjeta(alumno.idAlumno, uid)
        val restantes = _alumnosPendientes.value.drop(1)
        _alumnosPendientes.value = restantes
        _message.value = "Tarjeta $uid → ${alumno.nombres} ${alumno.apellidos}. " +
            if (restantes.isEmpty()) "Lote completado." else "Siguiente: ${restantes.first().nombres}"
    }

    private fun resumenCarga(r: CargaMasivaResult?): String {
        if (r == null) return ""
        return "Filas: ${r.filasLeidas} | Creados: ${r.creados} | Omitidos: ${r.omitidos} | Errores: ${r.errores}"
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
