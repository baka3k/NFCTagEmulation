package hi.baka3k.nfcemulator.secure

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.SignatureException
import java.security.spec.ECGenParameterSpec
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * CCC-TS-101-Digital-Key-R3_1.0.0.pdf
 * A.4.6. X.509 ECDSA Signature
 * */
object KeyECDSA {
    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    private fun getInstanceKeyPairECDSA(): KeyPair {
        val keyStore = KeyStore.getInstance(PROVIDER)
        keyStore.load(null)
        var privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey?
        var publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey
        if (privateKey == null || publicKey == null) {
            initKeyPairECDSA()
        }
        privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey
        publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey
        return KeyPair(publicKey, privateKey)
    }

    private fun initKeyPairECDSA() {
        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, PROVIDER
        )
        val keyParams = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
            .setAlgorithmParameterSpec(ECGenParameterSpec(STD_ECGEN))
            .setDigests(
                KeyProperties.DIGEST_SHA256,
                KeyProperties.DIGEST_SHA384,
                KeyProperties.DIGEST_SHA512
            )
            .setUserAuthenticationRequired(false) // for test
            .build()
        keyPairGenerator.initialize(keyParams, SecureRandom.getInstance("SHA1PRNG"))
        keyPairGenerator.generateKeyPair()
    }

    fun createSignature(): Signature {
        return Signature.getInstance(SIGNATURE_ALGORITHM)
    }

    fun sign(data: ByteArray): ByteArray {
        return try {
            val signatureSign = createSignature()
            signatureSign.initSign(keyPair.private)
            signatureSign.update(data)
            signatureSign.sign() // mobile
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "#getAnSignKey() NoSuchAlgorithmException ${e.message}", e)
            data
        } catch (e: SignatureException) {
            Log.e(TAG, "#getAnSignKey() SignatureException ${e.message}", e)
            data
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "#getAnSignKey() InvalidKeyException ${e.message}", e)
            data
        }
    }

    fun verifySign(signedData: ByteArray, originalData: ByteArray?): Boolean {
        return verifySign(signedData, originalData, keyPair.public)
    }

    fun verifySign(signedData: ByteArray, originalData: ByteArray?, pulickey: PublicKey): Boolean {
        return try {
            val signatureVerity = createSignature()
            signatureVerity.initVerify(pulickey)
            if (originalData == null) {
                signatureVerity.update(seed)
            } else {
                signatureVerity.update(originalData)
            }
            signatureVerity.verify(signedData)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "#verifySign() NoSuchAlgorithmException ${e.message}", e)
            false
        } catch (e: SignatureException) {
            Log.e(TAG, "#verifySign() SignatureException ${e.message}", e)
            false
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "#verifySign() InvalidKeyException ${e.message}", e)
            false
        }
    }
    /**
     * * Generate Shared Secret
     * * vehicle & applet must share PublicKey with each other
     * * then used(private key, public key) to gen Shared SecretKey
     * * (privateKeyVehicle, publicKeyApplet) == (privateKeyApplet, publickeyVerhicle)
     * */
    fun generateSharedSecret(
        privateKey: PrivateKey,
        publicKey: PublicKey
    ): SecretKey? {
        return try {
            val keyAgreement = KeyAgreement.getInstance("ECDH")
            keyAgreement.init(privateKey)
            keyAgreement.doPhase(publicKey, true)
            keyAgreement.generateSecret("AES")
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "#generateSharedSecret() InvalidKeyException ${e.message}", e)
            null
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "#generateSharedSecret() NoSuchAlgorithmException ${e.message}", e)
            null
        } catch (e: NoSuchProviderException) {
            Log.e(TAG, "#generateSharedSecret() NoSuchProviderException ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * create secret key from password
     * */
    fun createSecretKey(pass: String): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(pass.toCharArray(), iv, 65536, 256)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    /**
     * init new keypair EC
     * */
    public fun initKeyPairECDSA(stdName: String): KeyPair {
        val keyPairGenerator: KeyPairGenerator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        keyPairGenerator.initialize(256)
        return keyPairGenerator.generateKeyPair()
    }

    private val seed = byteArrayOf(22, 33, 44, 55, 66, 77, 88, 99)
    private val iv =
        byteArrayOf(11, 22, 33, 44, 55, 66, 88, 88, 99, 11, 11, 11, 11, 11, 11, 11) // 16 bit
    private const val TAG = "KEY_EC"
    private const val KEY_ALIAS = "keyalias_ecdsa"
    private const val PROVIDER = "AndroidKeyStore"
    private const val STD_ECGEN = "secp256r1"

    //private const val STD_ECGEN = "secp112r1"
    //private const val STD_ECGEN = "nistp192"
    //private const val STD_ECGEN = "nistp224"
    //private const val STD_ECGEN = "nistp384"
    //private const val STD_ECGEN = "secp256r1"
    //private const val STD_ECGEN = "nistp521"
    //private const val STD_ECGEN = "nistp256 / secp256r1"
    //private const val STD_ECGEN = " nistp384 / secp384r1"
    private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"

    val keyPair = getInstanceKeyPairECDSA()
}