package com.fazpass.android_trusted_device_v2

import android.os.Build
import com.fazpass.android_trusted_device_v2.`object`.DeviceInfo
import java.io.File
import java.util.Scanner

internal class DeviceInfoUtil {

    companion object {

        val deviceInfo : DeviceInfo by lazy {
            DeviceInfo("Android ${Build.VERSION.SDK_INT}", Build.BRAND, Build.TYPE, getCpuModel())
        }

        private fun getCpuModel() : String {
            var cpuModel: String? = null

            try {
                val map: HashMap<String, String> = HashMap()
                val scanner =  Scanner(File("/proc/cpuinfo"))
                while (scanner.hasNextLine()) {
                    val v = scanner.nextLine().split(": ")
                    if (v.size > 1) map[v[0].lowercase().trim { it <= ' ' }] = v[1].trim { it <= ' ' }
                }

                when {
                    map.contains("model name") -> cpuModel = map["model name"]
                    map.contains("hardware") -> cpuModel = map["hardware"]
                    map.contains("cpu implementer") -> cpuModel = map["cpu implementer"]
                }
            } catch (e: Exception) {
                if (AndroidTrustedDevice.IS_DEBUG) e.printStackTrace()
            }

            return cpuModel ?: Build.SUPPORTED_ABIS[0]
        }
    }
}