package hi.baka3k.nfcemulator.secure

import android.util.Base64
import java.io.File
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

object ECKeyUtils {
    fun publicKeyToPem(publicKey: PublicKey): String {
        val base64PubKey = Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
        return "-----BEGIN PUBLIC KEY-----\n" + base64PubKey.replace("(.{64})".toRegex(), "$1\n") +
                "\n-----END PUBLIC KEY-----\n"
    }

    fun publicKeyFromFile(filePublicKey: File): PublicKey? {
        val key = filePublicKey.readText(Charset.defaultCharset())
        val publicKeyPEM: String = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PUBLIC KEY-----", "")
        val encoded: ByteArray = Base64.decode(publicKeyPEM, Base64.DEFAULT)
        val keyFactory = KeyFactory.getInstance("EC")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec)
    }

    fun privateKeyToPem(privateKey: PrivateKey): String {
        val base64PubKey = Base64.encodeToString(privateKey.encoded, Base64.DEFAULT)
        return "-----BEGIN PRIVATE KEY-----\n" +
                base64PubKey.replace("(.{64})".toRegex(), "$1\n") +
                "\n-----END PRIVATE KEY-----\n"
    }
}