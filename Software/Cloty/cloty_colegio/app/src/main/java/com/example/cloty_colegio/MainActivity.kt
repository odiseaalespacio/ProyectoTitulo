package com.example.cloty_colegio

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.cloty_colegio.nfc.NfcUidFormatter
import com.example.cloty_colegio.ui.navigation.ClotyNavGraph
import com.example.cloty_colegio.ui.theme.Cloty_colegioTheme

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var ultimoUid by mutableStateOf<String?>(null)
    private var scanCount by mutableIntStateOf(0)

    private val readerCallback = NfcAdapter.ReaderCallback { tag ->
        val uid = NfcUidFormatter.fromTag(tag)
        runOnUiThread {
            ultimoUid = uid
            scanCount++
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        enableEdgeToEdge()
        setContent {
            Cloty_colegioTheme {
                ClotyNavGraph(ultimoUidNfc = ultimoUid, scanCount = scanCount)
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
