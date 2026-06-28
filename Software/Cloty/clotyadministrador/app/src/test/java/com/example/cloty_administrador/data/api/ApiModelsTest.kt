package com.example.cloty_administrador.data.api

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiModelsTest {

    @Test
    fun colegioRequestConservaCamposUbicacion() {
        val req = ColegioRequest(
            rut = "12345678-5",
            nombre = "Liceo Central",
            email = "contacto@liceo.cl",
            telefono = "+56912345678",
            codigoComuna = "13101",
            calleNumero = "Av. Libertador 100"
        )
        assertEquals("13101", req.codigoComuna)
        assertEquals("Av. Libertador 100", req.calleNumero)
    }

    @Test
    fun authTokenResponseIncluyeExpiracion() {
        val token = AuthTokenResponse(token = "jwt", tokenType = "Bearer", expiresInMs = 3600000)
        assertEquals("Bearer", token.tokenType)
        assertEquals(3600000L, token.expiresInMs)
    }
}
