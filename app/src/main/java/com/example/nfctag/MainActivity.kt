package com.example.nfctag

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.nfctag.felica.NFCFelicaActivity
import com.example.nfctag.nfc.config.NfcReaderType


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openNFCReader(view: View) {
        val intent = Intent(this, NFCReaderActivity::class.java)
        if (view.id == R.id.nfcReaderFelica) {
            intent.putExtra("type", NfcReaderType.NFC_A)
        } else {
            intent.putExtra("type", NfcReaderType.ISO_DEP)
        }
        startActivity(intent)
    }

    fun openNFCEmulation(view: View) {
        val intent = Intent(this, NFCEmulationCardActivity::class.java)
        startActivity(intent)
    }

    fun openNFCFelicaEmulation(view: View) {
        val intent = Intent(this, NFCFelicaActivity::class.java)
        startActivity(intent)
    }
}
