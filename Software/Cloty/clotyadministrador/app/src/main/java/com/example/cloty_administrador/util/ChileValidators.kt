package com.example.cloty_administrador.util


import android.util.Patterns


object ChileValidators {


    const val MSG_RUT_FORMATO = "Use nÃºmero y dÃ­gito verificador separados por guiÃ³n (ej: 30686957-4 o 30.686.957-4)"

    const val MSG_RUT_DV = "DÃ­gito verificador incorrecto"

    const val MSG_RUT_REQUERIDO = "Debe ingresar un RUT"

    const val HINT_RUT = "NÃºmero-DV con guiÃ³n (puntos opcionales)"

    const val MSG_EMAIL_INVALIDO = "Correo electrÃ³nico invÃ¡lido"

    const val MSG_EMAIL_REQUERIDO = "Debe ingresar un correo electrÃ³nico"

    const val MSG_TELEFONO_INVALIDO = "TelÃ©fono chileno invÃ¡lido (mÃ³vil: 9 dÃ­gitos, ej: 912345678)"

    const val MSG_TELEFONO_REQUERIDO = "Debe ingresar un telÃ©fono"

    const val HINT_TELEFONO = "MÃ³vil: 9XXXXXXXX o +569XXXXXXXX"


    fun normalizarRutParaApi(rut: String): String {

        val partes = parsearRut(rut)

        if (partes != null) {

            return "${partes.first}-${partes.second}"

        }

        val clean = rut.replace(".", "").replace(" ", "").replace("-", "").trim().uppercase()

        if (clean.length >= 2) {

            return "${clean.dropLast(1)}-${clean.last()}"

        }

        return rut.trim()

    }


    fun esRutValido(rut: String): Boolean = mensajeErrorRut(rut) == null


    fun esEmailValido(email: String, obligatorio: Boolean = false): Boolean =

        mensajeErrorEmail(email, obligatorio) == null


    fun esTelefonoChilenoValido(telefono: String, obligatorio: Boolean = false): Boolean =

        mensajeErrorTelefono(telefono, obligatorio) == null


    fun mensajeErrorRut(rut: String, obligatorio: Boolean = true, mostrarVacios: Boolean = false): String? {

        val t = rut.trim()

        if (t.isEmpty()) return if (obligatorio && mostrarVacios) MSG_RUT_REQUERIDO else null

        val sinPuntos = t.replace(".", "").replace(" ", "").uppercase()

        if (!sinPuntos.contains("-")) return MSG_RUT_FORMATO

        val idx = sinPuntos.indexOf('-')

        if (idx <= 0 || idx != sinPuntos.lastIndexOf('-') || idx != sinPuntos.length - 2) {

            return MSG_RUT_FORMATO

        }

        val cuerpo = sinPuntos.substring(0, idx)

        val dv = sinPuntos[idx + 1]

        if (!cuerpo.all { it.isDigit() } || cuerpo.length !in 7..8) return MSG_RUT_FORMATO

        if (dv != '0' && dv != 'K' && !dv.isDigit()) return MSG_RUT_FORMATO

        return if (dv == calcularDigitoVerificador(cuerpo)) null else MSG_RUT_DV

    }


    fun mensajeErrorEmail(email: String, obligatorio: Boolean = false, mostrarVacios: Boolean = false): String? {

        val t = email.trim()

        if (t.isEmpty()) return if (obligatorio && mostrarVacios) MSG_EMAIL_REQUERIDO else null

        return if (Patterns.EMAIL_ADDRESS.matcher(t).matches()) null else MSG_EMAIL_INVALIDO

    }


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


    fun primerMensajeError(vararg mensajes: String?): String? =

        mensajes.firstOrNull { !it.isNullOrBlank() }


    private fun parsearRut(rut: String): Pair<String, Char>? {

        val sinPuntos = rut.replace(".", "").replace(" ", "").trim().uppercase()

        if (!sinPuntos.contains("-")) return null

        val idx = sinPuntos.indexOf('-')

        if (idx <= 0 || idx != sinPuntos.lastIndexOf('-') || idx != sinPuntos.length - 2) return null

        val cuerpo = sinPuntos.substring(0, idx)

        val dv = sinPuntos[idx + 1]

        if (!cuerpo.all { it.isDigit() } || cuerpo.length !in 7..8) return null

        if (dv != '0' && dv != 'K' && !dv.isDigit()) return null

        if (dv != calcularDigitoVerificador(cuerpo)) return null

        return cuerpo to dv

    }


    private fun calcularDigitoVerificador(cuerpo: String): Char {

        var suma = 0

        var factor = 2

        for (c in cuerpo.reversed()) {

            suma += c.digitToInt() * factor

            factor = if (factor == 7) 2 else factor + 1

        }

        val resto = 11 - (suma % 11)

        return when (resto) {

            11 -> '0'

            10 -> 'K'

            else -> resto.digitToChar()

        }

    }

}

