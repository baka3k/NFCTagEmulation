package com.example.nfctag.nfc.cmd

import com.example.nfctag.nfc.nfcreader.NFCTagReader
import kotlinx.coroutines.flow.Flow

abstract class PingPong {
    abstract fun execute(
        nfcTagReader: NFCTagReader,
        onReponse: (ByteArray?) -> Unit
    )
}