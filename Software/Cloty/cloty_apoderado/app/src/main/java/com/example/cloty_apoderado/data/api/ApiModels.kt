package com.example.cloty_apoderado.data.api

data class LoginRequest(val identificador: String, val password: String)

data class AuthTokenResponse(val token: String, val tokenType: String?, val expiresInMs: Long?)

data class AuthMeResponse(
    val idUsuario: Int?,
    val username: String?,
    val rol: String?,
    val idColegio: Int?,
    val idApoderado: Int?
)

data class CambiarContrasenaRequest(
    val contrasenaActual: String,
    val contrasenaNueva: String
)

data class ApoderadoRequest(
    val idUsuario: Int? = null,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val email: String? = null,
    val telefono: String? = null,
    val codigoComuna: String? = null,
    val calleNumero: String? = null
)

data class Apoderado(
    val idApoderado: Int,
    val idUsuario: Int?,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val email: String?,
    val telefono: String?,
    val codigoComuna: String?,
    val calleNumero: String?
)

data class SolicitarCodigoActivacionRequest(val rut: String)

data class RestablecerContrasenaRequest(
    val rut: String,
    val codigo: String,
    val password: String
)

data class SolicitarCodigoActivacionResponse(
    val correoEnmascarado: String?,
    val mensaje: String?
)

data class ValidarCodigoActivacionRequest(
    val rut: String,
    val codigo: String
)

data class ActivarCuentaApoderadoRequest(
    val rut: String,
    val codigo: String,
    val password: String
)

data class PupiloResumen(
    val idAlumno: Int?,
    val rut: String?,
    val nombres: String?,
    val apellidos: String?,
    val estado: Boolean?,
    val idCurso: Int?,
    val nombreCurso: String?,
    val idColegio: Int?,
    val nombreColegio: String?
)

data class Notificacion(
    val idNotificacion: Int?,
    val idEvento: Int?,
    val idApoderado: Int?,
    val titulo: String?,
    val mensaje: String?,
    val estado: String?,
    val leida: Boolean?,
    val fechaEnvio: String?
)

data class Region(
    val codigoRegion: String,
    val nombre: String
)

data class Comuna(
    val codigoComuna: String,
    val codigoRegion: String,
    val nombre: String
)
