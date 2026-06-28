package com.example.cloty_colegio.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ClotyApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthTokenResponse

    @POST("api/auth/solicitar-codigo-colegio")
    suspend fun solicitarCodigoActivacion(@Body body: SolicitarCodigoActivacionRequest): SolicitarCodigoActivacionResponse

    @POST("api/auth/solicitar-recuperacion-contrasena")
    suspend fun solicitarRecuperacionContrasena(@Body body: SolicitarCodigoActivacionRequest): SolicitarCodigoActivacionResponse

    @POST("api/auth/restablecer-contrasena")
    suspend fun restablecerContrasena(@Body body: RestablecerContrasenaRequest)

    @POST("api/auth/validar-codigo-colegio")
    suspend fun validarCodigoActivacion(@Body body: ValidarCodigoActivacionRequest)

    @POST("api/auth/activar-cuenta-colegio")
    suspend fun activarCuentaColegio(@Body body: ActivarCuentaColegioRequest): AuthTokenResponse

    @GET("api/auth/me")
    suspend fun me(): AuthMeResponse

    @POST("api/colegio/operaciones/escanear")
    suspend fun escanear(@Body body: ScanPrendaRequest): OperacionPrendaResponse

    @GET("api/colegio/operaciones/dashboard")
    suspend fun dashboard(): ColegioDashboard

    @GET("api/colegio/operaciones/dashboard/comunidad")
    suspend fun dashboardComunidad(): DashboardComunidadDetalle

    @GET("api/colegio/operaciones/dashboard/tarjetas")
    suspend fun dashboardTarjetas(): DashboardTarjetasDetalle

    @GET("api/colegio/operaciones/dashboard/prendas")
    suspend fun dashboardPrendas(): DashboardPrendasDetalle

    @GET("api/colegio/operaciones/dashboard/notificaciones")
    suspend fun dashboardNotificaciones(): DashboardNotificacionesDetalle

    @GET("api/colegio/operaciones/dashboard/cursos")
    suspend fun dashboardCursos(): List<DashboardCursoDetalle>

    @GET("api/colegio/operaciones/dashboard/cursos/{idCurso}")
    suspend fun dashboardCurso(@Path("idCurso") idCurso: Int): DashboardCursoDetalle

    @GET("api/colegio/operaciones/dashboard/actividad")
    suspend fun dashboardActividad(): List<ActividadReciente>

    @POST("api/auth/cambiar-contrasena")
    suspend fun cambiarContrasena(@Body body: CambiarContrasenaRequest)

    @GET("api/colegios/{id}")
    suspend fun obtenerColegio(@Path("id") id: Int): Colegio

    @PUT("api/colegios/{id}")
    suspend fun actualizarColegio(@Path("id") id: Int, @Body body: ColegioRequest): Colegio

    @GET("api/apoderados/colegio/{idColegio}")
    suspend fun listarApoderadosPorColegio(@Path("idColegio") idColegio: Int): List<Apoderado>

    @POST("api/apoderados")
    suspend fun crearApoderado(@Body body: ApoderadoRequest): Apoderado

    @PUT("api/apoderados/{id}")
    suspend fun actualizarApoderado(@Path("id") id: Int, @Body body: ApoderadoRequest): Apoderado

    @DELETE("api/apoderados/{id}")
    suspend fun eliminarApoderado(@Path("id") id: Int)

    @POST("api/colegio-apoderados")
    suspend fun crearColegioApoderado(@Body body: ColegioApoderadoRequest)

    @GET("api/alumnos/colegio/{idColegio}")
    suspend fun listarAlumnosPorColegio(@Path("idColegio") idColegio: Int): List<Alumno>

    @POST("api/alumnos")
    suspend fun crearAlumno(@Body body: AlumnoRequest): Alumno

    @PUT("api/alumnos/{id}")
    suspend fun actualizarAlumno(@Path("id") id: Int, @Body body: AlumnoRequest): Alumno

    @DELETE("api/alumnos/{id}")
    suspend fun eliminarAlumno(@Path("id") id: Int)

    @GET("api/cursos/colegio/{idColegio}")
    suspend fun listarCursos(@Path("idColegio") idColegio: Int): List<Curso>

    @POST("api/cursos")
    suspend fun crearCurso(@Body body: CursoRequest): Curso

    @PUT("api/cursos/{id}")
    suspend fun actualizarCurso(@Path("id") id: Int, @Body body: CursoRequest): Curso

    @DELETE("api/cursos/{id}")
    suspend fun eliminarCurso(@Path("id") id: Int)

    @GET("api/ubicacion/regiones")
    suspend fun listarRegiones(): List<Region>

    @GET("api/ubicacion/regiones/{codigoRegion}/comunas")
    suspend fun listarComunas(@Path("codigoRegion") codigoRegion: String): List<Comuna>

    @GET("api/ubicacion/comunas/{codigoComuna}")
    suspend fun obtenerComuna(@Path("codigoComuna") codigoComuna: String): Comuna
}
