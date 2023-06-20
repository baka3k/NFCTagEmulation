package hi.baka3k.nfcemulator.applet

import hi.baka3k.nfcemulator.data.Config
import hi.baka3k.nfctool.data.CommandAPDU
import hi.baka3k.nfctool.utils.hexStringToByteArray

class AppletCommand {
    /**
     * This command selects the Digital Key applet instance to be used for the transaction.
     * The instance AID parameter is defined during applet installation (see the INSTALL for INSTALL command in [2]).
     * CCC-TS-101-Digital-Key-R3_1.0.0.pdf
     * 15.3.2.1 SELECT command
     * command: CLA1 A4 04 00 Lc [instance AID] 00 response: [Table 15-11] 90 00
     * The CLA1 is as defined in Table 15-3.
     * */
    fun selectAIDCmd(): CommandAPDU {
        return CommandAPDU(
            cla = 0x00, // just test - refer `CLA1 is as defined in Table 15-3.`
            ins = 0xA4,
            p1 = 0x04,
            p2 = 0x00,
            data = Config.DIGITAL_KEY_FRAMWORK_AID.hexStringToByteArray(), // AID_DIGITAL_KEY
        )
    }

    /**
     * This command creates an onboard signing authority called Instance CA used for endpoint issuance.
     * The generation and provisioning of this entity is out of scope of this specification.
     * The certificate containing the Instance CA public key shall comply with Listing 15-16.
     * The INS value for CREATE CA command shall be 38h.
     * CCC-TS-101-Digital-Key-R3_1.0.0.pdf
     * 15.3.2.2 CREATE CA command
     * command: CLA1 ins 04 00 Lc [instance AID] 00 response: [Table 15-11] 90 00
     * The CLA1 is as defined in Table 15-3.
     * */
    fun createCaCmd(): CommandAPDU {
        return CommandAPDU(
            cla = 0x00, // just test - refer `CLA1 is as defined in Table 15-3.`
            ins = 0x38,
            p1 = 0x04,
            p2 = 0x00,
            data = Config.DIGITAL_KEY_FRAMWORK_AID.hexStringToByteArray(), // AID_DIGITAL_KEY
        )
    }

    /**
     * This command deletes an Instance CA.
     * The method of deletion of this entity is out of scope of this specification.
     * The INS value for DELETE CA command shall be 3Ah
     * CCC-TS-101-Digital-Key-R3_1.0.0.pdf
     * 15.3.2.3 DELETE CA command
     * command: CLA1 ins 04 00 Lc [instance AID] 00 response: [Table 15-11] 90 00
     * The CLA1 is as defined in Table 15-3.
     * */
    fun deleteCaCmd(): CommandAPDU {
        return CommandAPDU(
            cla = 0x00, // just test - refer `CLA1 is as defined in Table 15-3.`
            ins = 0x3A,
            p1 = 0x04,
            p2 = 0x00,
            data = Config.DIGITAL_KEY_FRAMWORK_AID.hexStringToByteArray(), // AID_DIGITAL_KEY
        )
    }

    /**
     * The command creates a communication endpoint and provides the corresponding endpoint certificate.
     * A communication endpoint distributes signatures and mailbox content over a secure channel to authenticated parties only.
     * The endpoint configuration fields described in Table 15-12 may be provided either
     * in the command payload or in the internal buffer using the WRITE BUFFER command described in Section 15.3.2.14.
     * The endpoint creation certificate described in Listing 15-5 is always provided in the internal buffer and
     * is accessible using the READ BUFFER command described in Section 15.3.2.13.
     * CCC-TS-101-Digital-Key-R3_1.0.0.pdf
     * 15.3.2.4 CREATE ENDPOINT command
     * command: CLA2 70 00 00 Lc ([Table 15-12] [Table 15-15] 00) response: [response_length] 9000
     * The CLA2 is as defined in Table 15-3.
     * */
    fun createEnPointCmd(): CommandAPDU {
        return CommandAPDU(
            cla = 0x80, // just test - refer 'The CLA2 is as defined in Table 15-3'. (80,84,80->83, 80-87)
            ins = 0x70,
            p1 = 0x00,
            p2 = 0x00,
            data = "".toByteArray(), // AID_DIGITAL_KEY
        )
    }
}