package com.fazpass.android_trusted_device_v2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Arrays

class Fazpass private constructor(): AndroidTrustedDevice() {

    companion object {
        val instance : Fazpass by lazy { Fazpass() }

        var appPackageName : String? = null
    }

    fun init(context: Context, appPackageName: String) {
        Fazpass.appPackageName = appPackageName
        NotificationUtil(context).initNotificationChannel()
        CloningUtil.init(appPackageName)
    }

    fun check(context: Context) {
        generateMeta(context) {
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
            Log.i("META-COORDINATE", it.coordinate.toString())
            Log.i("META-MOCK_LOCATION", it.isMockLocation.toString())
        }
    }

    fun requestPermission(activity: Activity) {
        val requiredPermissions = ArrayList(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
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
}