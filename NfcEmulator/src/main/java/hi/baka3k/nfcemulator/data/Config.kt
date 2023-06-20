package hi.baka3k.nfcemulator.data

object Config {
    const val DIGITAL_KEY_FRAMWORK_AID = "A000000809434343444B417631"
    const val ISO_SELECT_APPLICATION = 0xA4 //
    const val SELECT_APPLICATION = 0x5A //

    /* Card commands we support. */
    const val INS_GET_PUBLIC_KEY = 0x04
    const val INS_AUTHENTICATE = 0x11
    const val INS_GET_CARD_INFO = 0x14

    const val OPERATION_OK = 0x00
    const val STATUS_OK = 0x91
}