package com.fazpass.android_trusted_device_v2

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


internal class SecureUtil {

    companion object {

        private const val SECURE_KEY_ALIAS = "FazpassSecureKey"

        private const val KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7

        fun generateKey() {
            // load AndroidKeyStore and delete existing key if there is any
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(SECURE_KEY_ALIAS)

            val spec = KeyGenParameterSpec.Builder(
                SECURE_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT
                        or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(BLOCK_MODE)
                .setEncryptionPaddings(ENCRYPTION_PADDING)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setKeySize(256)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true)
                .build()

            val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM, "AndroidKeyStore")
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }

        fun getEncryptCipher() : Cipher? {
            return try {
                val cipher = getCipher()
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
                cipher
            } catch (e: KeyPermanentlyInvalidatedException) {
                null
            }
        }

        fun getDecryptCipher(iv: IvParameterSpec) : Cipher? {
            return try {
                val cipher = getCipher()
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), iv)
                cipher
            } catch (e: KeyPermanentlyInvalidatedException) {
                null
            }
        }

        private fun getSecretKey() : SecretKey? {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            return try {
                keyStore.getKey(SECURE_KEY_ALIAS, null) as SecretKey
            } catch (e : Exception) {
                null
            }
        }

        private fun getCipher(): Cipher {
            return Cipher.getInstance(
                "$KEY_ALGORITHM/$BLOCK_MODE/$ENCRYPTION_PADDING")
        }
    }
}