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

data class ActivarCuentaApoderadoRequest(
    val rut: String,
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
