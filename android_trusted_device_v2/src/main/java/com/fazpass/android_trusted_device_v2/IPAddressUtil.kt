package com.fazpass.android_trusted_device_v2

import android.os.Handler
import android.os.Looper
import java.io.IOException
import java.net.URL
import java.util.Scanner

internal class IPAddressUtil {

    fun getIPAddress(callback: (String) -> Unit) {
        Thread {
            var s: Scanner? = null
            var ip = ""
            try {
                s = Scanner(
                    URL("https://api.ipify.org").openStream(),
                    "UTF-8"
                ).useDelimiter("\\A")
                ip = s.next()
            } catch (ignored: IOException) {
            } finally {
                s?.close()
            }

            Handler(Looper.getMainLooper()).post { callback(ip) }
        }.start()
    }
}