package hi.baka3k.nfcemulator.command

import android.nfc.tech.IsoDep
import android.util.Log
import java.io.IOException

object NFCPingPong {
    @Throws(IOException::class)
    fun playPingPong(isoDep: IsoDep) {
        val ping = getPing()
        var count = 0
        var elapsed: Long = 0
        var max = Long.MIN_VALUE
        var min = Long.MAX_VALUE
        while (isoDep.isConnected) {
            var time = System.currentTimeMillis()
            val pong: ByteArray = isoDep.transceive(ping)
            time = System.currentTimeMillis() - time
            if (!isPong(ping, pong)) {
                Log.d(TAG, "No pong to the ping")
                break
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

    fun getPong(): ByteArray {
        val ping: ByteArray = getPing()
        val pong = ByteArray(10)
        for (i in ping.indices) {
            pong[ping.size - 1 - i] = i.toByte()
        }
        return pong
    }

    fun getPing(): ByteArray {
        val ping = ByteArray(10)
        for (i in ping.indices) {
            ping[i] = i.toByte()
        }
        return ping
    }

    fun isPing(ping: ByteArray): Boolean {
        for (i in ping.indices) {
            if (ping[i] != i.toByte()) {
                return false
            }
        }
        return true
    }

    private fun isPong(ping: ByteArray, pong: ByteArray): Boolean {
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

    const val TAG = "PingPong"
}