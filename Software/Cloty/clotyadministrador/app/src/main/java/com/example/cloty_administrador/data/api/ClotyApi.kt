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

    @POST("api/auth/solicitar-recuperacion-contrasena")
    suspend fun solicitarRecuperacionContrasena(@Body body: SolicitarCodigoActivacionRequest): SolicitarCodigoActivacionResponse

    @POST("api/auth/restablecer-contrasena")
    suspend fun restablecerContrasena(@Body body: RestablecerContrasenaRequest)

    @GET("api/auth/me")
    suspend fun me(): AuthMeResponse

    @POST("api/auth/cambiar-contrasena")
    suspend fun cambiarContrasena(@Body body: CambiarContrasenaRequest)

    @GET("api/usuarios")
    suspend fun listarUsuarios(): List<Usuario>

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body body: UsuarioCreateRequest): Usuario

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Int, @Body body: UsuarioUpdateRequest): Usuario

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Int)

    @GET("api/super-usuarios")
    suspend fun listarSuperUsuarios(): List<SuperUsuario>

    @POST("api/super-usuarios/completo")
    suspend fun crearSuperUsuario(@Body body: SuperUsuarioCompletoRequest): SuperUsuario

    @PUT("api/super-usuarios/{id}")
    suspend fun actualizarSuperUsuario(@Path("id") id: Int, @Body body: SuperUsuarioRequest): SuperUsuario

    @DELETE("api/super-usuarios/{id}")
    suspend fun eliminarSuperUsuario(@Path("id") id: Int)

    @GET("api/administradores")
    suspend fun listarAdministradores(): List<Administrador>

    @POST("api/administradores/completo")
    suspend fun crearAdministrador(@Body body: AdministradorCompletoRequest): Administrador

    @PUT("api/administradores/{id}")
    suspend fun actualizarAdministrador(@Path("id") id: Int, @Body body: AdministradorRequest): Administrador

    @DELETE("api/administradores/{id}")
    suspend fun eliminarAdministrador(@Path("id") id: Int)

    @GET("api/colegios")
    suspend fun listarColegios(): List<Colegio>

    @POST("api/colegios")
    suspend fun crearColegio(@Body body: ColegioRequest): Colegio

    @PUT("api/colegios/{id}")
    suspend fun actualizarColegio(@Path("id") id: Int, @Body body: ColegioRequest): Colegio

    @DELETE("api/colegios/{id}")
    suspend fun eliminarColegio(@Path("id") id: Int)

    @GET("api/apoderados/colegio/{idColegio}")
    suspend fun listarApoderadosPorColegio(@Path("idColegio") idColegio: Int): List<Apoderado>

    @GET("api/apoderados")
    suspend fun listarApoderados(): List<Apoderado>

    @POST("api/apoderados")
    suspend fun crearApoderado(@Body body: ApoderadoRequest): Apoderado

    @PUT("api/apoderados/{id}")
    suspend fun actualizarApoderado(@Path("id") id: Int, @Body body: ApoderadoRequest): Apoderado

    @DELETE("api/apoderados/{id}")
    suspend fun eliminarApoderado(@Path("id") id: Int)

    @GET("api/colegio-apoderados/colegio/{idColegio}")
    suspend fun listarColegioApoderados(@Path("idColegio") idColegio: Int): List<ColegioApoderado>

    @POST("api/colegio-apoderados")
    suspend fun crearColegioApoderado(@Body body: ColegioApoderadoRequest): ColegioApoderado

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

    @GET("api/ubicacion/regiones")
    suspend fun listarRegiones(): List<Region>

    @GET("api/ubicacion/regiones/{codigoRegion}/comunas")
    suspend fun listarComunas(@Path("codigoRegion") codigoRegion: String): List<Comuna>

    @GET("api/ubicacion/comunas/{codigoComuna}")
    suspend fun obtenerComuna(@Path("codigoComuna") codigoComuna: String): Comuna
}
