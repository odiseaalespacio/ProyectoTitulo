package com.example.cloty_administrador.util

import android.util.Patterns

// esto es nuevo
object ChileValidators {

    const val MSG_RUT_INVALIDO = "RUT inválido. Formato: 12.345.678-9 (dígito verificador 0-9 o K)"
    const val MSG_RUT_REQUERIDO = "Debe ingresar un RUT"
    const val HINT_RUT = "7 u 8 dígitos + verificador (0-9 o K)"
    const val MSG_EMAIL_INVALIDO = "Correo electrónico inválido"
    const val MSG_EMAIL_REQUERIDO = "Debe ingresar un correo electrónico"
    const val MSG_TELEFONO_INVALIDO = "Teléfono chileno inválido (móvil: 9 dígitos, ej: 912345678)"
    const val MSG_TELEFONO_REQUERIDO = "Debe ingresar un teléfono"
    const val HINT_TELEFONO = "Móvil: 9XXXXXXXX o +569XXXXXXXX"

    fun normalizarRutParaApi(rut: String): String {
        val clean = rut.replace(".", "").replace(" ", "").trim().uppercase()
        if (clean.length < 2) return clean
        return "${clean.dropLast(1)}-${clean.last()}"
    }

    fun esRutValido(rut: String): Boolean = mensajeErrorRut(rut) == null

    fun esEmailValido(email: String, obligatorio: Boolean = false): Boolean =
        mensajeErrorEmail(email, obligatorio) == null

    fun esTelefonoChilenoValido(telefono: String, obligatorio: Boolean = false): Boolean =
        mensajeErrorTelefono(telefono, obligatorio) == null

    // esto es nuevo
    fun mensajeErrorRut(rut: String, obligatorio: Boolean = true, mostrarVacios: Boolean = false): String? {
        val t = rut.trim()
        if (t.isEmpty()) return if (obligatorio && mostrarVacios) MSG_RUT_REQUERIDO else null
        val clean = t.replace(".", "").replace("-", "").replace(" ", "").uppercase()
        if (clean.length !in 8..9) return MSG_RUT_INVALIDO
        val cuerpo = clean.dropLast(1)
        val dv = clean.last()
        if (!cuerpo.all { it.isDigit() }) return MSG_RUT_INVALIDO
        if (dv != '0' && dv != 'K' && !dv.isDigit()) return MSG_RUT_INVALIDO
        var suma = 0
        var factor = 2
        for (c in cuerpo.reversed()) {
            suma += c.digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val resto = 11 - (suma % 11)
        val esperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
        return if (dv == esperado) null else MSG_RUT_INVALIDO
    }

    // esto es nuevo
    fun mensajeErrorEmail(email: String, obligatorio: Boolean = false, mostrarVacios: Boolean = false): String? {
        val t = email.trim()
        if (t.isEmpty()) return if (obligatorio && mostrarVacios) MSG_EMAIL_REQUERIDO else null
        return if (Patterns.EMAIL_ADDRESS.matcher(t).matches()) null else MSG_EMAIL_INVALIDO
    }

    // esto es nuevo
    fun mensajeErrorTelefono(telefono: String, obligatorio: Boolean = false, mostrarVacios: Boolean = false): String? {
        val t = telefono.trim()
        if (t.isEmpty()) return if (obligatorio && mostrarVacios) MSG_TELEFONO_REQUERIDO else null
        val digits = t.filter { it.isDigit() }
        val valido = when {
            digits.length == 9 && digits.startsWith("9") -> true
            digits.length == 11 && digits.startsWith("569") -> true
            digits.length == 8 && digits.first() in '2'..'9' -> true
            else -> false
        }
        return if (valido) null else MSG_TELEFONO_INVALIDO
    }

    // esto es nuevo
    fun primerMensajeError(vararg mensajes: String?): String? =
        mensajes.firstOrNull { !it.isNullOrBlank() }
}
