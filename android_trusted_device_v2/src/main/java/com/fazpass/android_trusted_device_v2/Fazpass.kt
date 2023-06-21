package com.fazpass.android_trusted_device_v2

import android.app.Activity
import android.content.Context
import com.fazpass.android_trusted_device_v2.enum.CrossDeviceStatus
import com.fazpass.android_trusted_device_v2.enum.TrustedDeviceStatus

interface Fazpass {

    companion object {
        val instance : Fazpass by lazy { AndroidTrustedDevice() }
    }

    fun init(context: Context, appPackageName: String)

    fun check(context: Context, callback: (TrustedDeviceStatus, CrossDeviceStatus) -> Unit)

    fun validate(callback: (Double) -> Unit)

    fun enrollByPin(context: Context, pin: String, callback: (Boolean) -> Unit)

    fun enrollByFinger(context: Context, callback: (Boolean) -> Unit)

    fun removeDevice(context: Context)

    fun validateCrossDevice(context: Context)

    fun requestPermissions(activity: Activity)

    fun getSignatures(context: Context) : List<String>?
}