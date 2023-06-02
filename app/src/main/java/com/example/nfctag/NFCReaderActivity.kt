package com.example.nfctag

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.nfctag.base.BaseActivity
import com.example.nfctag.nfc.cmd.DigitalKeyPingPong
import com.example.nfctag.nfc.cmd.FelicaPingPong
import com.example.nfctag.nfc.config.NfcReaderType
import com.example.nfctag.nfc.nfcreader.NfcReaderCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NFCReaderActivity : BaseActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var txtTitle: TextView
    private lateinit var txtData: TextView
    private lateinit var nfcReaderCallback: NfcReaderCallback
    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_reader)
        txtTitle = findViewById(R.id.title)
        txtData = findViewById(R.id.datatxt)
        initCardReader()
        observeCardReaderFlow()
        initNfc()
    }

    private fun initCardReader() {
        val nfcType = intent.getIntExtra("type", NfcReaderType.ISO_DEP)
        nfcReaderCallback = if (nfcType == NfcReaderType.ISO_DEP) {
            txtTitle.text = "NFCReader Screen - ISO_DEP reader "
            NfcReaderCallback(pingPong = DigitalKeyPingPong())
        } else {
            // just test only
            // TODO
            txtTitle.text = "NFCReader Screen - NFC-A/B/F reader "
            NfcReaderCallback(pingPong = FelicaPingPong())
        }
    }

    private fun observeCardReaderFlow() {
        coroutineScope.launch {
            nfcReaderCallback.nfcData.collect { nfcData ->
                Log.d(TAG, "#onDataReceived() $nfcData")
                val mess = nfcData.ifEmpty {
                    "NFCData is empty - waiting to read"
                }
                withContext(Dispatchers.Main) {
                    "$mess\n${txtData.text}".also { txtData.text = it }
                }
            }
        }
    }

    private fun initNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        enableNFCReaderMode()
    }

    private fun enableNFCReaderMode() {
        if (nfcAdapter.isEnabled) {
            nfcAdapter.enableReaderMode(
                this, nfcReaderCallback, READER_FLAGS, null
            )
        } else {
            showNFCSettings()
        }
    }

    override fun onPause() {
        disableNFCReaderMode()
        super.onPause()
    }

    private fun disableNFCReaderMode() {
        nfcAdapter.disableReaderMode(this)
    }

    private fun showNFCSettings() {
        Toast.makeText(this, getString(R.string.warning_enable_nfc), Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
    }

    companion object {
        private val TAG = "NFCActivity"
        private const val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NFC_F
    }
}