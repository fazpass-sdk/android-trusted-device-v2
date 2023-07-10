package com.fazpass.android_trusted_device_v2

import java.io.IOException
import java.net.URL
import java.util.Scanner

internal class IPAddressUtil {

    companion object {

        fun getIPAddress(callback: (String) -> Unit) {
            Thread {
                var s: Scanner? = null
                try {
                    s = Scanner(
                        URL("https://api.ipify.org").openStream(),
                        "UTF-8"
                    ).useDelimiter("\\A")
                    callback(s.next())
                } catch (e: IOException) {
                    callback("")
                } finally {
                    s?.close()
                }
            }.start()
        }
    }
}