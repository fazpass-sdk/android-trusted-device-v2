package com.fazpass.android_trusted_device_v2

import android.content.Context
import com.scottyab.rootbeer.RootBeer

internal class RootUtil(private val context: Context) {

    val isDeviceRooted: Boolean
        get() {
            val rootBeer = RootBeer(context)
            rootBeer.setLogging(Fazpass.IS_DEBUG)
            return rootBeer.isRooted
        }
}