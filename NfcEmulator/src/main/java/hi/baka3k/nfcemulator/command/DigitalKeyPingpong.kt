package hi.baka3k.nfcemulator.command

import android.util.Log
import hi.baka3k.nfcemulator.data.Config
import hi.baka3k.nfcemulator.data.Config.INS_GET_PUBLIC_KEY
import hi.baka3k.nfcemulator.data.Config.OPERATION_OK
import hi.baka3k.nfcemulator.data.Config.STATUS_OK
import hi.baka3k.nfcemulator.vehicle.PairingCommand
import hi.baka3k.nfctool.command.PingPong
import hi.baka3k.nfctool.data.CommandAPDU
import hi.baka3k.nfctool.data.ResponseAPDU
import hi.baka3k.nfctool.nfcreader.NFCTagReader
import hi.baka3k.nfctool.utils.hexStringToByteArray

class DigitalKeyPingPong : PingPong() {
    private val vehicleCommand = PairingCommand()
    override fun execute(nfcTagReader: NFCTagReader, onReponse: (ByteArray?) -> Unit) {
        // vehicle send select Apdu
        onReponse("Vehicle send select APDU".toByteArray())
        val selectApduResponse = selectApdu(nfcTagReader)
        if (selectApduResponse != null) {
            val responseAPDU = ResponseAPDU(selectApduResponse)
            val status = responseAPDU.getSW1()
            val command = responseAPDU.getSW2()
            if (status == STATUS_OK && command == OPERATION_OK) {
                onReponse("Applet Response status: STATUS_OK - Start handshake".toByteArray())
                val handShakeSuccess = handShake(nfcTagReader,onReponse)
                if (handShakeSuccess) {
                    onReponse("Vehicle & Applet handShakeSuccess - let Ping pong".toByteArray())
                    pingpong(nfcTagReader, onReponse)
                } else {
                    onReponse("Vehicle & Applet handShake Fail - Stop transaction".toByteArray())
                }
            } else {
                onReponse("Applet select status: STATUS_FAIL".toByteArray())
            }
        }
    }

    private fun pingpong(
        nfcTagReader: NFCTagReader,
        onReponse: (ByteArray?) -> Unit
    ) {
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
                onReponse("No pong to the ping - stopped".toByteArray())
                break //- disable to test message from another mobile
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
     * simulation vehicle send get Public key
     * */
    private fun handShake(nfcTagReader: NFCTagReader, response: (ByteArray?) -> Unit): Boolean {
        // vehicle send request get public key
        response("Vehicle send request get publickey to Applet".toByteArray())
        sendRequestGetPublickey(nfcTagReader)
        return true
    }

    /**
     * simulation vehicle send get Public key
     * */
    private fun sendRequestGetPublickey(nfcTagReader: NFCTagReader) {
        val cmd = CommandAPDU(
            cla = OPERATION_OK,
            ins = INS_GET_PUBLIC_KEY,
            p1 = 0x04,
            p2 = 0x00,
            data = Config.DIGITAL_KEY_FRAMWORK_AID.hexStringToByteArray(), // AID_DIGITAL_KEY
        )
        nfcTagReader.transceive(command = cmd.getBytes())
    }

    private fun selectApdu(
        nfcTagReader: NFCTagReader,
    ): ByteArray? {
        return sendRequestSelectAidApdu(nfcTagReader)
    }

    /**
     * init first command by select AID
     * MUST BE called at the fist connection
     * */
    private fun sendRequestSelectAidApdu(nfcTagReader: NFCTagReader): ByteArray? {
        val command = vehicleCommand.selectAIDCmd()
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