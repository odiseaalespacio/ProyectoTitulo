package com.example.cloty_administrador.util

import com.example.cloty_administrador.data.api.Usuario

object SuperRootUser {
    const val USERNAME = "superadmin"
    const val RUT = "00000000-0"

    fun esRoot(usuario: Usuario): Boolean =
        usuario.username == USERNAME && usuario.rut == RUT
}
