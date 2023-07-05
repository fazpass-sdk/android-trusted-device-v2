package com.fazpass.android_trusted_device_v2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.location.Location
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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


internal class AndroidTrustedDevice : Fazpass {

    private var isInitialized : Boolean = false
    private lateinit var assetManager : AssetManager
    private lateinit var publicKeyAssetName : String

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
        val isRooted = RootUtil.isDeviceRooted(context)
        val isEmulator = EmulatorUtil.isEmulator
        val isVpn = ConnectionUtil.isVpnConnectionAvailable(context)
        val isCloned = CloningUtil(context).isAppCloned
        val isScreenMirroring = ScreenMirroringUtil(context).isScreenMirroring
        val isDebuggable = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

        val signatures = AppSignatureUtil.getSignatures(context)
        val deviceInfo = DeviceInfoUtil.deviceInfo

        val dataCarrierUtil = DataCarrierUtil(context)
        val simNumbers = dataCarrierUtil.simNumbers
        val simOperators = dataCarrierUtil.simOperators

        val locationUtil = LocationUtil(context)
        locationUtil.getLastKnownLocation {
            val location : Location? = it
            val isMockLocation : Boolean = locationUtil.isMockLocationOn(it)
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
                packageName = packageName
            )
            if (IS_DEBUG) printMetaData(metadata)

            callback(encryptMetaData(metadata))
        }
    }

    override fun requestPermissions(activity: Activity) {
        val requiredPermissions = ArrayList(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requiredPermissions.add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }
        val deniedPermissions: MutableList<String> = ArrayList()
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                deniedPermissions.add(permission)
            }
        }
        if (deniedPermissions.size != 0) ActivityCompat.requestPermissions(
            activity,
            deniedPermissions.toTypedArray(),
            1
        )
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

    private fun printMetaData(it: MetaData) {
        Log.i("META-PLATFORM", it.platform)
        Log.i("META-ROOTED", it.isRooted.toString())
        Log.i("META-EMULATOR", it.isEmulator.toString())
        Log.i("META-VPN", it.isVpn.toString())
        Log.i("META-CLONED", it.isCloned.toString())
        Log.i("META-SCREEN_MIRRORING", it.isScreenMirroring.toString())
        Log.i("META-DEBUGGABLE", it.isDebuggable.toString())
        Log.i("META-SIGNATURES", it.signatures.toString())
        Log.i("META-DEVICE_INFO", it.deviceInfo.toString())
        Log.i("META-SIM_NUMBERS", it.simNumbers.toString())
        Log.i("META-SIM_OPERATORS", it.simOperators.toString())
        Log.i("META-COORDINATE_LAT", "${it.coordinate.lat}")
        Log.i("META-COORDINATE_LNG", "${it.coordinate.lng}")
        Log.i("META-MOCK_LOCATION", it.isMockLocation.toString())
    }
}