package com.example.nfctag.nfc.applet

class DigitalKeyApplet(private val appletCommand: AppletCommand) {
    fun createCaCmd(): ByteArray {
        return appletCommand.createCaCmd().getBytes()
    }
}
