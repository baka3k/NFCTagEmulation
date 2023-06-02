package com.example.nfctag.felica

import android.content.ComponentName
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.cardemulation.NfcFCardEmulation
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.nfctag.R
import com.example.nfctag.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Just testing only - Felica is not Supported by CCC
 * */
class NFCFelicaActivity : BaseActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val componentNameService = ComponentName(
        "com.example.nfctag",
        "com.example.nfctag.nfc.cardemulator.MyHostNFCService"
    )
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcFCardEmulation: NfcFCardEmulation
    private lateinit var txtData: TextView
    private lateinit var txtInforEmulation: TextView
    private lateinit var background: ConstraintLayout
    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_felica)
        background = findViewById(R.id.background)
        txtData = findViewById(R.id.textView)
        txtInforEmulation = findViewById(R.id.inforEmulation)
        initNfc()
        checkNFCCardEmulation()
    }

    private fun checkNFCCardEmulation() {
        val pm = this.packageManager
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF)) {
            Log.i("MainActivity", "Missing HCE functionality.")
            txtInforEmulation.text = "ERROR: Missing Felica NFCF"
            background.setBackgroundResource(R.color.red)
        } else {
            txtInforEmulation.text =
                "GOOD! NFC Emulator Card is running!!!\nYou MUST to open this activity for NFCF reader"
            background.setBackgroundResource(R.color.green)
        }
    }

    private fun initNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcFCardEmulation = NfcFCardEmulation.getInstance(nfcAdapter)
    }

    /**
     * As mentioned in section 2.3 “Enabling and disabling the HCE-F service”, the target HCE-F service must be
     * enabled before communication is possible. Figure 3-4 shows a sample of how to enable the service. The
     * target HCE-F service can be enabled by calling the NfcFCardEmulation#enableService method. It can be
     * disabled by calling the NfcFCardEmulation#disableService method.
     * */
    private fun enableServiceHCEF() {
//        val orgSys = nfcFCardEmulation.getSystemCodeForService(componentNameService)
//        Log.d("HAHAHA","#enableServiceHCEF(): $orgSys")
//        nfcFCardEmulation.registerSystemCodeForService(componentNameService,orgSys)
        nfcFCardEmulation.enableService(this, componentNameService)
    }

    private fun disableServiceHCEF() {
        nfcFCardEmulation.disableService(this)
    }

    override fun onResume() {
        super.onResume()
        enableServiceHCEF()
    }

    override fun onPause() {
        disableServiceHCEF()
        super.onPause()
    }

    private fun setSys(sys: String) {
        val resultSys = nfcFCardEmulation.registerSystemCodeForService(componentNameService, sys)
        nfcFCardEmulation.enableService(this, componentNameService)
    }

    private fun setIDm(idm: String) {
        nfcFCardEmulation.disableService(this)
        val resultIdm = nfcFCardEmulation.setNfcid2ForService(componentNameService, idm)
        nfcFCardEmulation.enableService(this, componentNameService)
    }
}