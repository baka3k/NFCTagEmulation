package com.example.nfctag.nfc

import java.util.Locale

/**
 * Converts the byte array to HEX string.
 *
 * @param buffer
 * the buffer.
 * @return the HEX string.
 */
fun ByteArray.toHexString(): String {
    return toHexString(0, this.size)
}

/**
 * Converts the byte array to HEX string.
 *
 * @param buffer
 * the buffer.
 * @return the HEX string.
 */
fun ByteArray.toHexString(offset: Int, length: Int): String {
    val sb = StringBuilder()
    for (i in offset until (offset + length)) {
        val b = this[i]
        sb.append(String.format("%02x", b.toInt() and 0xff))
    }
    return sb.toString().uppercase(Locale.getDefault())
}