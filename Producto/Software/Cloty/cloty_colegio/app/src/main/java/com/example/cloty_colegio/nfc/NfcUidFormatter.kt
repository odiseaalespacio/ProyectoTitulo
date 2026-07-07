package com.example.cloty_colegio.nfc

import android.nfc.Tag

object NfcUidFormatter {
    fun fromTag(tag: Tag): String = tag.id.joinToString("") { "%02X".format(it) }
}
