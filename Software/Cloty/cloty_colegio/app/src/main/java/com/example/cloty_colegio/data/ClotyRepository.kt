package com.example.cloty_colegio.data

import com.example.cloty_colegio.data.api.ActivarCuentaColegioRequest
import com.example.cloty_colegio.data.api.SolicitarCodigoActivacionRequest
import com.example.cloty_colegio.data.api.AlumnoRequest
import com.example.cloty_colegio.data.api.ApiClient
import com.example.cloty_colegio.data.api.ApoderadoRequest
import com.example.cloty_colegio.data.api.AuthMeResponse
import com.example.cloty_colegio.data.api.ColegioApoderadoRequest
import com.example.cloty_colegio.data.api.ColegioDashboard
import com.example.cloty_colegio.data.api.ColegioRequest
import com.example.cloty_colegio.data.api.CursoRequest
import com.example.cloty_colegio.data.api.LoginRequest
import com.example.cloty_colegio.data.api.OperacionPrendaResponse
import com.example.cloty_colegio.data.api.ScanPrendaRequest

class ClotyRepository(context: android.content.Context) {

    val tokenStore = TokenStore(context.applicationContext)

    init {
        ApiClient.init(tokenStore)
    }

    private val api get() = ApiClient.api

    suspend fun login(identificador: String, password: String) {
        val response = api.login(LoginRequest(identificador, password))
        tokenStore.saveToken(response.token)
    }

    suspend fun solicitarCodigoActivacion(rut: String) =
        api.solicitarCodigoActivacion(SolicitarCodigoActivacionRequest(rut))

    suspend fun solicitarRecuperacionContrasena(rut: String) =
        api.solicitarRecuperacionContrasena(SolicitarCodigoActivacionRequest(rut))

    suspend fun restablecerContrasena(rut: String, codigo: String, password: String) =
        api.restablecerContrasena(
            com.example.cloty_colegio.data.api.RestablecerContrasenaRequest(rut, codigo, password)
        )

    suspend fun validarCodigoActivacion(rut: String, codigo: String) =
        api.validarCodigoActivacion(
            com.example.cloty_colegio.data.api.ValidarCodigoActivacionRequest(rut, codigo)
        )

    suspend fun activarCuenta(rut: String, codigo: String, password: String) {
        val response = api.activarCuentaColegio(
            ActivarCuentaColegioRequest(rut, codigo, password)
        )
        tokenStore.saveToken(response.token)
    }

    suspend fun logout() = tokenStore.clear()

    suspend fun me(): AuthMeResponse = api.me()

    suspend fun escanear(uidNfc: String, ubicacion: String?): OperacionPrendaResponse =
        api.escanear(ScanPrendaRequest(uidNfc = uidNfc, ubicacion = ubicacion))

    suspend fun dashboard(): ColegioDashboard = api.dashboard()

    suspend fun cambiarContrasena(actual: String, nueva: String) =
        api.cambiarContrasena(
            com.example.cloty_colegio.data.api.CambiarContrasenaRequest(actual, nueva)
        )

    suspend fun obtenerColegio(id: Int) = api.obtenerColegio(id)

    suspend fun actualizarColegio(id: Int, req: ColegioRequest) = api.actualizarColegio(id, req)

    suspend fun listarApoderadosPorColegio(idColegio: Int) =
        api.listarApoderadosPorColegio(idColegio)

    suspend fun crearApoderadoEnColegio(idColegio: Int, req: ApoderadoRequest) {
        val apoderado = api.crearApoderado(req)
        api.crearColegioApoderado(ColegioApoderadoRequest(idColegio, apoderado.idApoderado))
    }

    suspend fun actualizarApoderado(id: Int, req: ApoderadoRequest) =
        api.actualizarApoderado(id, req)

    suspend fun eliminarApoderado(id: Int) = api.eliminarApoderado(id)

    suspend fun listarAlumnosPorColegio(idColegio: Int) = api.listarAlumnosPorColegio(idColegio)

    suspend fun crearAlumno(req: AlumnoRequest) = api.crearAlumno(req)

    suspend fun actualizarAlumno(id: Int, req: AlumnoRequest) = api.actualizarAlumno(id, req)

    suspend fun eliminarAlumno(id: Int) = api.eliminarAlumno(id)

    suspend fun listarCursos(idColegio: Int) = api.listarCursos(idColegio)

    suspend fun crearCurso(req: CursoRequest) = api.crearCurso(req)

    suspend fun actualizarCurso(id: Int, req: CursoRequest) = api.actualizarCurso(id, req)

    suspend fun eliminarCurso(id: Int) = api.eliminarCurso(id)
}
