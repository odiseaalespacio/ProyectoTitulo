package com.example.cloty_administrador.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ClotyApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthTokenResponse

    @GET("api/auth/me")
    suspend fun me(): AuthMeResponse

    @POST("api/auth/cambiar-contrasena")
    suspend fun cambiarContrasena(@Body body: CambiarContrasenaRequest)

    @GET("api/usuarios")
    suspend fun listarUsuarios(): List<Usuario>

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body body: UsuarioCreateRequest): Usuario

    @GET("api/administradores")
    suspend fun listarAdministradores(): List<Administrador>

    @POST("api/administradores/completo")
    suspend fun crearAdministrador(@Body body: AdministradorCompletoRequest): Administrador

    @GET("api/colegios")
    suspend fun listarColegios(): List<Colegio>

    @POST("api/colegios")
    suspend fun crearColegio(@Body body: ColegioRequest): Colegio

    // esta parte es nueva
    @PUT("api/colegios/{id}")
    suspend fun actualizarColegio(@Path("id") id: Int, @Body body: ColegioRequest): Colegio

    // esta parte es nueva
    @DELETE("api/colegios/{id}")
    suspend fun eliminarColegio(@Path("id") id: Int)

    // esta parte es nueva
    @GET("api/apoderados")
    suspend fun listarApoderados(): List<Apoderado>

    // esta parte es nueva
    @POST("api/apoderados")
    suspend fun crearApoderado(@Body body: ApoderadoRequest): Apoderado

    // esta parte es nueva
    @PUT("api/apoderados/{id}")
    suspend fun actualizarApoderado(@Path("id") id: Int, @Body body: ApoderadoRequest): Apoderado

    // esta parte es nueva
    @DELETE("api/apoderados/{id}")
    suspend fun eliminarApoderado(@Path("id") id: Int)

    // esta parte es nueva
    @GET("api/colegio-apoderados/colegio/{idColegio}")
    suspend fun listarColegioApoderados(@Path("idColegio") idColegio: Int): List<ColegioApoderado>

    // esta parte es nueva
    @POST("api/colegio-apoderados")
    suspend fun crearColegioApoderado(@Body body: ColegioApoderadoRequest): ColegioApoderado

    // esta parte es nueva
    @GET("api/alumnos/colegio/{idColegio}")
    suspend fun listarAlumnosPorColegio(@Path("idColegio") idColegio: Int): List<Alumno>

    // esta parte es nueva
    @POST("api/alumnos")
    suspend fun crearAlumno(@Body body: AlumnoRequest): Alumno

    // esta parte es nueva
    @PUT("api/alumnos/{id}")
    suspend fun actualizarAlumno(@Path("id") id: Int, @Body body: AlumnoRequest): Alumno

    // esta parte es nueva
    @DELETE("api/alumnos/{id}")
    suspend fun eliminarAlumno(@Path("id") id: Int)

    @GET("api/cursos/colegio/{idColegio}")
    suspend fun listarCursos(@Path("idColegio") idColegio: Int): List<Curso>

    @POST("api/cursos")
    suspend fun crearCurso(@Body body: CursoRequest): Curso

    @GET("api/alumnos/curso/{idCurso}")
    suspend fun listarAlumnosPorCurso(@Path("idCurso") idCurso: Int): List<Alumno>

    @GET("api/tarjetas/alumno/{idAlumno}")
    suspend fun listarTarjetasAlumno(@Path("idAlumno") idAlumno: Int): List<Tarjeta>

    @POST("api/tarjetas")
    suspend fun crearTarjeta(@Body body: TarjetaRequest): Tarjeta

    @Multipart
    @POST("api/carga-masiva/colegio/{idColegio}/apoderados")
    suspend fun importarApoderados(
        @Path("idColegio") idColegio: Int,
        @Part archivo: MultipartBody.Part
    ): CargaMasivaResult

    @Multipart
    @POST("api/carga-masiva/colegio/{idColegio}/alumnos")
    suspend fun importarAlumnos(
        @Path("idColegio") idColegio: Int,
        @Part archivo: MultipartBody.Part
    ): CargaMasivaResult
}
