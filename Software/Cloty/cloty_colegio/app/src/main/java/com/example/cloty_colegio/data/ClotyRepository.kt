package com.example.cloty_colegio.data

import com.example.cloty_colegio.data.api.ApiClient
import com.example.cloty_colegio.data.api.AuthMeResponse
import com.example.cloty_colegio.data.api.ColegioDashboard
import com.example.cloty_colegio.data.api.LoginRequest
import com.example.cloty_colegio.data.api.OperacionPrendaResponse
import com.example.cloty_colegio.data.api.ScanPrendaRequest

class ClotyRepository(context: android.content.Context) {

    val tokenStore = TokenStore(context.applicationContext)

    init {
        ApiClient.init(tokenStore)
    }

    private val api get() = ApiClient.api

    suspend fun login(identificador: String, password: String) {
        val response = api.login(LoginRequest(identificador, password))
        tokenStore.saveToken(response.token)
    }

    suspend fun logout() = tokenStore.clear()

    suspend fun me(): AuthMeResponse = api.me()

    suspend fun escanear(uidNfc: String, ubicacion: String?): OperacionPrendaResponse =
        api.escanear(ScanPrendaRequest(uidNfc = uidNfc, ubicacion = ubicacion))

    suspend fun dashboard(): ColegioDashboard = api.dashboard()

    suspend fun cambiarContrasena(actual: String, nueva: String) =
        api.cambiarContrasena(
            com.example.cloty_colegio.data.api.CambiarContrasenaRequest(actual, nueva)
        )
}
