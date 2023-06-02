package com.example.nfctag.nfc.cardemulator

import android.nfc.cardemulation.HostApduService
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.example.nfctag.nfc.applet.AppletCommand
import com.example.nfctag.nfc.applet.DigitalKeyApplet
import com.example.nfctag.nfc.data.CommandAPDU
import com.example.nfctag.nfc.data.Config.INS_AUTHENTICATE
import com.example.nfctag.nfc.data.Config.INS_GET_CARD_INFO
import com.example.nfctag.nfc.data.Config.INS_GET_PUBLIC_KEY
import com.example.nfctag.nfc.data.Config.ISO_SELECT_APPLICATION
import com.example.nfctag.nfc.data.Config.OPERATION_OK
import com.example.nfctag.nfc.data.Config.SELECT_APPLICATION
import com.example.nfctag.nfc.toHexString
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
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
        Log.d(
            TAG,
            "#processCommandApdu() ${apdu?.toHexString()} ${Looper.myLooper() == mainLooper}"
        )
        if (extras != null) {
            for (s in extras.keySet()) {
                Log.d(
                    TAG, "Got extras $s:${extras.get(s)}"
                )
            }
        }

        if (apdu != null) {
            val command = CommandAPDU(apdu)
            val ins = command.getINS()
            var response = response(
                OPERATION_OK,
                String.format("Hello operation %02X", ins).toByteArray(StandardCharsets.UTF_8)
            )
            if (ins == ISO_SELECT_APPLICATION || ins == SELECT_APPLICATION) {
                val application: String = command.getData().toHexString(1, 3)
                Log.i(TAG, "Selected Application $application")
                response = response(
                    OPERATION_OK, byteArrayOf(0x03, 0x04, 0x05)
                )
            } else if (ins == INS_GET_PUBLIC_KEY) {
                response = getPublicKey()
            } else if (ins == INS_AUTHENTICATE) {
                response = authenticate()
            } else if (ins == INS_GET_CARD_INFO) {
                response = getCardInfo()
            } else {
                response = getNextMessage()
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
        private const val TAG = "Card Emulation"
    }
}