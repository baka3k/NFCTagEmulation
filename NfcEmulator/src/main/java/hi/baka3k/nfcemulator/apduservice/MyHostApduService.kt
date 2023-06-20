package hi.baka3k.nfcemulator.apduservice

import android.nfc.cardemulation.HostApduService
import android.os.Build
import android.os.Bundle
import android.util.Log
import hi.baka3k.nfcemulator.applet.AppletCommand
import hi.baka3k.nfcemulator.applet.DigitalKeyApplet
import hi.baka3k.nfcemulator.data.Config.INS_AUTHENTICATE
import hi.baka3k.nfcemulator.data.Config.INS_GET_CARD_INFO
import hi.baka3k.nfcemulator.data.Config.INS_GET_PUBLIC_KEY
import hi.baka3k.nfcemulator.data.Config.ISO_SELECT_APPLICATION
import hi.baka3k.nfcemulator.data.Config.OPERATION_OK
import hi.baka3k.nfcemulator.data.Config.SELECT_APPLICATION
import hi.baka3k.nfcemulator.data.Config.STATUS_OK
import hi.baka3k.nfcemulator.secure.KeyECDSA
import hi.baka3k.nfctool.data.CommandAPDU
import hi.baka3k.nfctool.utils.toHexString
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale


class MyHostApduService : HostApduService() {
    private val digitalKeyApplet = DigitalKeyApplet(AppletCommand())
    private lateinit var deviceName: String
    override fun onCreate() {
        super.onCreate()
        deviceName = getDeviceName()
    }

    private var messageCounter = 0
    override fun processCommandApdu(apdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.d(TAG, "#processCommandApdu() ${apdu?.toHexString()}")
        if (extras != null) {
            for (s in extras.keySet()) {
                Log.d(
                    TAG, "Got extras $s:${extras.get(s)}"
                )
            }
        }

        if (apdu != null) {
            val commandRequest = CommandAPDU(apdu)
            val response: ByteArray = when (commandRequest.getINS()) {
                ISO_SELECT_APPLICATION, SELECT_APPLICATION -> {
                    val application: String = commandRequest.getData().toHexString(1, 3)
                    Log.i(TAG, "SELECT_APPLICATION $application")
                    getOkCommand()
                }

                INS_GET_PUBLIC_KEY -> {
                    Log.i(TAG, "INS_GET_PUBLIC_KEY")
                    getPublicKey()
                }

                INS_AUTHENTICATE -> {
                    Log.i(TAG, "INS_AUTHENTICATE")
                    authenticate(commandRequest)
                }

                INS_GET_CARD_INFO -> {
                    getCardInfo()
                }

                else -> {
                    getNextMessage()
                }
            }
            return response
        } else {
            Log.i(TAG, "hit APDU null: ")
            return "ERROR!!! reason: APDU null".toByteArray()
        }
    }


    private fun getCardInfo(): ByteArray {
        return "getCardInfo()".toByteArray()
    }

    private fun authenticate(command: CommandAPDU): ByteArray {
        return KeyECDSA.sign(command.getData())
    }

    private fun getPublicKey(): ByteArray {
        return response(OPERATION_OK.toByte(), KeyECDSA.keyPair.public.encoded)
    }

    private fun getOkCommand() = response(
        OPERATION_OK.toByte(), byteArrayOf(0x03, 0x04, 0x05)
    )

    private fun response(command: Byte, contents: ByteArray?): ByteArray {
        return try {
            val bout = ByteArrayOutputStream()
            bout.write(contents)
            bout.write(STATUS_OK)
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
        private const val TAG = "Card Emulation"
    }
}