package com.example.cloty_administrador.util

import android.util.Base64

object JwtClaims {

    fun rol(token: String): String? {
        val parts = token.split('.')
        if (parts.size < 2) return null
        val payload = parts[1]
        val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
        return try {
            val json = String(
                Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP),
                Charsets.UTF_8
            )
            Regex("\"rol\"\\s*:\\s*\"([^\"]+)\"")
                .find(json)
                ?.groupValues
                ?.getOrNull(1)
        } catch (_: Exception) {
            null
        }
    }
}
