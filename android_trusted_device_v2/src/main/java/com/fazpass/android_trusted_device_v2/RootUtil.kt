package com.fazpass.android_trusted_device_v2

import android.content.Context
import android.os.Build
import com.scottyab.rootbeer.RootBeer
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


internal class RootUtil {

    companion object {

        fun isDeviceRooted(context: Context): Boolean {
            val simpleCheck = checkRootMethod1() || checkRootMethod2() || checkRootMethod3() || checkRootMethod4()
            if (simpleCheck) return true
            return checkRootMethod5(context)
        }

        private fun checkRootMethod1(): Boolean {
            val buildTags = Build.TAGS
            return buildTags != null && buildTags.contains("test-keys")
        }

        private fun checkRootMethod2(): Boolean {
            val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su", "/system/bin/su", "/system/xbin/su",
                "/data/local/xbin/su", "/data/local/bin/su",
                "/system/sd/xbin/su", "/system/bin/failsafe/su",
                "/data/local/su", "/su/bin/su"
            )
            for (path in paths) {
                if (File(path).exists()) return true
            }
            return false
        }

        private fun checkRootMethod3(): Boolean {
            var process: Process? = null
            return try {
                process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
                val `in` = BufferedReader(InputStreamReader(process.inputStream))
                `in`.readLine() != null
            } catch (t: Throwable) {
                false
            } finally {
                process?.destroy()
            }
        }

        private fun checkRootMethod4() : Boolean {
            val roSecure = SystemProperties.getProp("ro.secure")
            val roDebuggable = SystemProperties.getProp("ro.debuggable")

            if (roSecure == "" || roDebuggable == "") return false
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    roSecure != "1" &&
                    roDebuggable != "0"
        }

        private fun checkRootMethod5(context: Context): Boolean {
            val rootBeer = RootBeer(context)
            return rootBeer.isRooted
        }
    }
}