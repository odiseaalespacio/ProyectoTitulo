package com.example.cloty_colegio

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.cloty_colegio.nfc.NfcUidFormatter
import com.example.cloty_colegio.ui.ClotyViewModel
import com.example.cloty_colegio.ui.navigation.ClotyNavGraph
import com.example.cloty_colegio.ui.theme.Cloty_colegioTheme
import java.util.concurrent.atomic.AtomicLong

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private val viewModel: ClotyViewModel by viewModels()
    private val lastScanMs = AtomicLong(0)

    private val readerCallback = NfcAdapter.ReaderCallback { tag ->
        val now = System.currentTimeMillis()
        val prev = lastScanMs.get()
        if (now - prev < 3000 || !lastScanMs.compareAndSet(prev, now)) return@ReaderCallback
        val uid = NfcUidFormatter.fromTag(tag)
        runOnUiThread { viewModel.onNfcTagDetected(uid) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        enableEdgeToEdge()
        setContent {
            Cloty_colegioTheme {
                ClotyNavGraph(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val options = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000)
        }
        nfcAdapter?.enableReaderMode(
            this,
            readerCallback,
            NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            options
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }
}
