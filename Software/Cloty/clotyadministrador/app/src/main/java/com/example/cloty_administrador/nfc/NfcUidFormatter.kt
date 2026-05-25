package com.example.cloty_administrador.nfc

import android.nfc.Tag

object NfcUidFormatter {
    fun fromTag(tag: Tag): String {
        return tag.id.joinToString("") { byte -> "%02X".format(byte) }
    }
}
