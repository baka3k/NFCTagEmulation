package hi.baka3k.nfctool.data

import java.io.IOException
import java.io.ObjectInputStream

/**
 * ISO/IEC 7816-4 Organization
 * https://en.wikipedia.org/wiki/Smart_card_application_protocol_data_unit
 * */
class CommandAPDU {
    private lateinit var apdu: ByteArray

    @Transient
    private var dataOffset: Int = 0

    @Transient
    private var nc = 0

    // value of ne
    @Transient
    private var ne = 0

    constructor(cla: Int, ins: Int, p1: Int, p2: Int, data: ByteArray) : this(
        cla = cla,
        ins = ins,
        p1 = p1,
        p2 = p2,
        data = data,
        dataOffset = 0,
        dataLength = data.size,
        ne = 0
    )

    /**
     * Constructs a CommandAPDU from a byte array containing the complete
     * APDU contents (header and body).
     *
     *
     * Note that the apdu bytes are copied to protect against
     * subsequent modification.
     *
     * @param apdu the complete command APDU
     *
     * @throws NullPointerException if apdu is null
     * @throws IllegalArgumentException if apdu does not contain a valid
     * command APDU
     */
    constructor(apdu: ByteArray) {
        this.apdu = apdu.clone()
        parse()
    }

    /**
     * Constructs a CommandAPDU from a byte array containing the complete
     * APDU contents (header and body). The APDU starts at the index
     * `apduOffset` in the byte array and is `apduLength`
     * bytes long.
     *
     *
     * Note that the apdu bytes are copied to protect against
     * subsequent modification.
     *
     * @param apdu the complete command APDU
     * @param apduOffset the offset in the byte array at which the apdu
     * data begins
     * @param apduLength the length of the APDU
     *
     * @throws NullPointerException if apdu is null
     * @throws IllegalArgumentException if apduOffset or apduLength are
     * negative or if apduOffset + apduLength are greater than apdu.length,
     * or if the specified bytes are not a valid APDU
     */
    constructor(apdu: ByteArray, apduOffset: Int, apduLength: Int) {
        checkArrayBounds(apdu, apduOffset, apduLength)
        this.apdu = ByteArray(apduLength)
        copyArray(apdu, apduOffset, this.apdu, 0, apduLength)
        parse()
    }

    /**
     * Constructs a CommandAPDU from the four header bytes, command data,
     * and expected response data length. This is case 4 in ISO 7816,
     * command data and Le present. The value Nc is taken as
     * <code>dataLength</code>.
     * If Ne or Nc
     * are zero, the APDU is encoded as case 1, 2, or 3 per ISO 7816.
     *
     * <p>Note that the data bytes are copied to protect against
     * subsequent modification.
     *
     * @param cla the class byte CLA
     * @param ins the instruction byte INS
     * @param p1 the parameter byte P1
     * @param p2 the parameter byte P2
     * @param data the byte array containing the data bytes of the command body
     * @param dataOffset the offset in the byte array at which the data
     *   bytes of the command body begin
     * @param dataLength the number of the data bytes in the command body
     * @param ne the maximum number of expected data bytes in a response APDU
     *
     * @throws NullPointerException if data is null and dataLength is not 0
     * @throws IllegalArgumentException if dataOffset or dataLength are
     *   negative or if dataOffset + dataLength are greater than data.length,
     *   or if ne is negative or greater than 65536,
     *   or if dataLength is greater than 65535
     */
    constructor(
        cla: Int,
        ins: Int,
        p1: Int,
        p2: Int,
        data: ByteArray,
        dataOffset: Int,
        dataLength: Int,
        ne: Int
    ) {
        checkArrayBounds(data, dataOffset, dataLength)
        checkdataLength(ne, dataLength)
        this.ne = ne
        this.nc = dataLength
        if (dataLength == 0) {
            if (ne == 0) {
                apdu = ByteArray(4)
                setHeader(cla = cla, ins = ins, p1 = p1, p2 = p2)
            } else {
                // case 2s or 2e
                if (ne <= 256) {
                    initApu2Sor2E(ne, cla, ins, p1, p2)
                } else {
                    // case 2e
                    initApu2E(ne, cla, ins, p1, p2)
                }
            }
        } else {
            if (ne == 0) {
                // case 3s or 3e
                if (dataLength <= 255) {
                    // case 3s
                    initApu3s(dataLength, cla, ins, p1, p2, data, dataOffset)
                } else {
                    // case 3e
                    initApu3E(dataLength, cla, ins, p1, p2, data, dataOffset)
                }
            } else {
                // case 4s or 4e
                if ((dataLength <= 255) && (ne <= 256)) {
                    // case 4s
                    initApu4s(dataLength, cla, ins, p1, p2, data, dataOffset, ne)
                } else {
                    // case 4e
                    apdu = ByteArray(4 + 5 + dataLength)
                    setHeader(cla = cla, ins = ins, p1 = p1, p2 = p2)
                    apdu[4] = 0
                    apdu[5] = (dataLength shr 8).toByte()
                    apdu[6] = dataLength.toByte()
                    this.dataOffset = 7
                    copyArray(data, dataOffset, apdu, 7, dataLength)
                    if (ne != 65536) {
                        val leOfs = apdu.size - 2
                        apdu[leOfs] = (ne shr 8).toByte()
                        apdu[leOfs + 1] = ne.toByte()
                    } else {
                        //else le == 65536: no need to fill in, encoded as 0
                    }
                }
            }

        }
    }

    /**
     * Returns the value of the instruction byte INS.
     *
     * @return the value of the instruction byte INS.
     */
    fun getCLA(): Int {
        return apdu[0].toInt() and 0xff
    }

    /**
     * Returns the value of the instruction byte INS.
     *
     * @return the value of the instruction byte INS.
     */
    fun getINS(): Int {
        return apdu[1].toInt() and 0xff
    }

    /**
     * Returns the value of the parameter byte P1.
     *
     * @return the value of the parameter byte P1.
     */
    fun getP1(): Int {
        return apdu[2].toInt() and 0xff
    }

    /**
     * Returns the value of the parameter byte P2.
     *
     * @return the value of the parameter byte P2.
     */
    fun getP2(): Int {
        return apdu[3].toInt() and 0xff
    }

    /**
     * Returns the number of data bytes in the command body (Nc) or 0 if this
     * APDU has no body. This call is equivalent to
     * `getData().length`.
     *
     * @return the number of data bytes in the command body or 0 if this APDU
     * has no body.
     */
    fun getNc(): Int {
        return nc
    }

    /**
     * Returns the maximum number of expected data bytes in a response
     * APDU (Ne).
     *
     * @return the maximum number of expected data bytes in a response APDU.
     */
    fun getNe(): Int {
        return ne
    }

    /**
     * Returns a copy of the bytes in this APDU.
     *
     * @return a copy of the bytes in this APDU.
     */
    fun getBytes(): ByteArray {
        return apdu.clone()
    }

    /**
     * Returns a string representation of this command APDU.
     *
     * @return a String representation of this command APDU.
     */
    override fun toString(): String {
        return "CommmandAPDU: " + apdu.size + " bytes, nc=" + nc + ", ne=" + ne
    }

    /**
     * Returns a copy of the data bytes in the command body. If this APDU as
     * no body, this method returns a byte array with length zero.
     *
     * @return a copy of the data bytes in the command body or the empty
     *    byte array if this APDU has no body.
     */
    fun getData(): ByteArray {
        val data = ByteArray(nc)
        copyArray(apdu, dataOffset, data, 0, nc)
        return data
    }


    private fun initApu4s(
        dataLength: Int,
        cla: Int,
        ins: Int,
        p1: Int,
        p2: Int,
        data: ByteArray,
        dataOffset: Int,
        ne: Int
    ) {
        apdu = ByteArray(4 + 2 + dataLength)
        setHeader(cla = cla, ins = ins, p1 = p1, p2 = p2)
        apdu[4] = dataLength.toByte()
        this.dataOffset = 5
        copyArray(data, dataOffset, apdu, 5, dataLength)
        apdu[apdu.size - 1] = if (ne != 256) ne.toByte() else 0
    }

    private fun initApu3E(
        dataLength: Int,
        cla: Int,
        ins: Int,
        p1: Int,
        p2: Int,
        data: ByteArray,
        dataOffset: Int
    ) {
        apdu = ByteArray(4 + 3 + dataLength)
        setHeader(cla = cla, ins = ins, p1 = p1, p2 = p2)
        apdu[4] = 0
        apdu[5] = (dataLength shr 8).toByte()
        apdu[6] = dataLength.toByte()
        this.dataOffset = 7
        copyArray(data, dataOffset, apdu, 7, dataLength)
    }

    private fun initApu3s(
        dataLength: Int,
        cla: Int,
        ins: Int,
        p1: Int,
        p2: Int,
        data: ByteArray,
        dataOffset: Int
    ) {
        apdu = ByteArray(4 + 1 + dataLength)
        setHeader(cla = cla, ins = ins, p1 = p1, p2 = p2)
        apdu[4] = dataLength.toByte()
        this.dataOffset = 5
        copyArray(data, dataOffset, apdu, 5, dataLength)
    }

    private fun copyArray(
        src: ByteArray,
        srcPos: Int,
        dest: ByteArray,
        destPos: Int,
        length: Int
    ) {
        System.arraycopy(src, srcPos, dest, destPos, length)
    }

    private fun initApu2E(
        ne: Int,
        cla: Int,
        ins: Int,
        p1: Int,
        p2: Int
    ) {
        var l1: Byte
        var l2: Byte
        if (ne == 65536) {
            l1 = 0
            l2 = 0
        } else {
            l1 = (ne shr 8).toByte()
            l2 = ne.toByte()
        }
        apdu = ByteArray(7)
        setHeader(cla = cla, ins = ins, p1 = p1, p2 = p2)
        apdu[5] = l1
        apdu[6] = l2
    }

    private fun initApu2Sor2E(
        ne: Int,
        cla: Int,
        ins: Int,
        p1: Int,
        p2: Int
    ) {
        val len = if (ne != 256) {
            ne.toByte()
        } else {
            0
        }
        apdu = ByteArray(5)
        setHeader(cla = cla, ins = ins, p1 = p1, p2 = p2)
        apdu[4] = len
    }

    private fun setHeader(cla: Int, ins: Int, p1: Int, p2: Int) {
        apdu[0] = cla.toByte()
        apdu[1] = ins.toByte()
        apdu[2] = p1.toByte()
        apdu[3] = p2.toByte()
    }

    private fun checkdataLength(ne: Int, dataLength: Int) {
        if (dataLength > 65535) {
            throw IllegalArgumentException("dataLength is too large")
        }
        if (ne < 0) {
            throw IllegalArgumentException("ne must not be negative")
        }
        if (ne > 65536) {
            throw IllegalArgumentException("ne is too large");
        }
    }

    private fun checkArrayBounds(data: ByteArray?, ofs: Int, len: Int) {
        require(!(ofs < 0 || len < 0)) { "Offset and length must not be negative" }
        if (data == null) {
            require(!(ofs != 0 && len != 0)) { "offset and length must be 0 if array is null" }
        } else {
            require(ofs <= data.size - len) { "Offset plus length exceed array size" }
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(objectInputStream: ObjectInputStream) {
        apdu = objectInputStream.readUnshared() as ByteArray
        // initialize transient fields
        parse()
    }

    /**
     * Command APDU encoding options:
     *
     * case 1:  |CLA|INS|P1 |P2 |                                 len = 4
     * case 2s: |CLA|INS|P1 |P2 |LE |                             len = 5
     * case 3s: |CLA|INS|P1 |P2 |LC |...BODY...|                  len = 6..260
     * case 4s: |CLA|INS|P1 |P2 |LC |...BODY...|LE |              len = 7..261
     * case 2e: |CLA|INS|P1 |P2 |00 |LE1|LE2|                     len = 7
     * case 3e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|          len = 8..65542
     * case 4e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|LE1|LE2|  len =10..65544
     *
     * LE, LE1, LE2 may be 0x00.
     * LC must not be 0x00 and LC1|LC2 must not be 0x00|0x00
     */
    private fun parse() {
        if (apdu.size < 4) {
            throw IllegalArgumentException("apdu must be at least 4 bytes long");
        }
        if (apdu.size == 4) {
            // case 1
            return
        }
        val l1: Int = apdu[4].toInt() and 0xff
        if (apdu.size == 5) {
            // case 2s
            ne = if (l1 == 0) 256 else l1
            return
        }
        if (l1 != 0) {
            if (apdu.size == 4 + 1 + l1) {
                // case 3s
                this.nc = l1
                this.dataOffset = 5
                return
            } else if (apdu.size == 4 + 2 + l1) {
                // case 4s
                nc = l1
                this.dataOffset = 5
                val l2 = apdu[apdu.size - 1].toInt() and 0xff
                ne = if (l2 == 0) 256 else l2
                return
            } else {
                throw IllegalArgumentException("Invalid APDU: length=${apdu.size}, b1=$l1")
            }
        }
        if (apdu.size < 7) {
            throw IllegalArgumentException("Invalid APDU: length=${apdu.size}, b1=$l1")
        }
        val l2 = apdu[5].toInt() and 0xff shl 8 or (apdu[6].toInt() and 0xff)
        if (apdu.size == 7) {
            // case 2e
            ne = if (l2 == 0) 65536 else l2
            return
        }
        if (l2 == 0) {
            throw IllegalArgumentException("Invalid APDU: length=${apdu.size}, b1=$l1, b2||b3=$l2")
        }
        if (apdu.size == 4 + 3 + l2) {
            // case 3e
            nc = l2
            dataOffset = 7
            return
        } else if (apdu.size == 4 + 5 + l2) {
            // case 4e
            nc = l2
            dataOffset = 7
            val leOfs: Int = apdu.size - 2
            val l3: Int = apdu[leOfs].toInt() and 0xff shl 8 or (apdu[leOfs + 1].toInt() and 0xff)
            ne = if (l3 == 0) 65536 else l3
        } else {
            throw IllegalArgumentException("Invalid APDU: length=${apdu.size}, b1=$l1, b2||b3=$l2")
        }
    }

    /**
     * Compares the specified object with this command APDU for equality.
     * Returns true if the given object is also a CommandAPDU and its bytes are
     * identical to the bytes in this CommandAPDU.
     *
     * @param obj the object to be compared for equality with this command APDU
     * @return true if the specified object is equal to this command APDU
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is CommandAPDU) {
            return false
        }
        return apdu.contentEquals(other.apdu)
    }

    /**
     * Returns the hash code value for this command APDU.
     *
     * @return the hash code value for this command APDU.
     */
    override fun hashCode(): Int {
        return apdu.contentHashCode()
    }

    companion object {
        private const val MAX_APDU_SIZE = 65544
    }
}
