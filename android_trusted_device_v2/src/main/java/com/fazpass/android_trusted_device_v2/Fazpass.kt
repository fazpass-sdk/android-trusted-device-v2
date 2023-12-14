package com.fazpass.android_trusted_device_v2

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.fazpass.android_trusted_device_v2.`object`.BiometricInfo
import com.fazpass.android_trusted_device_v2.`object`.Coordinate
import com.fazpass.android_trusted_device_v2.`object`.CrossDeviceRequest
import com.fazpass.android_trusted_device_v2.`object`.CrossDeviceRequestStream
import com.fazpass.android_trusted_device_v2.`object`.FazpassSettings
import com.fazpass.android_trusted_device_v2.`object`.MetaData
import com.fazpass.android_trusted_device_v2.`object`.MetaDataSerializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

class Fazpass private constructor(): AndroidTrustedDevice {

    private var isInitialized : Boolean = false
    private lateinit var assetManager : AssetManager
    private lateinit var publicKeyAssetName : String
    private lateinit var settings : HashMap<Int, FazpassSettings>

    companion object {
        /**
         * If true, every print and log will be recorded to terminal.
         *
         * Change to false on production!
         */
        internal const val IS_DEBUG = true

        private const val FCM_TOKEN_TIMEOUT = 5 // in seconds

        val instance : Fazpass by lazy { Fazpass() }
    }

    override fun init(context: Context, publicKeyAssetName: String) {
        this.publicKeyAssetName = publicKeyAssetName
        NotificationUtil(context).initialize()

        // load settings
        this.settings = hashMapOf()
        val prefs = SharedPreferenceUtil(context)
        prefs.getAccountIndexSet().forEach {
            val accountIndex = it.toInt()
            val setting = prefs.getFazpassSettings(accountIndex)
            if (setting != null) {
                this.settings[accountIndex] = setting
            }
        }

        this.isInitialized = true
    }

    override fun generateMeta(activity: Activity, accountIndex: Int, callback: (String, FazpassException?) -> Unit) {
        doGenerateMeta(activity, accountIndex) { meta, exception ->
            if (exception != null) {
                // TODO: Add error reporter here
            }
            callback(meta, exception)
        }
    }

    override fun generateSecretKeyForHighLevelBiometric(context: Context) {
        // delete all saved iv parameter and encrypted string
        val prefs = SharedPreferenceUtil(context)
        val accountIndexSet = prefs.getAccountIndexSet()
        for (i in accountIndexSet) {
            prefs.removeEncryptedString(i.toInt())
            prefs.removeIVParameter(i.toInt())
        }

        // generate new key
        SecureUtil.generateKey()
    }

    override fun setSettingsForAccountIndex(context: Context, accountIndex: Int, settings: FazpassSettings?) {
        val prefs = SharedPreferenceUtil(context)

        if (settings != null) {
            this.settings[accountIndex] = settings
        } else {
            this.settings.remove(accountIndex)
        }

        prefs.saveFazpassSettings(accountIndex, settings)
    }

    override fun getSettingsForAccountIndex(accountIndex: Int): FazpassSettings? =
        this.settings[accountIndex]

    override fun getCrossDeviceRequestStreamInstance(context: Context) : CrossDeviceRequestStream {
        return CrossDeviceRequestStream(
            context,
            channel = NotificationUtil.fcmCrossDeviceRequestReceiverChannel,
        )
    }

    override fun getCrossDeviceRequestFromFirstActivityIntent(intent: Intent?): CrossDeviceRequest? {
        val bundle = intent?.extras ?: return null
        val request = CrossDeviceRequest(bundle)

        if (request.merchantAppId != ""
            && request.expired != -1
            && request.deviceReceive != ""
            && request.deviceRequest != ""
            && request.deviceIdReceive != ""
            && request.deviceIdRequest != "") return request
        return null
    }

    private fun getFcmToken() : String? = NotificationUtil.fcmToken

    private fun doGenerateMeta(activity: Activity, accountIndex: Int, callback: (String, FazpassException?) -> Unit) {
        if (!isInitialized) throw UninitializedException()
        this.assetManager = activity.assets

        // declare settings for this generated meta
        var locationEnabled = false
        var simNumbersAndOperatorsEnabled = false
        var isBiometricLevelHigh = false

        // load settings that has been set for this account index
        val settings = this.settings[accountIndex]
        if (settings != null) {
            locationEnabled = settings.sensitiveData.contains(SensitiveData.location)
            simNumbersAndOperatorsEnabled = settings.sensitiveData.contains(SensitiveData.simNumbersAndOperators)
            isBiometricLevelHigh = settings.isBiometricLevelHigh
        }

        openBiometric(activity, accountIndex, isBiometricLevelHigh) { hasChanged, biometricErr ->
            if (biometricErr != null) {
                if (IS_DEBUG) biometricErr.printStackTrace()
                callback("", biometricErr)
                return@openBiometric
            }

            val platform = "android"
            val packageName = activity.packageName
            val isDebuggable = 0 != activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
            val isRooted = RootUtil(activity).isDeviceRooted
            val isEmulator = EmulatorUtil().isEmulator
            val isVpn = ConnectionUtil(activity).isVpnConnectionAvailable
            val isCloned = CloningUtil(activity).isAppCloned
            val isScreenMirroring = ScreenMirroringUtil(activity).isScreenMirroring
            val signatures = AppSignatureUtil(activity).getSignatures
            val deviceInfo = DeviceInfoUtil().deviceInfo
            val biometricInfo = BiometricInfo(if (isBiometricLevelHigh) "HIGH" else "LOW", hasChanged)

            Thread {
                runBlocking {
                    // sim numbers & operators
                    val dataCarrierUtil = if (simNumbersAndOperatorsEnabled) DataCarrierUtil(activity) else null
                    val simNumbers = dataCarrierUtil?.simNumbers ?: listOf()
                    val simOperators = dataCarrierUtil?.simOperators ?: listOf()

                    // ip address
                    val ipAddress = suspendCoroutine {
                        IPAddressUtil().getIPAddress { ip ->
                            it.resume(ip)
                        }
                    }

                    // location
                    val locationUtil = if (locationEnabled) LocationUtil(activity) else null
                    val location = suspendCoroutine {
                        locationUtil?.getLastKnownLocation { location ->
                            it.resume(location)
                        } ?: it.resume(null)
                    }
                    val coordinate = Coordinate(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
                    val isMockLocation = locationUtil?.isMockLocationOn(location) ?: false

                    // fcm token
                    val fcmTokenFlow : Flow<String> = flow {
                        for (i in 0..FCM_TOKEN_TIMEOUT) {
                            if (getFcmToken() != null) {
                                emit(getFcmToken()!!)
                                break
                            }
                            delay(1000L)
                        }
                    }
                    val fcmToken = fcmTokenFlow.singleOrNull() ?: ""

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
                        fcmToken = fcmToken,
                        biometric = biometricInfo,
                    )
                    if (IS_DEBUG) printMetaData(metadata)

                    activity.runOnUiThread {
                        try {
                            val encryptedMetaData = encryptMetaData(metadata)
                            callback(encryptedMetaData, null)
                        } catch (e: PublicKeyNotExistException) {
                            callback("", e)
                        } catch (e: Exception) {
                            callback("", EncryptionException(e))
                        }
                    }
                }
            }.start()
        }
    }

    private fun openBiometric(ctx: Activity, accountIndex: Int, isBiometricLevelHigh: Boolean,
                              callback: (hasChanged: Boolean, FazpassException?) -> Unit) {
        val authenticators = if (isBiometricLevelHigh) BIOMETRIC_STRONG else DEVICE_CREDENTIAL or BIOMETRIC_STRONG

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val keyguardManager = ctx.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (!keyguardManager.isDeviceSecure) {
                callback(false, BiometricNoneEnrolledError())
                return
            }
        }

        val biometricManager = BiometricManager.from(ctx)
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                callback(false, BiometricNoneEnrolledError())
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                callback(false, BiometricUnavailableError())
                return
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                callback(false, BiometricSecurityUpdateRequiredError())
                return
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                callback(false, BiometricUnsupportedError())
                return
            }
            else -> {}
        }

        var hasChanged = false
        var cipherMode = CipherMode.None

        val listener = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                callback(hasChanged, BiometricAuthError(errString.toString()))
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                if (hasChanged || cipherMode == CipherMode.None) {
                    callback(hasChanged, null)
                    return
                }

                val cipher = result.cryptoObject!!.cipher!!
                val prefs = SharedPreferenceUtil(ctx)
                val originalString = DeviceInfoUtil().deviceInfo.toString()
                if (cipherMode == CipherMode.Encrypt) {
                    val encryptedData = cipher.doFinal(originalString.toByteArray())
                    val encryptedString = Base64.encodeToString(encryptedData, Base64.DEFAULT)

                    prefs.saveEncryptedString(accountIndex, encryptedString)
                    prefs.saveIVParameter(accountIndex, IvParameterSpec(cipher.iv))

                    callback(false, null)
                }
                if (cipherMode == CipherMode.Decrypt) {
                    val encryptedString = prefs.loadEncryptedString(accountIndex) ?: ""

                    val encryptedData = try { Base64.decode(encryptedString, Base64.DEFAULT) }
                    catch (e: Exception) { byteArrayOf() }
                    val decryptedString = String(cipher.doFinal(encryptedData))

                    if (originalString == decryptedString) {
                        callback(false, null)
                    } else {
                        callback(true, null)
                    }
                }
            }
        }
        val executor = ContextCompat.getMainExecutor(ctx)
        val biometricPrompt = BiometricPrompt(ctx as FragmentActivity, executor, listener)
        val promptInfoBuilder: BiometricPrompt.PromptInfo.Builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Required")
            .setAllowedAuthenticators(authenticators)

        if (isBiometricLevelHigh) {
            // required for biometric prompt
            promptInfoBuilder.setNegativeButtonText("Cancel")

            val prefs = SharedPreferenceUtil(ctx)
            val iv = prefs.loadIvParameter(accountIndex)
            // if there is no saved iv, do encrypt
            val cipher = if (iv == null) {
                cipherMode = CipherMode.Encrypt
                SecureUtil.getEncryptCipher()
            }
            // otherwise do decrypt
            else {
                cipherMode = CipherMode.Decrypt
                SecureUtil.getDecryptCipher(iv)
            }

            // if cipher is null, it means key has been invalidated. set hasChanged to true.
            // then authenticate without encryption test.
            if (cipher == null) {
                hasChanged = true
                biometricPrompt.authenticate(promptInfoBuilder.build())
                return
            }

            biometricPrompt.authenticate(promptInfoBuilder.build(), BiometricPrompt.CryptoObject(cipher))
        } else {
            biometricPrompt.authenticate(promptInfoBuilder.build())
        }
    }

    private fun encryptMetaData(metadata: MetaData) : String {
        val jsonString: String = MetaDataSerializer(metadata).result
        if (IS_DEBUG) Log.i("META-AS-STRING", jsonString)

        var keyInputStream : InputStream? = null
        try {
            keyInputStream = assetManager.open(publicKeyAssetName)
        } catch (e: IOException) {
            if (IS_DEBUG) e.printStackTrace()
            assetManager.close()
        }
        if (keyInputStream == null) throw PublicKeyNotExistException(publicKeyAssetName)

        var key = String(keyInputStream.readBytes())
        keyInputStream.close()
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

    private enum class CipherMode {
        Encrypt, Decrypt, None
    }
}