package com.example.cloty_administrador.data.api

data class LoginRequest(val identificador: String, val password: String)

data class CambiarContrasenaRequest(
    val contrasenaActual: String,
    val contrasenaNueva: String
)

data class AuthTokenResponse(
    val token: String,
    val tokenType: String?,
    val expiresInMs: Long?
)

data class AuthMeResponse(
    val idUsuario: Int,
    val username: String,
    val rol: String,
    val idColegio: Int?,
    val idApoderado: Int?
)

data class AdministradorCompletoRequest(
    val username: String,
    val password: String,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String?
)

data class ColegioRequest(
    val idUsuario: Int? = null,
    val rut: String,
    val nombre: String,
    val email: String? = null,
    val telefono: String? = null,
    val direccion: String? = null
)

data class CursoRequest(
    val idColegio: Int,
    val nombre: String,
    val nivel: String? = null,
    val estado: Boolean? = true
)

data class TarjetaRequest(
    val idAlumno: Int,
    val uidNfc: String,
    val codigoVisual: String? = null,
    val tipoPrenda: String? = null,
    val estado: String? = "ACTIVA"
)

data class CargaMasivaResult(
    val filasLeidas: Int,
    val creados: Int,
    val omitidos: Int,
    val errores: Int,
    val mensajes: List<String>?
)

data class Administrador(
    val idAdministrador: Int,
    val idUsuario: Int,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String?
)

data class Colegio(
    val idColegio: Int,
    val idUsuario: Int?,
    val rut: String,
    val nombre: String,
    val email: String?,
    val telefono: String?,
    val direccion: String?
)

data class Curso(
    val idCurso: Int,
    val idColegio: Int,
    val nombre: String,
    val nivel: String?,
    val estado: Boolean?
)

data class Alumno(
    val idAlumno: Int,
    val idColegio: Int,
    val idApoderado: Int,
    val idCurso: Int,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val estado: Boolean?
)

data class Tarjeta(
    val idTarjeta: Int,
    val idAlumno: Int,
    val uidNfc: String,
    val codigoVisual: String?,
    val tipoPrenda: String?,
    val estado: String?
)
