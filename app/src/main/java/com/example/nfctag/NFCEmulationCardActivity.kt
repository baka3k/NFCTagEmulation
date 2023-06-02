package com.example.nfctag

import android.content.ComponentName
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.nfctag.base.BaseActivity
import com.example.nfctag.nfc.cardemulator.MyHostApduService
import org.xmlpull.v1.XmlPullParser

class NFCEmulationCardActivity : BaseActivity() {
    private lateinit var background: ConstraintLayout
    private lateinit var inforEmulation: TextView
    private lateinit var cardEmulation: CardEmulation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_emulation_card)
        inforEmulation = findViewById(R.id.inforEmulation)
        background = findViewById(R.id.background)
        checkNFCCardEmulation()
        cardEmulation = CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(this))
        checkForceDefaultServiceAid()
    }

    private fun checkForceDefaultServiceAid() {
        val aid = parseAid(resources.getXml(R.xml.myaptuservice))
        if (!cardEmulation.isDefaultServiceForAid(
                ComponentName(
                    this,
                    MyHostApduService::class.java
                ), aid
            )
        ) {
            Log.d(
                "NFCEmulationCardActivity",
                "This application is NOT the preferred service for aid $aid"
            )

        } else {
            Log.d(
                "NFCEmulationCardActivity",
                "This application is the preferred service for aid  $aid"
            )
        }
    }

    override fun onResume() {
        setPreferServiceAid()
        super.onResume()
    }

    private fun setPreferServiceAid() {
        cardEmulation.setPreferredService(
            this, ComponentName(
                this,
                MyHostApduService::class.java
            )
        )
    }

    private fun checkNFCCardEmulation() {
        val pm = this.packageManager
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {
            Log.i("MainActivity", "Missing HCE functionality.")
            inforEmulation.text = "ERROR: Missing HCE functionality"
            background.setBackgroundResource(R.color.red)
        } else {
            inforEmulation.text = "GOOD! NFC Emulator Card is running!!!"
            background.setBackgroundResource(R.color.green)
        }
    }

    private fun parseAid(xmlResource: XmlResourceParser): String {
        try {
            xmlResource.use { parser ->
                var eventType: Int
                do {
                    eventType = parser.next()
                    if (eventType == XmlPullParser.START_TAG) {
                        if (parser.name == "aid-filter") {
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i) == "name") {
                                    return parser.getAttributeValue(i)
                                }
                            }
                        }
                    }
                } while (eventType != XmlPullParser.END_DOCUMENT)
                throw IllegalArgumentException("No aid-filter found")
            }
        } catch (e: Exception) {
            throw IllegalArgumentException(e)
        }
    }
}