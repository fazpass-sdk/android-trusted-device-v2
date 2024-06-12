package com.fazpass.android_trusted_device_v2_app

import android.app.Activity
import android.util.Log
import com.fazpass.android_trusted_device_v2.Fazpass
import com.fazpass.android_trusted_device_v2.FazpassException
import com.fazpass.android_trusted_device_v2.SensitiveData
import com.fazpass.android_trusted_device_v2.`object`.FazpassSettings
import org.json.JSONObject

class FazpassService(private val activity: Activity) {

    private val fazpass = Fazpass.instance

    companion object {
        private const val PUBLIC_KEY_ASSET_FILENAME = "new-public-key.pub"
        //"staging-public_key.pub"
        //"new-public-key.pub"
        //"my_public_key.pub"
        private const val ACCOUNT_INDEX = 0
    }

    fun init() {
        fazpass.init(activity, PUBLIC_KEY_ASSET_FILENAME)
        Log.i("APP SIGNATURES", Fazpass.helper.getAppSignatures(activity).toString())
    }

    fun getSettings(): Settings {
        val fazpassSettings = fazpass.getSettings(ACCOUNT_INDEX)
        return Settings(
            fazpassSettings?.sensitiveData?.contains(SensitiveData.location) ?: false,
            fazpassSettings?.sensitiveData?.contains(SensitiveData.simNumbersAndOperators) ?: false,
            fazpassSettings?.isBiometricLevelHigh ?: false
        )
    }

    fun setSettings(settings: Settings) {
        val newFazpassSettings = FazpassSettings.Builder()

        if (settings.isLocationEnabled) {
            newFazpassSettings.enableSelectedSensitiveData(SensitiveData.location)
        } else {
            newFazpassSettings.disableSelectedSensitiveData(SensitiveData.location)
        }

        if (settings.isSimInfoEnabled) {
            newFazpassSettings.enableSelectedSensitiveData(SensitiveData.simNumbersAndOperators)
        } else {
            newFazpassSettings.disableSelectedSensitiveData(SensitiveData.simNumbersAndOperators)
        }

        if (settings.isHighLevelBiometricEnabled) {
            fazpass.generateNewSecretKey(activity)
            newFazpassSettings.setBiometricLevelToHigh()
        } else {
            newFazpassSettings.setBiometricLevelToLow()
        }

        fazpass.setSettings(activity, ACCOUNT_INDEX, newFazpassSettings.build())
    }

    fun generateMeta(callback: (String, FazpassException?) -> Unit) {
        fazpass.generateMeta(activity, ACCOUNT_INDEX, callback)
    }

    fun listenToIncomingCrossDeviceData(
        onRequest: (deviceName: String, notificationId: String) -> Unit,
        onValidate: (deviceName: String, action: String) -> Unit
    ) {
        val fromNotification = fazpass.getCrossDeviceDataFromNotification(activity.intent)
        if (fromNotification != null) {
            when (fromNotification.status) {
                "request" -> onRequest(localizeDeviceName(fromNotification.deviceRequest), fromNotification.notificationId!!)
                "validate" -> onValidate(localizeDeviceName(fromNotification.deviceReceive), fromNotification.action!!)
                else -> println(JSONObject(fromNotification.toMap()).toString())
            }
        }

        fazpass.getCrossDeviceDataStreamInstance(activity).listen {
            when (it.status) {
                "request" -> onRequest(localizeDeviceName(it.deviceRequest), it.notificationId!!)
                "validate" -> onValidate(localizeDeviceName(it.deviceReceive), it.action!!)
                else -> println(JSONObject(it.toMap()).toString())
            }
        }
    }

    /**
     * FROM: VIVO;Vivo V1;MT6769V/CZ;Android 31
     *
     * TO: VIVO, Vivo V1
     */
    private fun localizeDeviceName(rawDeviceName: String): String {
        return rawDeviceName.split(";").subList(0, 2).joinToString(", ")
    }
}