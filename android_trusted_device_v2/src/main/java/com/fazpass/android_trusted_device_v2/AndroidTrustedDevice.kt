package com.fazpass.android_trusted_device_v2

import android.content.Context
import android.content.pm.ApplicationInfo
import android.location.Location
import com.fazpass.android_trusted_device_v2.`object`.Coordinate
import com.fazpass.android_trusted_device_v2.`object`.MetaData

open class AndroidTrustedDevice internal constructor() {

    fun generateMeta(context: Context, callback: (MetaData) -> Unit) {
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