package com.example.cloty_colegio.util

import retrofit2.HttpException

object ApiErrorParser {

    fun mensaje(e: Throwable): String {
        if (e is HttpException) {
            val cuerpo = e.response()?.errorBody()?.string().orEmpty()
            mensajeJson(cuerpo)?.let { return it }
            return when (e.code()) {
                401 -> "Sesión expirada. Vuelva a iniciar sesión."
                403 -> "No tiene permiso para realizar esta operación."
                404 -> "No se encontró la información solicitada."
                409 -> "No se pudo completar la operación porque hay datos en conflicto."
                400 -> "Solicitud inválida. Revise los datos ingresados."
                else -> "Error del servidor (${e.code()}). Intente nuevamente."
            }
        }
        val raw = e.message.orEmpty()
        mensajeJson(raw)?.let { return it }
        return when {
            raw.contains("403") -> "No tiene permiso para realizar esta operación."
            raw.contains("401") -> "Sesión expirada. Vuelva a iniciar sesión."
            raw.contains("404") -> "No se encontró la información solicitada."
            raw.contains("409") -> "No se pudo completar la operación porque hay datos en conflicto."
            raw.contains("400") -> "Solicitud inválida. Revise los datos ingresados."
            raw.isNotBlank() -> raw
            else -> "Ocurrió un error inesperado. Intente nuevamente."
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
