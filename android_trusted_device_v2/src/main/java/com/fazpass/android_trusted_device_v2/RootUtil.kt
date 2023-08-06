package com.fazpass.android_trusted_device_v2

import android.content.Context
import com.scottyab.rootbeer.RootBeer

internal class RootUtil {

    companion object {

        fun isDeviceRooted(context: Context): Boolean {
            val rootBeer = RootBeer(context)
            rootBeer.setLogging(Fazpass.IS_DEBUG)
            return rootBeer.isRooted
        }
    }
}