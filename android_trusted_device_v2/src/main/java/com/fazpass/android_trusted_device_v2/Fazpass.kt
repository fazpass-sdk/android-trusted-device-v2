package com.fazpass.android_trusted_device_v2

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.fazpass.android_trusted_device_v2.`object`.Coordinate
import com.fazpass.android_trusted_device_v2.`object`.DeviceInfo
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

class Fazpass private constructor(): AndroidTrustedDevice {

    private var isInitialized : Boolean = false
    private lateinit var assetManager : AssetManager
    private lateinit var publicKeyAssetName : String

    private var locationEnabled = false
    private var simNumbersAndOperatorsEnabled = false

    private var tempMetaMapper : HashMap<String, Any>? = null

    companion object {
        /** If true, every print and log will be recorded to terminal */
        // TODO: Change to false on production!
        const val IS_DEBUG = false

        val instance : Fazpass by lazy { Fazpass() }
    }

    override fun init(context: Context, publicKeyAssetName: String) {
        assetManager = context.assets

        var assetNotExist = false
        var iSPubKey : InputStream? = null
        try {
            iSPubKey = assetManager.open(publicKeyAssetName)
        } catch (e: IOException) {
            if (IS_DEBUG) e.printStackTrace()
            assetNotExist = true
            assetManager.close()
        } finally {
            iSPubKey?.close()
        }
        if (assetNotExist) throw Exception("Key files doesn't exist in the 'assets' directory!")

        this.publicKeyAssetName = publicKeyAssetName
        NotificationUtil(context).initNotificationChannel()
        this.isInitialized = true
    }

    override fun generateMeta(activity: Activity, callback: (String, Exception?) -> Unit) {
        if (!isInitialized) throw Exception("Fazpass init has to be called first!")

        openBiometric(activity) { biometricErr ->
            if (biometricErr != null) {
                if (IS_DEBUG) biometricErr.printStackTrace()
                callback("", biometricErr)
                return@openBiometric
            }

            tempMetaMapper = hashMapOf()
            tempMetaMapper!!["platform"] = "android"
            tempMetaMapper!!["packageName"] = activity.packageName
            tempMetaMapper!!["isDebuggable"] = 0 != activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
            tempMetaMapper!!["isRooted"] = RootUtil.isDeviceRooted(activity)
            tempMetaMapper!!["isEmulator"] = EmulatorUtil.isEmulator
            tempMetaMapper!!["isVpn"] = ConnectionUtil.isVpnConnectionAvailable(activity)
            tempMetaMapper!!["isCloned"] = CloningUtil(activity).isAppCloned
            tempMetaMapper!!["isScreenMirroring"] = ScreenMirroringUtil(activity).isScreenMirroring
            tempMetaMapper!!["signatures"] = AppSignatureUtil.getSignatures(activity)
            tempMetaMapper!!["deviceInfo"] = DeviceInfoUtil.deviceInfo

            val dataCarrierUtil = if (simNumbersAndOperatorsEnabled) DataCarrierUtil(activity) else null
            tempMetaMapper!!["simNumbers"] = dataCarrierUtil?.simNumbers ?: listOf<String>()
            tempMetaMapper!!["simOperators"] = dataCarrierUtil?.simOperators ?: listOf<String>()

            IPAddressUtil.getIPAddress { ipAddress ->
                tempMetaMapper!!["ipAddress"] = ipAddress
                finalizeGenerateMeta(callback)
            }

            val locationUtil = if (locationEnabled) LocationUtil(activity) else null
            locationUtil?.getLastKnownLocation { location ->
                tempMetaMapper!!["isMockLocation"] = locationUtil.isMockLocationOn(location)
                tempMetaMapper!!["coordinate"] = if (location != null) {
                    Coordinate(location.latitude, location.longitude)
                } else {
                    Coordinate(0.0,0.0)
                }
                finalizeGenerateMeta(callback)
            }.let {
                if (it == null) {
                    tempMetaMapper!!["isMockLocation"] = false
                    tempMetaMapper!!["coordinate"] = Coordinate(0.0,0.0)
                    finalizeGenerateMeta(callback)
                }
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

    @Suppress("UNCHECKED_CAST")
    private fun finalizeGenerateMeta(callback: (String, Exception?) -> Unit) {
        if (tempMetaMapper == null) return
        val platform = tempMetaMapper!!["platform"]
        val packageName = tempMetaMapper!!["packageName"]
        val isDebuggable = tempMetaMapper!!["isDebuggable"]
        val isRooted = tempMetaMapper!!["isRooted"]
        val isEmulator = tempMetaMapper!!["isEmulator"]
        val isVpn = tempMetaMapper!!["isVpn"]
        val isCloned = tempMetaMapper!!["isCloned"]
        val isScreenMirroring = tempMetaMapper!!["isScreenMirroring"]
        val signatures = tempMetaMapper!!["signatures"]
        val deviceInfo = tempMetaMapper!!["deviceInfo"]
        val simNumbers = tempMetaMapper!!["simNumbers"]
        val simOperators = tempMetaMapper!!["simOperators"]
        val ipAddress = tempMetaMapper!!["ipAddress"]
        val coordinate = tempMetaMapper!!["coordinate"]
        val isMockLocation = tempMetaMapper!!["isMockLocation"]

        val metadata: MetaData
        try {
            metadata = MetaData(
                platform = platform as String,
                isRooted = isRooted as Boolean,
                isEmulator = isEmulator as Boolean,
                isVpn = isVpn as Boolean,
                isCloned = isCloned as Boolean,
                isScreenMirroring = isScreenMirroring as Boolean,
                isDebuggable = isDebuggable as Boolean,
                signatures = signatures as List<String>,
                deviceInfo = deviceInfo as DeviceInfo,
                simNumbers = simNumbers as List<String>,
                simOperators = simOperators as List<String>,
                coordinate = coordinate as Coordinate,
                isMockLocation = isMockLocation as Boolean,
                packageName = packageName as String,
                ipAddress = ipAddress as String,
            )
            if (IS_DEBUG) printMetaData(metadata)
        } catch (_: NullPointerException) { return }

        try {
            val encryptedMetaData = encryptMetaData(metadata)
            callback(encryptedMetaData, null)
        } catch (e: Exception) {
            callback("", e)
        }

        tempMetaMapper = null
    }

    private fun openBiometric(ctx: Activity, callback: (Exception?) -> Unit) {
        val listener = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                callback(Exception(errString.toString()))
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                callback(null)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }
        val executor = ContextCompat.getMainExecutor(ctx)
        val biometricPrompt = BiometricPrompt(ctx as FragmentActivity, executor, listener)
        val promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Required")
            .setSubtitle("")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun encryptMetaData(metadata: MetaData) : String {
        val jsonString: String = MetaDataSerializer(metadata).result
        if (IS_DEBUG) Log.i("META-AS-STRING", jsonString)

        var key = String(assetManager.open(publicKeyAssetName).readBytes())
        key = key.replace("-----BEGIN RSA PUBLIC KEY-----", "")
            .replace("-----END RSA PUBLIC KEY-----", "")
            .replace("\n", "").replace("\r", "")
        val publicKey: PublicKey?
        try {
            val keySpec = X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT))
            val keyFactory = KeyFactory.getInstance("RSA")
            publicKey = keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            if (IS_DEBUG) e.printStackTrace()
            throw e
        }

        // Encrypt string JSON with public key
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(jsonString.toByteArray(StandardCharsets.UTF_8))

        // Encode to base64 string then return
        val base64Result = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        if (IS_DEBUG) Log.i("META-RESULT", base64Result)
        return base64Result
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