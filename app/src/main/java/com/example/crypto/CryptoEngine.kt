package com.example.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedResult(
    val ciphertext: String,
    val iv: String,
    val salt: String,
    val algorithm: String
)

object CryptoEngine {
    private val secureRandom = SecureRandom()
    private const val MASTER_SEED = "Keepseek_Vault_Secure_Master_Seed_2026_Key"

    fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        secureRandom.nextBytes(bytes)
        return bytes
    }

    private fun deriveKey(salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(MASTER_SEED.toCharArray(), salt, iterations, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plaintext: String, settings: EncryptionSettings): EncryptedResult {
        val salt = generateRandomBytes(settings.saltSize.bytes)
        val key = deriveKey(salt, settings.pbkdfRounds.rounds)
        val iv = generateRandomBytes(settings.algorithm.ivLength)

        val cipher = Cipher.getInstance(settings.algorithm.transformation)
        when (settings.algorithm) {
            CipherAlgorithm.AES_GCM -> {
                val gcmSpec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
            }
            CipherAlgorithm.AES_CBC -> {
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            }
        }

        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return EncryptedResult(
            ciphertext = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            algorithm = settings.algorithm.id
        )
    }

    fun decrypt(
        ciphertextBase64: String,
        ivBase64: String,
        saltBase64: String,
        algorithmId: String,
        iterations: Int = 100_000
    ): String {
        return try {
            val ciphertext = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val salt = Base64.decode(saltBase64, Base64.NO_WRAP)

            val algo = if (algorithmId == CipherAlgorithm.AES_CBC.id) {
                CipherAlgorithm.AES_CBC
            } else {
                CipherAlgorithm.AES_GCM
            }

            val key = deriveKey(salt, iterations)
            val cipher = Cipher.getInstance(algo.transformation)
            when (algo) {
                CipherAlgorithm.AES_GCM -> {
                    val gcmSpec = GCMParameterSpec(128, iv)
                    cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
                }
                CipherAlgorithm.AES_CBC -> {
                    val ivSpec = IvParameterSpec(iv)
                    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
                }
            }
            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "••••••••••••"
        }
    }

    fun verifyIntegrity(): Boolean {
        return try {
            val testPlain = "Keepseek_Cryptographic_Self_Test_Passed_2026"
            val settings = EncryptionSettings()
            val enc = encrypt(testPlain, settings)
            val dec = decrypt(enc.ciphertext, enc.iv, enc.salt, enc.algorithm, settings.pbkdfRounds.rounds)
            dec == testPlain
        } catch (e: Exception) {
            false
        }
    }
}
