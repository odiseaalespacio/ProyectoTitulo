package com.example.cloty_administrador

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.cloty_administrador.nfc.NfcUidFormatter
import com.example.cloty_administrador.ui.navigation.ClotyNavGraph
import com.example.cloty_administrador.ui.theme.ClotyadministradorTheme

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var ultimoUid by mutableStateOf<String?>(null)

    private val readerCallback = NfcAdapter.ReaderCallback { tag ->
        val uid = NfcUidFormatter.fromTag(tag)
        runOnUiThread { ultimoUid = uid }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        enableEdgeToEdge()
        setContent {
            ClotyadministradorTheme {
                ClotyNavGraph(ultimoUidNfc = ultimoUid)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(
            this,
            readerCallback,
            NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }
}
