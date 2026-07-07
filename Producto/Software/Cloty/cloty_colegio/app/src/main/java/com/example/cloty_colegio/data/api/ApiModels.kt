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

data class ActivarCuentaColegioRequest(
    val rut: String,
    val codigo: String,
    val password: String
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

data class ColegioRequest(
    val idUsuario: Int? = null,
    val rut: String,
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val codigoComuna: String? = null,
    val calleNumero: String? = null
)

data class Colegio(
    val idColegio: Int,
    val idUsuario: Int?,
    val rut: String,
    val nombre: String,
    val email: String?,
    val telefono: String?,
    val codigoComuna: String?,
    val calleNumero: String?
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

data class ColegioApoderadoRequest(
    val idColegio: Int,
    val idApoderado: Int
)

data class AlumnoRequest(
    val idColegio: Int,
    val idApoderado: Int,
    val idCurso: Int,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val estado: Boolean? = true
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

data class CursoRequest(
    val idColegio: Int,
    val nombre: String,
    val nivel: String? = null,
    val estado: Boolean? = true
)

data class Curso(
    val idCurso: Int,
    val idColegio: Int,
    val nombre: String,
    val nivel: String?,
    val estado: Boolean?
)

data class ColegioDashboard(
    val idColegio: Int?,
    val nombreColegio: String?,
    val totalAlumnos: Long,
    val totalApoderados: Long,
    val totalCursos: Long,
    val apoderadosConCuenta: Long,
    val alumnosConTarjeta: Long,
    val alumnosSinTarjeta: Long,
    val tarjetasActivas: Long,
    val tarjetasPerdidas: Long,
    val tarjetasDesactivadas: Long,
    val prendasEncontradasHoy: Long,
    val prendasEncontradasTotal: Long,
    val prendasEntregadasHoy: Long,
    val prendasEntregadasTotal: Long,
    val notificacionesEnviadas: Long,
    val notificacionesPendientes: Long,
    val resumenCursos: List<ResumenCursoDashboard>?,
    val ultimasAcciones: List<ActividadReciente>?
)

data class ResumenCursoDashboard(
    val idCurso: Int?,
    val nombre: String?,
    val nivel: String?,
    val totalAlumnos: Long,
    val alumnosConTarjeta: Long,
    val alumnosSinTarjeta: Long
)

data class DashboardAlumnoItem(
    val idAlumno: Int?,
    val rut: String?,
    val nombres: String?,
    val apellidos: String?,
    val nombreCurso: String?,
    val tieneTarjeta: Boolean,
    val nombreApoderado: String?
)

data class DashboardApoderadoItem(
    val idApoderado: Int?,
    val rut: String?,
    val nombres: String?,
    val apellidos: String?,
    val email: String?,
    val tieneCuenta: Boolean
)

data class DashboardComunidadDetalle(
    val totalAlumnos: Long,
    val totalApoderados: Long,
    val totalCursos: Long,
    val apoderadosConCuenta: Long,
    val alumnosConTarjeta: Long,
    val alumnosSinTarjeta: Long,
    val alumnosLista: List<DashboardAlumnoItem>?,
    val apoderadosLista: List<DashboardApoderadoItem>?
)

data class DashboardTarjetaItem(
    val idTarjeta: Int?,
    val uidNfc: String?,
    val estado: String?,
    val tipoPrenda: String?,
    val nombreAlumno: String?,
    val nombreCurso: String?
)

data class DashboardTarjetasDetalle(
    val tarjetasActivas: Long,
    val tarjetasPerdidas: Long,
    val tarjetasDesactivadas: Long,
    val tarjetas: List<DashboardTarjetaItem>?
)

data class DashboardPrendasDetalle(
    val prendasEncontradasHoy: Long,
    val prendasEncontradasTotal: Long,
    val prendasEntregadasHoy: Long,
    val prendasEntregadasTotal: Long,
    val actividad: List<ActividadReciente>?
)

data class DashboardNotificacionItem(
    val idNotificacion: Int?,
    val titulo: String?,
    val mensaje: String?,
    val estado: String?,
    val nombreApoderado: String?,
    val fechaEnvio: String?
)

data class DashboardNotificacionesDetalle(
    val notificacionesEnviadas: Long,
    val notificacionesPendientes: Long,
    val recientes: List<DashboardNotificacionItem>?
)

data class DashboardCursoDetalle(
    val idCurso: Int?,
    val nombre: String?,
    val nivel: String?,
    val totalAlumnos: Long,
    val alumnosConTarjeta: Long,
    val alumnosSinTarjeta: Long,
    val alumnos: List<DashboardAlumnoItem>?
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
