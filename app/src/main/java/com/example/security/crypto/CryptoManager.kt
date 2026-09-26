package com.example.security.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages hardware-backed encryption using the Android Keystore system.
 * Prevents plain-text storage of pairing secrets and command credentials.
 * Automatically provides fallback in JVM testing environments where AndroidKeyStore provider is absent.
 */
class CryptoManager {

    private var keyStore: KeyStore? = null
    private var fallbackSecretKey: SecretKey? = null

    init {
        try {
            val ks = KeyStore.getInstance(ANDROID_KEY_STORE)
            ks.load(null)
            keyStore = ks
            ensureMasterKey()
        } catch (e: Throwable) {
            // AndroidKeyStore is not present in pure JVM / Robolectric host environment.
            // Generate standard AES key for test runner.
            try {
                val keyGen = KeyGenerator.getInstance("AES")
                keyGen.init(256)
                fallbackSecretKey = keyGen.generateKey()
            } catch (ex: Exception) {
                fallbackSecretKey = SecretKeySpec(ByteArray(32) { 0x42 }, "AES")
            }
        }
    }

    private fun ensureMasterKey() {
        val ks = keyStore ?: return
        if (!ks.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE
            )
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return keyStore?.let { ks ->
            (ks.getKey(KEY_ALIAS, null) as? SecretKey)
        } ?: fallbackSecretKey ?: SecretKeySpec(ByteArray(32) { 0x42 }, "AES")
    }

    /**
     * Encrypts plaintext string using AES-256-GCM.
     * Returns Base64-encoded payload (IV + ciphertext).
     */
    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Prepend IV (12 bytes for GCM)
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts Base64-encoded AES-256-GCM ciphertext.
     */
    fun decrypt(encryptedBase64: String): String? {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < GCM_IV_LENGTH) return null

            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Signs command message using HMAC-SHA256 with pairing secret key.
     */
    fun signCommand(payload: String, secretKeyString: String): String = signMessage(payload, secretKeyString)

    fun signMessage(payload: String, secretKeyString: String): String {
        return try {
            val mac = Mac.getInstance(HMAC_ALGO)
            val keySpec = SecretKeySpec(secretKeyString.toByteArray(Charsets.UTF_8), HMAC_ALGO)
            mac.init(keySpec)
            val rawHmac = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(rawHmac, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Verifies HMAC signature for incoming messages.
     */
    fun verifySignature(payload: String, signature: String, secretKeyString: String): Boolean {
        val calculated = signMessage(payload, secretKeyString)
        return calculated.isNotEmpty() && calculated == signature
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "tetherguard_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val HMAC_ALGO = "HmacSHA256"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }
}
