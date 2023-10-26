package com.fazpass.android_trusted_device_v2

import android.content.res.AssetManager
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import com.fazpass.android_trusted_device_v2.`object`.Coordinate
import com.fazpass.android_trusted_device_v2.`object`.DeviceInfo
import com.fazpass.android_trusted_device_v2.`object`.MetaData
import com.fazpass.android_trusted_device_v2.`object`.MetaDataSerializer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class EncryptionTest {
    
    companion object {

        private val metaData = MetaData(
            platform = "android",
            isRooted = false,
            isEmulator = false,
            isVpn = false,
            isCloned = false,
            isScreenMirroring = false,
            isDebuggable = false,
            signatures = listOf("signature="),
            deviceInfo = DeviceInfo("", "", "", ""),
            simNumbers = listOf("081234567890"),
            simOperators = listOf("Named Operator"),
            coordinate = Coordinate(0.0, 0.0),
            isMockLocation = false,
            packageName = "com.fazpass.package_name",
            ipAddress = "127.0.0.1",
            fcmToken = "fcm-token"
        )

        private val jsonMetaData: String
            get() = MetaDataSerializer(metaData).result

    }

    private var assetManager: AssetManager? = null

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assetManager = appContext.assets
    }

    @After
    fun tearDown() {
        assetManager = null
    }

    @Test
    fun encryptAndDecryptMetaTest() {
        val encryptedMetaData = encryptMetaData(jsonMetaData)
        val decryptedMetaData = decryptMetaData(encryptedMetaData)

        assertEquals(jsonMetaData, decryptedMetaData)

        println(decryptedMetaData)
    }

    private fun encryptMetaData(jsonMetaData: String): String {
        var key = String(assetManager!!.open("public_2.pub").readBytes())
        key = key.replace("-----BEGIN RSA PUBLIC KEY-----", "")
            .replace("-----END RSA PUBLIC KEY-----", "")
            .replace("\n", "").replace("\r", "")
        var publicKey: PublicKey? = null
        try {
            val keySpec = X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT))
            val keyFactory = KeyFactory.getInstance("RSA")
            publicKey = keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Encrypt string JSON with public key
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(jsonMetaData.toByteArray(StandardCharsets.UTF_8))

        // Encode to base64 string then return
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    private fun decryptMetaData(encryptedMetaData: String): String {
        var key = String(assetManager!!.open("private_2.key").readBytes())
        key = key.replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\n", "").replace("\r", "")
        var privateKey: PrivateKey? = null
        try {
            val keySpec = PKCS8EncodedKeySpec(Base64.decode(key, Base64.DEFAULT))
            val keyFactory = KeyFactory.getInstance("RSA")
            privateKey = keyFactory.generatePrivate(keySpec)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val decryptedMetaData = Base64.decode(encryptedMetaData, Base64.DEFAULT)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(decryptedMetaData)

        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}