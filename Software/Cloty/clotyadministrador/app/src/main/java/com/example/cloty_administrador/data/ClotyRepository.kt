package com.example.cloty_administrador.data

import android.content.Context
import android.net.Uri
import com.example.cloty_administrador.data.api.AdministradorCompletoRequest
import com.example.cloty_administrador.data.api.ApiClient
import com.example.cloty_administrador.data.api.ColegioRequest
import com.example.cloty_administrador.data.api.CursoRequest
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

    suspend fun login(identificador: String, password: String): String {
        val response = api.login(LoginRequest(identificador, password))
        tokenStore.saveSession(response.token, "")
        val me = api.me()
        val rol = me.rol.trim()
        require(rol == TokenStore.ROL_ADMINISTRADOR || rol == TokenStore.ROL_SUPER_USUARIO) {
            "Esta cuenta no tiene acceso al panel de administración"
        }
        tokenStore.saveSession(response.token, rol)
        return rol
    }

    suspend fun refrescarRolSesion(): String? {
        val token = tokenStore.tokenFlow.firstOrNull()
        if (token.isNullOrBlank()) return null
        return try {
            val me = api.me()
            val rol = me.rol.trim()
            if (rol == TokenStore.ROL_ADMINISTRADOR || rol == TokenStore.ROL_SUPER_USUARIO) {
                tokenStore.saveSession(token, rol)
                rol
            } else {
                logout()
                null
            }
        } catch (_: Exception) {
            logout()
            null
        }
    }

    suspend fun cambiarContrasena(actual: String, nueva: String) =
        api.cambiarContrasena(
            com.example.cloty_administrador.data.api.CambiarContrasenaRequest(actual, nueva)
        )

    suspend fun logout() = tokenStore.clear()

    suspend fun listarAdministradores() = api.listarAdministradores()

    suspend fun crearAdministrador(req: AdministradorCompletoRequest) =
        api.crearAdministrador(req)

    suspend fun listarColegios() = api.listarColegios()

    suspend fun crearColegio(req: ColegioRequest) = api.crearColegio(req)

    suspend fun listarCursos(idColegio: Int) = api.listarCursos(idColegio)

    suspend fun crearCurso(req: CursoRequest) = api.crearCurso(req)

    suspend fun listarAlumnosPorCurso(idCurso: Int) = api.listarAlumnosPorCurso(idCurso)

    suspend fun contarTarjetasAlumno(idAlumno: Int): Int =
        api.listarTarjetasAlumno(idAlumno).size

    suspend fun asignarTarjeta(idAlumno: Int, uidNfc: String) =
        api.crearTarjeta(TarjetaRequest(idAlumno = idAlumno, uidNfc = uidNfc))

    suspend fun importarApoderados(idColegio: Int, uri: Uri, context: Context) =
        api.importarApoderados(idColegio, uri.toCsvPart(context, "apoderados.csv"))

    suspend fun importarAlumnos(idColegio: Int, uri: Uri, context: Context) =
        api.importarAlumnos(idColegio, uri.toCsvPart(context, "alumnos.csv"))

    private fun Uri.toCsvPart(context: Context, fileName: String): MultipartBody.Part {
        val temp = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(this)?.use { input ->
            FileOutputStream(temp).use { output -> input.copyTo(output) }
        } ?: error("No se pudo leer el archivo CSV")
        val body = temp.asRequestBody("text/csv".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("archivo", fileName, body)
    }
}
