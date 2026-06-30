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

    suspend fun solicitarCodigoActivacion(rut: String) =
        api.solicitarCodigoActivacion(
            com.example.cloty_apoderado.data.api.SolicitarCodigoActivacionRequest(rut)
        )

    suspend fun solicitarRecuperacionContrasena(rut: String) =
        api.solicitarRecuperacionContrasena(
            com.example.cloty_apoderado.data.api.SolicitarCodigoActivacionRequest(rut)
        )

    suspend fun restablecerContrasena(rut: String, codigo: String, password: String) =
        api.restablecerContrasena(
            com.example.cloty_apoderado.data.api.RestablecerContrasenaRequest(rut, codigo, password)
        )

    suspend fun validarCodigoActivacion(rut: String, codigo: String) =
        api.validarCodigoActivacion(
            com.example.cloty_apoderado.data.api.ValidarCodigoActivacionRequest(rut, codigo)
        )

    suspend fun activarCuenta(rut: String, codigo: String, password: String) {
        val req = com.example.cloty_apoderado.data.api.ActivarCuentaApoderadoRequest(rut, codigo, password)
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

    suspend fun obtenerApoderado(id: Int) = api.obtenerApoderado(id)

    suspend fun actualizarApoderado(id: Int, req: com.example.cloty_apoderado.data.api.ApoderadoRequest) =
        api.actualizarApoderado(id, req)

    suspend fun listarRegiones() = api.listarRegiones()

    suspend fun listarComunas(codigoRegion: String) = api.listarComunas(codigoRegion)

    suspend fun obtenerComuna(codigoComuna: String) =
        try {
            api.obtenerComuna(codigoComuna)
        } catch (_: Exception) {
            null
        }
}
