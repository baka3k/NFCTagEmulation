package com.example.nfctag.nfc.nfcreader

import android.nfc.Tag
import android.nfc.tech.NfcV
import com.example.nfctag.nfc.config.ConfigBuilder

class NfcVReader(tag: Tag) : NFCTagReader(NfcV.get(tag)) {
    override fun getConfig(): ConfigBuilder {
        val builder = ConfigBuilder()
        // TODO: V tags cannot be emulated (yet)
        return builder
    }
}