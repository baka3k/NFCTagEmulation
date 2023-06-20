package hi.baka3k.nfcemulator.command

import android.util.Log
import hi.baka3k.nfctool.command.PingPong
import hi.baka3k.nfctool.nfcreader.NFCTagReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays

class FelicaPingPong : PingPong() {
    override fun execute(nfcTagReader: NFCTagReader, onReponse: (ByteArray?) -> Unit) {
        while (nfcTagReader.isConnected()) {
            var time = System.currentTimeMillis()
            //System code System 1-> 0xFE00
//            val targetSystemCode = byteArrayOf(0xfe.toByte(), 0x00.toByte())
            // 02FE test
            val targetSystemCode = byteArrayOf(0x02.toByte(), 0xFE.toByte())
            // Tạo command polling
            val polling = polling(targetSystemCode)
            val pollingRes = nfcTagReader.transceive(polling)
            Log.d(TAG, "#execute() pollingRes $pollingRes")
            onReponse(pollingRes)
            if (pollingRes != null) {
                // get IDm of System 0 (Byte 1 is data size, byte 2 is response code, size of IDm is 8 byte)
                val targetIDm = Arrays.copyOfRange(pollingRes, 2, 10)
                // Size of data in service (in this case is 4)
                val size = 4
                // Service code of object  -> 0x1A8B
                val targetServiceCode = byteArrayOf(0x1A.toByte(), 0x8B.toByte())
                // create command Read Without Encryption
                val req = readWithoutEncryption(targetIDm, size, targetServiceCode)
                // send command & get response
                val res = nfcTagReader.transceive(req)
                Log.d(TAG, "#execute()  req $req res $res")
                onReponse(res)
            }

        }
    }

    /**
     * Command Polling
     * @param systemCode byte[]
     * @return Polling
     * @throws IOException
     */
    private fun polling(systemCode: ByteArray): ByteArray? {
        val bout = ByteArrayOutputStream(100)
        bout.write(0x00) // Dummy  byte data
        bout.write(0x00) // Command code
        bout.write(systemCode[0].toInt()) // systemCode
        bout.write(systemCode[1].toInt()) // systemCode
        bout.write(0x01) // Request code
        bout.write(0x0f) // Timeslot
        val msg = bout.toByteArray()
        msg[0] = msg.size.toByte() // 1 byte ở đầu là size of data
        return msg
    }

    /**
     * get command Read Without Encryption
     * @param IDm ID of system by
     * @param size data length to get
     * @return Read Without Encryption
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun readWithoutEncryption(
        idm: ByteArray,
        size: Int,
        serviceCode: ByteArray
    ): ByteArray? {
        val bout = ByteArrayOutputStream(100)
        bout.write(0) //data length byte dummy
        bout.write(0x06) // command code
        bout.write(idm) // IDm 8byte
        bout.write(1) // Service number length (the following 2 bytes are repeated for this number)

        bout.write(serviceCode[1].toInt()) // Service code low byte
        bout.write(serviceCode[0].toInt()) // Service code high byte
        bout.write(size) // block

        // Chỉ định Block number
        for (i in 0 until size) {
            bout.write(0x80) // Block element high byte
            bout.write(i) // Block number
        }
        val msg = bout.toByteArray()
        msg[0] = msg.size.toByte() // 1 byte data length
        return msg
    }

    /**
     * parse data Read Without Encryption
     * @param res byte[]
     * Show string @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun parse(res: ByteArray): Array<ByteArray>? {
        // res[10] error code.  0x00 : SUCCESS
        if (res[10].toInt() != 0x00) throw RuntimeException("Read Without Encryption Command Error")

        // res[12]  block response
        // res[13 + n * 16]  (byte/block) data
        val size = res[12].toInt()
        val data = Array(size) {
            ByteArray(
                16
            )
        }
        val str = ""
        for (i in 0 until size) {
            val tmp = ByteArray(16)
            val offset = 13 + i * 16
            for (j in 0..15) {
                tmp[j] = res[offset + j]
            }
            data[i] = tmp
        }
        return data
    }

    companion object {
        const val TAG = "FelicaPingPong"
    }

}