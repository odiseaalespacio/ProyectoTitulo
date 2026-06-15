package com.example.cloty_administrador.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloty_administrador.data.ClotyRepository
import com.example.cloty_administrador.data.TokenStore
import com.example.cloty_administrador.data.api.Administrador
import com.example.cloty_administrador.data.api.AdministradorCompletoRequest
import com.example.cloty_administrador.data.api.AdministradorRequest
import com.example.cloty_administrador.data.api.SuperUsuario
import com.example.cloty_administrador.data.api.SuperUsuarioCompletoRequest
import com.example.cloty_administrador.data.api.SuperUsuarioRequest
import com.example.cloty_administrador.data.api.Alumno
import com.example.cloty_administrador.data.api.AlumnoRequest
import com.example.cloty_administrador.data.api.Apoderado
import com.example.cloty_administrador.data.api.ApoderadoRequest
import com.example.cloty_administrador.data.api.CargaMasivaResult
import com.example.cloty_administrador.data.api.Colegio
import com.example.cloty_administrador.data.api.ColegioRequest
import com.example.cloty_administrador.data.api.Curso
import com.example.cloty_administrador.data.api.CursoRequest
import com.example.cloty_administrador.data.api.Usuario
import com.example.cloty_administrador.data.api.UsuarioUpdateRequest
import com.example.cloty_administrador.util.ApiErrorParser
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

    private val _usuariosPorId = MutableStateFlow<Map<Int, Usuario>>(emptyMap())
    val usuariosPorId = _usuariosPorId.asStateFlow()

    private val _superUsuarios = MutableStateFlow<List<SuperUsuario>>(emptyList())
    val superUsuarios = _superUsuarios.asStateFlow()

    private val _administradores = MutableStateFlow<List<Administrador>>(emptyList())
    val administradores = _administradores.asStateFlow()

    private val _colegios = MutableStateFlow<List<Colegio>>(emptyList())
    val colegios = _colegios.asStateFlow()

    private val _apoderadosColegio = MutableStateFlow<List<Apoderado>>(emptyList())
    val apoderadosColegio = _apoderadosColegio.asStateFlow()

    private val _alumnosColegio = MutableStateFlow<List<Alumno>>(emptyList())
    val alumnosColegio = _alumnosColegio.asStateFlow()

    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos = _cursos.asStateFlow()

    private val _alumnosPendientes = MutableStateFlow<List<Alumno>>(emptyList())
    val alumnosPendientes = _alumnosPendientes.asStateFlow()

    private val _tarjetasDelActual = MutableStateFlow(0)
    val tarjetasDelActual: StateFlow<Int> = _tarjetasDelActual.asStateFlow()

    companion object {
        const val TARJETAS_POR_ALUMNO = 3
    }

    private val _ultimaCarga = MutableStateFlow<CargaMasivaResult?>(null)
    val ultimaCarga = _ultimaCarga.asStateFlow()

    fun cambiarContrasena(actual: String, nueva: String) = launchTask {
        repo.cambiarContrasena(actual, nueva)
        _message.value = "ContraseÃ±a actualizada correctamente"
    }

    fun clearMessages() {
        _error.value = null
        _message.value = null
    }

    private val _correoRecuperacion = MutableStateFlow<String?>(null)
    val correoRecuperacion: StateFlow<String?> = _correoRecuperacion.asStateFlow()

    fun limpiarRecuperacion() {
        _correoRecuperacion.value = null
        _error.value = null
    }

    fun solicitarRecuperacionContrasena(rut: String, onExito: () -> Unit) = launchTask {
        val resp = repo.solicitarRecuperacionContrasena(rut)
        _correoRecuperacion.value = resp.correoEnmascarado
        _message.value = resp.mensaje ?: "CÃ³digo enviado al correo registrado"
        onExito()
    }

    fun restablecerContrasena(rut: String, codigo: String, password: String, onExito: () -> Unit) = launchTask {
        repo.restablecerContrasena(rut, codigo, password)
        _message.value = "ContraseÃ±a actualizada. Ya puede iniciar sesiÃ³n."
        onExito()
    }

    fun login(identificador: String, password: String) = launchTask {
        val rol = repo.login(identificador, password)
        _rolActual.value = rol
        _message.value = if (rol == TokenStore.ROL_SUPER_USUARIO) {
            "SesiÃ³n iniciada (super usuario)"
        } else {
            "SesiÃ³n iniciada"
        }
    }

    fun logout() = launchTask {
        repo.logout()
        _rolActual.value = null
        _administradores.value = emptyList()
        _colegios.value = emptyList()
        _apoderadosColegio.value = emptyList()
        _alumnosColegio.value = emptyList()
        _cursos.value = emptyList()
        _alumnosPendientes.value = emptyList()
    }

    fun cargarSuperUsuarios() = launchTask {
        val usuarios = repo.listarUsuarios()
        _usuariosPorId.value = usuarios.associateBy { it.idUsuario }
        _superUsuarios.value = repo.listarSuperUsuarios()
    }

    fun crearSuperUsuario(req: SuperUsuarioCompletoRequest) = launchTask {
        repo.crearSuperUsuario(req)
        _message.value = "Super usuario registrado"
        cargarSuperUsuarios()
    }

    fun actualizarSuperUsuario(
        idSuperUsuario: Int,
        req: SuperUsuarioRequest,
        username: String?,
        password: String?
    ) = launchTask {
        repo.actualizarSuperUsuario(idSuperUsuario, req)
        repo.actualizarUsuario(
            req.idUsuario,
            UsuarioUpdateRequest(
                username = username?.trim()?.takeIf { it.isNotBlank() },
                rut = req.rut,
                password = password?.takeIf { it.isNotBlank() },
                rol = "SUPER_USUARIO"
            )
        )
        _message.value = "Super usuario actualizado"
        cargarSuperUsuarios()
    }

    fun eliminarSuperUsuario(id: Int) = launchTask {
        repo.eliminarSuperUsuario(id)
        _message.value = "Super usuario eliminado"
        cargarSuperUsuarios()
    }

    fun cargarAdministradores() = launchTask {
        val usuarios = repo.listarUsuarios()
        _usuariosPorId.value = usuarios.associateBy { it.idUsuario }
        _administradores.value = repo.listarAdministradores()
    }

    fun crearAdministrador(req: AdministradorCompletoRequest) = launchTask {
        repo.crearAdministrador(req)
        _message.value = "Administrador registrado"
        cargarAdministradores()
    }

    fun actualizarAdministrador(
        idAdministrador: Int,
        req: AdministradorRequest,
        username: String?,
        password: String?
    ) = launchTask {
        repo.actualizarAdministrador(idAdministrador, req)
        repo.actualizarUsuario(
            req.idUsuario,
            UsuarioUpdateRequest(
                username = username?.trim()?.takeIf { it.isNotBlank() },
                rut = req.rut,
                password = password?.takeIf { it.isNotBlank() }
            )
        )
        _message.value = "Administrador actualizado"
        cargarAdministradores()
    }

    fun eliminarAdministrador(id: Int) = launchTask {
        repo.eliminarAdministrador(id)
        _message.value = "Administrador eliminado"
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

    fun actualizarColegio(id: Int, req: ColegioRequest) = launchTask {
        repo.actualizarColegio(id, req)
        _message.value = "Colegio actualizado"
        cargarColegios()
    }

    fun limpiarDatosColegio() {
        _cursos.value = emptyList()
        _apoderadosColegio.value = emptyList()
        _alumnosColegio.value = emptyList()
    }

    fun eliminarColegio(id: Int) = launchTask {
        repo.eliminarColegio(id)
        limpiarDatosColegio()
        _message.value = "Colegio eliminado"
        cargarColegios()
    }

    fun cargarApoderadosColegio(idColegio: Int) = launchTask {
        _apoderadosColegio.value = repo.listarApoderadosPorColegio(idColegio)
    }

    fun crearApoderado(idColegio: Int, req: ApoderadoRequest) = launchTask {
        repo.crearApoderadoEnColegio(idColegio, req)
        _message.value = "Apoderado registrado"
        cargarApoderadosColegio(idColegio)
    }

    fun actualizarApoderado(id: Int, idColegio: Int, req: ApoderadoRequest) = launchTask {
        repo.actualizarApoderado(id, req)
        _message.value = "Apoderado actualizado"
        cargarApoderadosColegio(idColegio)
    }

    fun eliminarApoderado(id: Int, idColegio: Int) = launchTask {
        repo.eliminarApoderado(id)
        _message.value = "Apoderado eliminado"
        cargarApoderadosColegio(idColegio)
    }

    fun cargarAlumnosColegio(idColegio: Int) = launchTask {
        _alumnosColegio.value = repo.listarAlumnosPorColegio(idColegio)
    }

    fun crearAlumno(req: AlumnoRequest) = launchTask {
        repo.crearAlumno(req)
        _message.value = "Alumno registrado"
        cargarAlumnosColegio(req.idColegio)
    }

    fun actualizarAlumno(id: Int, req: AlumnoRequest) = launchTask {
        repo.actualizarAlumno(id, req)
        _message.value = "Alumno actualizado"
        cargarAlumnosColegio(req.idColegio)
    }

    fun eliminarAlumno(id: Int, idColegio: Int) = launchTask {
        repo.eliminarAlumno(id)
        _message.value = "Alumno eliminado"
        cargarAlumnosColegio(idColegio)
    }

    fun cargarCursos(idColegio: Int) = launchTask {
        _cursos.value = repo.listarCursos(idColegio)
    }

    fun crearCurso(req: CursoRequest) = launchTask {
        repo.crearCurso(req)
        _message.value = "Curso creado"
        cargarCursos(req.idColegio)
    }

    fun actualizarCurso(id: Int, req: CursoRequest) = launchTask {
        repo.actualizarCurso(id, req)
        _message.value = "Curso actualizado"
        cargarCursos(req.idColegio)
    }

    fun eliminarCurso(id: Int, idColegio: Int) = launchTask {
        repo.eliminarCurso(id)
        _message.value = "Curso eliminado"
        cargarCursos(idColegio)
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
        val pendientes = alumnos.filter { repo.contarTarjetasAlumno(it.idAlumno) < TARJETAS_POR_ALUMNO }
        _alumnosPendientes.value = pendientes
        val primero = pendientes.firstOrNull()
        _tarjetasDelActual.value = if (primero != null) repo.contarTarjetasAlumno(primero.idAlumno) else 0
        _message.value = if (pendientes.isEmpty()) {
            "Todos los alumnos del curso ya tienen $TARJETAS_POR_ALUMNO tarjetas"
        } else {
            "Listo: ${pendientes.size} alumnos pendientes. Acerque la tarjeta NFC."
        }
    }

    fun alumnoActualNfc(): Alumno? = _alumnosPendientes.value.firstOrNull()

    fun registrarUidNfc(uid: String) = launchTask {
        val alumno = alumnoActualNfc()
            ?: throw IllegalStateException("No hay alumnos pendientes de tarjeta")
        repo.asignarTarjeta(alumno.idAlumno, uid)
        val nuevas = _tarjetasDelActual.value + 1
        if (nuevas >= TARJETAS_POR_ALUMNO) {
            val restantes = _alumnosPendientes.value.drop(1)
            _alumnosPendientes.value = restantes
            val siguiente = restantes.firstOrNull()
            _tarjetasDelActual.value = if (siguiente != null) repo.contarTarjetasAlumno(siguiente.idAlumno) else 0
            _message.value = "Tarjeta $TARJETAS_POR_ALUMNO/$TARJETAS_POR_ALUMNO $uid â†’ ${alumno.nombres} ${alumno.apellidos}. " +
                if (restantes.isEmpty()) "Lote completado." else "Siguiente: ${restantes.first().nombres}"
        } else {
            _tarjetasDelActual.value = nuevas
            _message.value = "Tarjeta $nuevas/$TARJETAS_POR_ALUMNO $uid â†’ ${alumno.nombres} ${alumno.apellidos}. Acerque la siguiente tarjeta."
        }
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
                _error.value = ApiErrorParser.mensaje(e)
            } finally {
                _loading.value = false
            }
        }
    }
}
