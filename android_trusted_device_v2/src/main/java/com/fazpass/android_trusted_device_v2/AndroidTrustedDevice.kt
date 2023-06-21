package com.fazpass.android_trusted_device_v2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fazpass.android_trusted_device_v2.enum.CrossDeviceStatus
import com.fazpass.android_trusted_device_v2.enum.TrustedDeviceStatus
import com.fazpass.android_trusted_device_v2.`object`.Coordinate
import com.fazpass.android_trusted_device_v2.`object`.MetaData

internal class AndroidTrustedDevice : Fazpass {

    private var appPackageName : String? = null

    override fun init(context: Context, appPackageName: String) {
        this.appPackageName = appPackageName
        NotificationUtil(context).initNotificationChannel()
        CloningUtil.init(appPackageName)
    }

    override fun check(
        context: Context,
        callback: (TrustedDeviceStatus, CrossDeviceStatus) -> Unit
    ) {
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

            // simulate device is trusted and cross device is available
            callback(TrustedDeviceStatus.Trusted, CrossDeviceStatus.Available)
        }
    }

    override fun validate(callback: (Double) -> Unit) {
        // simulate super trusted confidence level
        callback(1.0)
    }

    override fun enrollByPin(context: Context, pin: String, callback: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun enrollByFinger(context: Context, callback: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeDevice(context: Context) {
        TODO("Not yet implemented")
    }

    override fun validateCrossDevice(context: Context) {
        TODO("Not yet implemented")
    }

    override fun requestPermissions(activity: Activity) {
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

    override fun getSignatures(context: Context): List<String>? {
        return AppSignatureUtil.getSignatures(context)
    }

    private fun generateMeta(context: Context, callback: (MetaData) -> Unit) {
        val platform = "android"
        val isRooted = RootUtil.isDeviceRooted(context)
        val isVpn = ConnectionUtil.isVpnConnectionAvailable(context)
        val isCloned = CloningUtil(context).isAppCloned
        val isScreenMirroring = ScreenMirroringUtil(context).isScreenMirroring
        val isDebuggable = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

        val signatures = AppSignatureUtil.getSignatures(context)
        val deviceInfo = DeviceInfoUtil.deviceInfo
        val simNumbers = DeviceInfoUtil.getSimNumbers(context)

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

            EmulatorUtil.isEmulator(context) { isEmulator ->
                callback(MetaData(
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
                    coordinate = coordinate,
                    isMockLocation = isMockLocation
                ))
            }
        }
    }

}