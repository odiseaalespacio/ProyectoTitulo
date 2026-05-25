package com.example.cloty_colegio.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ClotyApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthTokenResponse

    @POST("api/auth/activar-cuenta-colegio")
    suspend fun activarCuentaColegio(@Body body: ActivarCuentaColegioRequest): AuthTokenResponse

    @GET("api/auth/me")
    suspend fun me(): AuthMeResponse

    @POST("api/colegio/operaciones/escanear")
    suspend fun escanear(@Body body: ScanPrendaRequest): OperacionPrendaResponse

    @GET("api/colegio/operaciones/dashboard")
    suspend fun dashboard(): ColegioDashboard

    @POST("api/auth/cambiar-contrasena")
    suspend fun cambiarContrasena(@Body body: CambiarContrasenaRequest)
}
