package com.example.cloty_administrador.util

import com.example.cloty_administrador.data.api.SuperUsuario

object SuperRootUser {
    const val USERNAME = "superadmin"
    const val RUT = "00000000-0"

    fun esRoot(superUsuario: SuperUsuario, username: String?): Boolean =
        username == USERNAME && superUsuario.rut == RUT
}
