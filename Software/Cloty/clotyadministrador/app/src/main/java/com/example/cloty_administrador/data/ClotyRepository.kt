package com.example.cloty_administrador.data

import android.content.Context
import android.net.Uri
import com.example.cloty_administrador.data.api.AdministradorCompletoRequest
import com.example.cloty_administrador.data.api.AdministradorRequest
import com.example.cloty_administrador.data.api.SuperUsuarioCompletoRequest
import com.example.cloty_administrador.data.api.SuperUsuarioRequest
import com.example.cloty_administrador.data.api.UsuarioUpdateRequest
import com.example.cloty_administrador.data.api.ApiClient
import com.example.cloty_administrador.util.JwtClaims
import com.example.cloty_administrador.data.api.AlumnoRequest
import com.example.cloty_administrador.data.api.ApoderadoRequest
import com.example.cloty_administrador.data.api.ColegioApoderadoRequest
import com.example.cloty_administrador.data.api.ColegioRequest
import com.example.cloty_administrador.data.api.Comuna
import com.example.cloty_administrador.data.api.CursoRequest
import com.example.cloty_administrador.data.api.Region
import com.example.cloty_administrador.data.api.LoginRequest
import com.example.cloty_administrador.data.api.TarjetaRequest
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ClotyRepository(context: Context) {

    val tokenStore = TokenStore(context.applicationContext)

    init {
        ApiClient.init(tokenStore)
    }

    private val api get() = ApiClient.api

    suspend fun solicitarRecuperacionContrasena(rut: String) =
        api.solicitarRecuperacionContrasena(
            com.example.cloty_administrador.data.api.SolicitarCodigoActivacionRequest(rut)
        )

    suspend fun restablecerContrasena(rut: String, codigo: String, password: String) =
        api.restablecerContrasena(
            com.example.cloty_administrador.data.api.RestablecerContrasenaRequest(rut, codigo, password)
        )

    suspend fun login(identificador: String, password: String): String {
        ApiClient.setBearerToken(null)
        val response = api.login(LoginRequest(identificador, password))
        val token = response.token
        ApiClient.setBearerToken(token)
        val rol = JwtClaims.rol(token)?.trim().orEmpty()
        require(rol == TokenStore.ROL_ADMINISTRADOR || rol == TokenStore.ROL_SUPER_USUARIO) {
            "Esta cuenta no tiene acceso al panel de administración"
        }
        tokenStore.saveSession(token, rol)
        return rol
    }

    suspend fun refrescarRolSesion(): String? {
        val token = tokenStore.peekToken()
            ?: tokenStore.tokenFlow.firstOrNull()
        if (token.isNullOrBlank()) return null
        ApiClient.setBearerToken(token)
        val rol = JwtClaims.rol(token)?.trim().orEmpty()
        return if (rol == TokenStore.ROL_ADMINISTRADOR || rol == TokenStore.ROL_SUPER_USUARIO) {
            tokenStore.saveSession(token, rol)
            rol
        } else {
            logout()
            null
        }
    }

    suspend fun cambiarContrasena(actual: String, nueva: String) =
        api.cambiarContrasena(
            com.example.cloty_administrador.data.api.CambiarContrasenaRequest(actual, nueva)
        )

    suspend fun logout() = tokenStore.clear()

    suspend fun listarUsuarios() = api.listarUsuarios()

    suspend fun listarSuperUsuarios() = api.listarSuperUsuarios()

    suspend fun crearSuperUsuario(req: SuperUsuarioCompletoRequest) =
        api.crearSuperUsuario(req)

    suspend fun actualizarSuperUsuario(id: Int, req: SuperUsuarioRequest) =
        api.actualizarSuperUsuario(id, req)

    suspend fun eliminarSuperUsuario(id: Int) = api.eliminarSuperUsuario(id)

    suspend fun actualizarUsuario(id: Int, req: UsuarioUpdateRequest) =
        api.actualizarUsuario(id, req)

    suspend fun eliminarUsuario(id: Int) = api.eliminarUsuario(id)

    suspend fun listarAdministradores() = api.listarAdministradores()

    suspend fun crearAdministrador(req: AdministradorCompletoRequest) =
        api.crearAdministrador(req)

    suspend fun actualizarAdministrador(id: Int, req: AdministradorRequest) =
        api.actualizarAdministrador(id, req)

    suspend fun eliminarAdministrador(id: Int) = api.eliminarAdministrador(id)

    suspend fun listarColegios() = api.listarColegios()

    suspend fun crearColegio(req: ColegioRequest) = api.crearColegio(req)

    suspend fun actualizarColegio(id: Int, req: ColegioRequest) = api.actualizarColegio(id, req)

    suspend fun eliminarColegio(id: Int) = api.eliminarColegio(id)

    suspend fun listarApoderadosPorColegio(idColegio: Int): List<com.example.cloty_administrador.data.api.Apoderado> =
        api.listarApoderadosPorColegio(idColegio)

    suspend fun crearApoderadoEnColegio(idColegio: Int, req: ApoderadoRequest): com.example.cloty_administrador.data.api.Apoderado {
        val apoderado = api.crearApoderado(req)
        api.crearColegioApoderado(ColegioApoderadoRequest(idColegio, apoderado.idApoderado))
        return apoderado
    }

    suspend fun actualizarApoderado(id: Int, req: ApoderadoRequest) = api.actualizarApoderado(id, req)

    suspend fun eliminarApoderado(id: Int) = api.eliminarApoderado(id)

    suspend fun listarAlumnosPorColegio(idColegio: Int) = api.listarAlumnosPorColegio(idColegio)

    suspend fun crearAlumno(req: AlumnoRequest) = api.crearAlumno(req)

    suspend fun actualizarAlumno(id: Int, req: AlumnoRequest) = api.actualizarAlumno(id, req)

    suspend fun eliminarAlumno(id: Int) = api.eliminarAlumno(id)

    suspend fun listarCursos(idColegio: Int) = api.listarCursos(idColegio)

    suspend fun crearCurso(req: CursoRequest) = api.crearCurso(req)

    suspend fun actualizarCurso(id: Int, req: CursoRequest) = api.actualizarCurso(id, req)

    suspend fun eliminarCurso(id: Int) = api.eliminarCurso(id)

    suspend fun listarAlumnosPorCurso(idCurso: Int) = api.listarAlumnosPorCurso(idCurso)

    suspend fun contarTarjetasAlumno(idAlumno: Int): Int =
        api.listarTarjetasAlumno(idAlumno).size

    suspend fun asignarTarjeta(idAlumno: Int, uidNfc: String) =
        api.crearTarjeta(TarjetaRequest(idAlumno = idAlumno, uidNfc = uidNfc))

    suspend fun importarApoderados(idColegio: Int, uri: Uri, context: Context) =
        api.importarApoderados(idColegio, uri.toCsvPart(context, "apoderados.csv"))

    suspend fun importarAlumnos(idColegio: Int, uri: Uri, context: Context) =
        api.importarAlumnos(idColegio, uri.toCsvPart(context, "alumnos.csv"))

    suspend fun listarRegiones() = api.listarRegiones()

    suspend fun listarComunas(codigoRegion: String) = api.listarComunas(codigoRegion)

    suspend fun obtenerComuna(codigoComuna: String): Comuna? =
        try {
            api.obtenerComuna(codigoComuna)
        } catch (_: Exception) {
            null
        }

    private fun Uri.toCsvPart(context: Context, fileName: String): MultipartBody.Part {
        val temp = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(this)?.use { input ->
            FileOutputStream(temp).use { output -> input.copyTo(output) }
        } ?: error("No se pudo leer el archivo CSV")
        val body = temp.asRequestBody("text/csv".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("archivo", fileName, body)
    }
}
