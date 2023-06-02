package com.example.nfctag.nfc.cmd

import android.util.Log
import com.example.nfctag.nfc.data.CommandAPDU
import com.example.nfctag.nfc.data.Config
import com.example.nfctag.nfc.hexStringToByteArray
import com.example.nfctag.nfc.nfcreader.NFCTagReader

class DigitalKeyPingPong : PingPong() {
    override fun execute(nfcTagReader: NFCTagReader, onReponse: (ByteArray?) -> Unit) {
        val response = sendRequestSelectAidApdu(nfcTagReader)
        onReponse(response)
        val ping = getPing()
        var count = 0
        var elapsed: Long = 0
        var max = Long.MIN_VALUE
        var min = Long.MAX_VALUE
        while (nfcTagReader.isConnected()) {
            var time = System.currentTimeMillis()
            val pong: ByteArray? = nfcTagReader.transceive(ping)
            onReponse(pong)
            time = System.currentTimeMillis() - time
            if (!isPong(ping, pong)) {
                Log.d(TAG, "No pong to the ping")
//                break - disable to test message from another mobile
            } else {
                if (time > max) {
                    max = time
                }
                if (time < min) {
                    min = time
                }
                count++
                elapsed += time
                Log.d(
                    TAG,
                    "Ping-pong in " + time + " ms (average " + elapsed / count + " ms. Min " + min + " / max " + max + ")"
                )
            }
        }
    }

    /**
     * init first command by select AID
     * MUST BE called at the fist connection
     * */
    private fun sendRequestSelectAidApdu(nfcTagReader: NFCTagReader): ByteArray? {
        val command = CommandAPDU(
            cla = 0x00,
            ins = 0xA4,
            p1 = 0x04,
            p2 = 0x00,
            data = Config.DIGITAL_KEY_FRAMWORK_AID.hexStringToByteArray(), // AID_DIGITAL_KEY
        )
        return nfcTagReader.transceive(command.getBytes())
    }

    private fun getPong(): ByteArray {
        val ping: ByteArray = getPing()
        val pong = ByteArray(10)
        for (i in ping.indices) {
            pong[ping.size - 1 - i] = i.toByte()
        }
        return pong
    }

    private fun getPing(): ByteArray {
        val ping = ByteArray(10)
        for (i in ping.indices) {
            ping[i] = i.toByte()
        }
        return ping
    }

    private fun isPing(ping: ByteArray): Boolean {
        for (i in ping.indices) {
            if (ping[i] != i.toByte()) {
                return false
            }
        }
        return true
    }

    private fun isPong(ping: ByteArray?, pong: ByteArray?): Boolean {
        if (ping == null || pong == null) {
            return false
        }
        if (ping.size != pong.size) {
            return false
        }
        for (i in ping.indices) {
            if (ping[i] != pong[ping.size - 1 - i]) {
                return false
            }
        }
        return true
    }

    companion object {
        const val TAG = "PingPong"
    }

}