package com.example.cloty_colegio.data.api

data class LoginRequest(val identificador: String, val password: String)

data class AuthTokenResponse(val token: String, val tokenType: String?, val expiresInMs: Long?)

data class CambiarContrasenaRequest(
    val contrasenaActual: String,
    val contrasenaNueva: String
)

data class AuthMeResponse(
    val idUsuario: Int?,
    val username: String?,
    val rol: String?,
    val idColegio: Int?,
    val idApoderado: Int?
)

data class ScanPrendaRequest(
    val uidNfc: String,
    val ubicacion: String? = null,
    val descripcion: String? = null
)

data class OperacionPrendaResponse(
    val tipoEvento: String,
    val accion: String,
    val idEvento: Int?,
    val idNotificacion: Int?,
    val idTarjeta: Int?,
    val uidNfc: String?,
    val idAlumno: Int?,
    val nombreAlumno: String?,
    val nombreCurso: String?,
    val idApoderado: Int?,
    val nombreApoderado: String?,
    val tipoPrenda: String?,
    val mensaje: String?,
    val fechaEvento: String?
)

data class ActividadReciente(
    val idEvento: Int?,
    val tipoEvento: String?,
    val accion: String?,
    val fecha: String?,
    val nombreAlumno: String?,
    val nombreCurso: String?,
    val uidNfc: String?,
    val tipoPrenda: String?,
    val descripcion: String?
)

data class ColegioDashboard(
    val idColegio: Int?,
    val nombreColegio: String?,
    val tarjetasActivas: Long,
    val alumnosConTarjeta: Long,
    val prendasEncontradasHoy: Long,
    val prendasEncontradasTotal: Long,
    val prendasEntregadasHoy: Long,
    val prendasEntregadasTotal: Long,
    val notificacionesEnviadas: Long,
    val ultimasAcciones: List<ActividadReciente>?
)
