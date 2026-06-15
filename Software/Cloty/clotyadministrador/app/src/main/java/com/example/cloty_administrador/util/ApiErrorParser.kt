package com.example.cloty_administrador.util

import retrofit2.HttpException

object ApiErrorParser {

    fun mensaje(e: Throwable): String {
        if (e is HttpException) {
            val cuerpo = e.response()?.errorBody()?.string().orEmpty()
            mensajeJson(cuerpo)?.let { return it }
            return when (e.code()) {
                401 -> "SesiÃ³n expirada. Vuelva a iniciar sesiÃ³n."
                403 -> "No tiene permiso para realizar esta operaciÃ³n."
                404 -> "No se encontrÃ³ la informaciÃ³n solicitada."
                409 -> "No se pudo completar la operaciÃ³n porque hay datos en conflicto."
                400 -> "Solicitud invÃ¡lida. Revise los datos ingresados."
                else -> "Error del servidor (${e.code()}). Intente nuevamente."
            }
        }
        val raw = e.message.orEmpty()
        mensajeJson(raw)?.let { return it }
        return when {
            raw.contains("403") -> "No tiene permiso para realizar esta operaciÃ³n."
            raw.contains("401") -> "SesiÃ³n expirada. Vuelva a iniciar sesiÃ³n."
            raw.contains("404") -> "No se encontrÃ³ la informaciÃ³n solicitada."
            raw.contains("409") -> "No se pudo completar la operaciÃ³n porque hay datos en conflicto."
            raw.contains("400") -> "Solicitud invÃ¡lida. Revise los datos ingresados."
            raw.isNotBlank() -> raw
            else -> "OcurriÃ³ un error inesperado. Intente nuevamente."
        }
    }

    private fun mensajeJson(raw: String): String? {
        if (raw.isBlank()) return null
        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(raw)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace("\\u0027", "'")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }
}
