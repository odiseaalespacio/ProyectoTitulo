package com.example.cloty_apoderado.data

import com.example.cloty_apoderado.data.api.ApiClient
import com.example.cloty_apoderado.data.api.AuthMeResponse
import com.example.cloty_apoderado.data.api.CambiarContrasenaRequest
import com.example.cloty_apoderado.data.api.LoginRequest
import com.example.cloty_apoderado.data.api.Notificacion
import com.example.cloty_apoderado.data.api.PupiloResumen

class ClotyRepository(context: android.content.Context) {

    val tokenStore = TokenStore(context.applicationContext)

    init {
        ApiClient.init(tokenStore)
    }

    private val api get() = ApiClient.api

    suspend fun login(identificador: String, password: String) {
        tokenStore.saveToken(api.login(LoginRequest(identificador, password)).token)
    }

    suspend fun activarCuenta(rut: String, password: String) {
        val req = com.example.cloty_apoderado.data.api.ActivarCuentaApoderadoRequest(rut, password)
        tokenStore.saveToken(api.activarCuentaApoderado(req).token)
    }

    suspend fun logout() = tokenStore.clear()

    suspend fun me(): AuthMeResponse = api.me()

    suspend fun misPupilos(): List<PupiloResumen> = api.misPupilos()

    suspend fun notificaciones(idApoderado: Int): List<Notificacion> =
        api.notificaciones(idApoderado)

    suspend fun marcarLeida(id: Int) = api.marcarLeida(id)

    suspend fun cambiarContrasena(actual: String, nueva: String) =
        api.cambiarContrasena(CambiarContrasenaRequest(actual, nueva))
}
