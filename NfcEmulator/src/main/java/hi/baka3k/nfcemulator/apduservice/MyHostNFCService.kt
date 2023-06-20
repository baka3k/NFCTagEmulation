package hi.baka3k.nfcemulator.apduservice

import android.nfc.cardemulation.HostNfcFService
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import hi.baka3k.nfcemulator.applet.AppletCommand
import hi.baka3k.nfcemulator.applet.DigitalKeyApplet
import hi.baka3k.nfctool.utils.toHexString
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale

/**
 * Felica card emulation
 * The target HCE-F service must be
 * enabled before communication is possible
 * */
class MyHostNFCService : HostNfcFService() {
    private val digitalKeyApplet = DigitalKeyApplet(AppletCommand())
    private lateinit var deviceName: String
    override fun onCreate() {
        super.onCreate()
        deviceName = getDeviceName()
    }

    override fun processNfcFPacket(apdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.d(
            TAG, "#processNfcFPacket() ${apdu?.toHexString()} ${Looper.myLooper() == mainLooper}"
        )
        if (extras != null) {
            for (s in extras.keySet()) {
                Log.d(
                    TAG, "Got extras $s:${extras.get(s)}"
                )
            }
        }

        if (apdu != null) {
            if (apdu.size < 1 + 1 + 8) {
                return "ERROR!!! reason: CMD is not correct".toByteArray()
            } else {
                val nfcid2 = ByteArray(8)
                System.arraycopy(apdu, 2, nfcid2, 0, 8)
                return if (apdu[1] == 0x04.toByte()) {
                    val resp = ByteArray(1 + 1 + 8 + 1)
                    resp[0] = 11.toByte()  // LEN
                    resp[1] = 0x05.toByte()// Response Code
                    System.arraycopy(nfcid2, 0, resp, 2, 8)// NFCID2
                    resp[10] = 0.toByte()   // Mode
                    resp
                } else {
                    "ERROR!!! reason: CMD null".toByteArray()
                }
            }
        } else {
            return "ERROR!!! reason: CMD null".toByteArray()
        }
    }

    private var messageCounter = 0

    private fun getCardInfo(): ByteArray {
        return "getCardInfo()".toByteArray()
    }

    private fun authenticate(): ByteArray {
        return "authenticate()".toByteArray()
    }

    private fun getPublicKey(): ByteArray {
        return "getPublicKey()".toByteArray()
    }

    private fun response(command: Byte, contents: ByteArray?): ByteArray {
        return try {
            val bout = ByteArrayOutputStream()
            bout.write(contents)
            bout.write(0x91)
            bout.write(command.toInt())
            bout.toByteArray()
        } catch (e: IOException) {
            throw RuntimeException()
        }
    }

    override fun onDeactivated(reason: Int) {
        Log.i(TAG, "Deactivated: $reason")
    }

    private fun getWelcomeMessage(): ByteArray {
        return "Hello ---------------------------!".toByteArray()
    }

    private fun getNextMessage(): ByteArray {
        return ("Data from $deviceName Host Emulation: " + messageCounter++).toByteArray()
    }

    private fun selectAidApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0.toByte() && apdu[1] == 0xa4.toByte()
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.lowercase(Locale.getDefault())
                .startsWith(manufacturer.lowercase(Locale.getDefault()))
        ) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }


    private fun capitalize(s: String): String {
        if (s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            first.uppercaseChar().toString() + s.substring(1)
        }
    }

    companion object {
        private const val TAG = "Card Emulation F"
    }
}