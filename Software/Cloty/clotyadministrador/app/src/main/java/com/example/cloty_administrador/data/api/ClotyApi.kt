package com.example.cloty_administrador.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ClotyApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthTokenResponse

    @GET("api/auth/me")
    suspend fun me(): AuthMeResponse

    @POST("api/auth/cambiar-contrasena")
    suspend fun cambiarContrasena(@Body body: CambiarContrasenaRequest)

    @GET("api/administradores")
    suspend fun listarAdministradores(): List<Administrador>

    @POST("api/administradores/completo")
    suspend fun crearAdministrador(@Body body: AdministradorCompletoRequest): Administrador

    @GET("api/colegios")
    suspend fun listarColegios(): List<Colegio>

    @POST("api/colegios")
    suspend fun crearColegio(@Body body: ColegioRequest): Colegio

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
