package com.fazpass.android_trusted_device_v2

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.util.Base64
import android.util.Log
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fazpass.android_trusted_device_v2.`object`.Coordinate
import com.fazpass.android_trusted_device_v2.`object`.MetaData
import com.fazpass.android_trusted_device_v2.`object`.MetaDataSerializer
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.math.min


internal class AndroidTrustedDevice : Fazpass {

    private var isInitialized : Boolean = false
    private lateinit var assetManager : AssetManager
    private lateinit var publicKeyAssetName : String

    private var locationEnabled = false
    private var simNumbersAndOperatorsEnabled = false

    companion object {
        /** If true, every print and log will be recorded to terminal */
        // TODO: Change to false on production!
        const val IS_DEBUG = true
    }

    override fun init(context: Context, keyAssetName: String) {
        assetManager = context.assets

        var assetNotExist = false
        var iStream : InputStream? = null
        try {
            iStream = assetManager.open(keyAssetName)
        } catch (e: IOException) {
            if (IS_DEBUG) e.printStackTrace()
            assetNotExist = true
            assetManager.close()
        } finally {
            iStream?.close()
        }
        if (assetNotExist) throw Exception("Key *.pub file with the name $keyAssetName doesn't exist in the 'assets' directory!")

        this.publicKeyAssetName = keyAssetName
        NotificationUtil(context).initNotificationChannel()
        this.isInitialized = true
    }

    override fun generateMeta(context: Context, callback: (String) -> Unit) {
        if (!isInitialized) throw Exception("Fazpass init has to be called first!")

        val platform = "android"
        val packageName = context.packageName
        val isDebuggable = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

        val isRooted = RootUtil.isDeviceRooted(context)
        val isEmulator = EmulatorUtil.isEmulator
        val isVpn = ConnectionUtil.isVpnConnectionAvailable(context)
        val isCloned = CloningUtil(context).isAppCloned
        val isScreenMirroring = ScreenMirroringUtil(context).isScreenMirroring
        val signatures = AppSignatureUtil.getSignatures(context)
        val deviceInfo = DeviceInfoUtil.deviceInfo

        val simNumbers: List<String>
        val simOperators: List<String>
        if (simNumbersAndOperatorsEnabled) {
            val dataCarrierUtil = DataCarrierUtil(context)
            simNumbers = dataCarrierUtil.simNumbers
            simOperators = dataCarrierUtil.simOperators
        } else {
            simNumbers = listOf()
            simOperators = listOf()
        }

        IPAddressUtil.getIPAddress { ipAddress ->

            if (locationEnabled) {
                val locationUtil = LocationUtil(context)
                locationUtil.getLastKnownLocation { location ->

                    val isMockLocation : Boolean = locationUtil.isMockLocationOn(location)
                    val coordinate = if (location != null) {
                        Coordinate(location.latitude, location.longitude)
                    }
                    else {
                        Coordinate(0.0,0.0)
                    }

                    val metadata = MetaData(
                        platform = platform,
                        isRooted = isRooted,
                        isEmulator = isEmulator,
                        isVpn = isVpn,
                        isCloned = isCloned,
                        isScreenMirroring = isScreenMirroring,
                        isDebuggable = isDebuggable,
                        signatures = signatures,
                        deviceInfo = deviceInfo,
                        simNumbers = simNumbers,
                        simOperators = simOperators,
                        coordinate = coordinate,
                        isMockLocation = isMockLocation,
                        packageName = packageName,
                        ipAddress = ipAddress,
                    )
                    if (IS_DEBUG) printMetaData(metadata)

                    callback(encryptMetaData(metadata))
                }
            } else {
                val metadata = MetaData(
                    platform = platform,
                    isRooted = isRooted,
                    isEmulator = isEmulator,
                    isVpn = isVpn,
                    isCloned = isCloned,
                    isScreenMirroring = isScreenMirroring,
                    isDebuggable = isDebuggable,
                    signatures = signatures,
                    deviceInfo = deviceInfo,
                    simNumbers = simNumbers,
                    simOperators = simOperators,
                    coordinate = Coordinate(0.0,0.0),
                    isMockLocation = false,
                    packageName = packageName,
                    ipAddress = ipAddress,
                )
                if (IS_DEBUG) printMetaData(metadata)

                callback(encryptMetaData(metadata))
            }
        }
    }

    override fun enableSelected(vararg sensitiveData: SensitiveData) {
        sensitiveData.forEach {
            when (it) {
                SensitiveData.location -> locationEnabled = true
                SensitiveData.simNumbersAndOperators -> simNumbersAndOperatorsEnabled = true
            }
        }
    }

    private fun encryptMetaData(metadata: MetaData) : String {
        val objectMapper = ObjectMapper()
        val module = SimpleModule("MetaDataSerializer", Version(1,0,0,null,null,null))
        module.addSerializer(MetaData::class.java, MetaDataSerializer())
        objectMapper.registerModule(module)
        val jsonString: String = objectMapper.writeValueAsString(metadata)
        if (IS_DEBUG) Log.i("META-AS-STRING", jsonString)

        var key = String(assetManager.open(publicKeyAssetName).readBytes())
        key = key.replace("-----BEGIN RSA PUBLIC KEY-----", "")
            .replace("-----END RSA PUBLIC KEY-----", "")
            .replace("\n", "").replace("\r", "")
        var publicKey: PublicKey? = null
        try {
            val keySpec = X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT))
            val keyFactory = KeyFactory.getInstance("RSA")
            publicKey = keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            if (IS_DEBUG) e.printStackTrace()
        }

        // Encrypt string JSON with public key
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(jsonString.toByteArray(StandardCharsets.UTF_8))

        // Encode to base64 string then return
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun printMetaData(metaData: MetaData) {
        metaData::class.java.declaredFields.forEach {
            it.isAccessible = true
            Log.i(
                "META-${it.name.uppercase().substring(0..min(18, it.name.length-1))}",
                it.get(metaData)?.toString() ?: "")
        }
    }
}