package com.example.cloty_apoderado.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ClotyApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthTokenResponse

    @POST("api/auth/solicitar-codigo-apoderado")
    suspend fun solicitarCodigoActivacion(@Body body: SolicitarCodigoActivacionRequest): SolicitarCodigoActivacionResponse

    @POST("api/auth/solicitar-recuperacion-contrasena")
    suspend fun solicitarRecuperacionContrasena(@Body body: SolicitarCodigoActivacionRequest): SolicitarCodigoActivacionResponse

    @POST("api/auth/restablecer-contrasena")
    suspend fun restablecerContrasena(@Body body: RestablecerContrasenaRequest)

    @POST("api/auth/validar-codigo-apoderado")
    suspend fun validarCodigoActivacion(@Body body: ValidarCodigoActivacionRequest)

    @POST("api/auth/activar-cuenta-apoderado")
    suspend fun activarCuentaApoderado(@Body body: ActivarCuentaApoderadoRequest): AuthTokenResponse

    @GET("api/auth/me")
    suspend fun me(): AuthMeResponse

    @POST("api/auth/cambiar-contrasena")
    suspend fun cambiarContrasena(@Body body: CambiarContrasenaRequest)

    // esta parte es nueva
    @GET("api/apoderados/{id}")
    suspend fun obtenerApoderado(@Path("id") id: Int): Apoderado

    @PUT("api/apoderados/{id}")
    suspend fun actualizarApoderado(@Path("id") id: Int, @Body body: ApoderadoRequest): Apoderado

    @GET("api/auth/mis-pupilos")
    suspend fun misPupilos(): List<PupiloResumen>

    @GET("api/notificaciones/apoderado/{idApoderado}")
    suspend fun notificaciones(@Path("idApoderado") idApoderado: Int): List<Notificacion>

    @PATCH("api/notificaciones/{id}/leida")
    suspend fun marcarLeida(@Path("id") id: Int): Notificacion
}
