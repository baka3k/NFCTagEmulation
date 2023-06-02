package com.example.nfctag.se

import android.os.Bundle
import android.se.omapi.Channel
import android.se.omapi.Reader
import android.se.omapi.SEService
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.nfctag.R
import com.example.nfctag.nfc.applet.AppletCommand
import com.example.nfctag.nfc.data.CommandAPDU
import com.example.nfctag.nfc.data.Config
import com.example.nfctag.nfc.hexStringToByteArray
import com.example.nfctag.nfc.toHex
import java.util.concurrent.Executors

/**
 * Testing class - do not include in release version
 * */
class SEActivity : AppCompatActivity(), SEService.OnConnectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_se)
    }

    fun testTelephony(view: View) {
        try {
            packageManager.systemAvailableFeatures
            if (packageManager.hasSystemFeature(android.content.Context.TELEPHONY_SERVICE)) {
                val tm =
                    getSystemService(android.content.Context.TELEPHONY_SERVICE) as TelephonyManager
                val resp = tm.iccOpenLogicalChannel(
                    "01020304050607",  // AID
                    0 // p2
                )
                val ch = resp.channel
                if (ch > 0) {
                    val sResp = tm.iccTransmitApduLogicalChannel(
                        ch,
                        0, 1, 2, 3, 4, "HAHA"
                    )
                    tm.iccCloseLogicalChannel(ch)
                }
            } else {
                Log.d(TAG, "#testTelephony() have no TELEPHONY_SERVICE")
            }
        } catch (e: Exception) {
            Log.e(TAG, "#testTelephony() err:$e  !!!!", e)
        }

    }

    val exe = Executors.newSingleThreadExecutor()
    lateinit var se: SEService
    fun testSecureElement(view: View) {
        try {
            se = SEService(
                this,  // context
                exe,  // callbacks processor
                this // listener
            )
            val command = CommandAPDU(
                cla = 0x00,
                ins = 0xA4,
                p1 = 0x04,
                p2 = 0x00,
                data = Config.DIGITAL_KEY_FRAMWORK_AID.hexStringToByteArray(), // AID_DIGITAL_KEY
            )
            command
            if (se.isConnected) {
                val rdrs: Array<Reader> = se.readers
                if (rdrs.isNotEmpty()) {
//                    val sess = rdrs[0].openSession()
//                    val ch: Channel? = sess.openLogicalChannel(
//                        Config.DIGITAL_KEY_FRAMWORK_AID.hexStringToByteArray(),
//                        0 // p2
//                    )
//                    if (ch == null) {
//                        Log.d(TAG, "#testTelephony() openLogicalChannel - FAIL - channel null")
//                    } else {
//                        val respApdu: ByteArray? =
//                            ch?.transmit(AppletCommand().createCaCmd().getBytes())
//
//                        if (respApdu != null) {
//                            Log.d(TAG, "#testTelephony() response - string :${String(respApdu)}")
//                            Log.d(TAG, "#testTelephony() response - hex: ${respApdu?.toHex()}")
//                        } else {
//                            Log.d(TAG, "#testTelephony() response - transmit NULL ")
//                        }
//                        ch?.close()
//                    }
                }
            }

        } catch (e: Exception) {
//            Log.e(TAG, "#testSecureElement() err:$e  !!!!", e)
        }

    }

    override fun onConnected() {
        val command = CommandAPDU(
            cla = 0x00,
            ins = 0xA4,
            p1 = 0x04,
            p2 = 0x00,
            data = Config.DIGITAL_KEY_FRAMWORK_AID.hexStringToByteArray(), // AID_DIGITAL_KEY
        )
        Log.d(TAG, "EService.OnConnectedListener() !!!!")
        if (se.isConnected) {
            val rdrs: Array<Reader> = se.readers
            Log.d(TAG, "EService.OnConnectedListener() rdrs size :${rdrs.size}")
            if (rdrs.isNotEmpty()) {
                rdrs.onEach {
                    if (it.isSecureElementPresent) {
                        val sess = it.openSession()
//                        val ch: Channel? = sess.openLogicalChannel(
////                            "A00000015141434C00".hexStringToByteArray(),
//                            "A000000476416E64726F6964435453FF".hexStringToByteArray(),
////                            "A000000063504B43532D3135".hexStringToByteArray(),
//                            0 // p2
//                        )
//                        A000000063504B43532D3135
//                        val ch: Channel? =
//                            sess.openLogicalChannel(
//                                ISD_AID
//                            )
                        val ch: Channel? = sess.openLogicalChannel(
                            "9f36407ead0639fc966f14dde7970f68".hexStringToByteArray(), 0x00
                        )
                        if (ch == null) {
                            Log.d(TAG, "#testTelephony() openLogicalChannel - FAIL - channel null")
                        } else {
                            val respApdu: ByteArray? =
                                ch.transmit(AppletCommand().createCaCmd().getBytes())

                            if (respApdu != null) {
                                Log.d(
                                    TAG,
                                    "#testTelephony() response - string :${String(respApdu)}"
                                )
                                Log.d(TAG, "#testTelephony() response - hex: ${respApdu.toHex()}")
                            } else {
                                Log.d(TAG, "#testTelephony() response - transmit NULL ")
                            }
                            ch.close()
                        }
                    } else {
//                        Log.d(TAG, "#testTelephony() $it is not secure ElementPresent")
                    }
                }

            }
        }
    }

    companion object {
        const val TAG = "HAHAHA"
        private val ISD_AID = byteArrayOf(0xA0.toByte(), 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00)
    }
}
