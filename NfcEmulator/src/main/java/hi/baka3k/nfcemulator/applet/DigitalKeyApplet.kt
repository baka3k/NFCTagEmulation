package hi.baka3k.nfcemulator.applet

class DigitalKeyApplet(private val appletCommand: AppletCommand) {
    fun createCaCmd(): ByteArray {
        return appletCommand.createCaCmd().getBytes()
    }

    fun createEnPointCmd(): ByteArray {
        return appletCommand.createEnPointCmd().getBytes()
    }
}
