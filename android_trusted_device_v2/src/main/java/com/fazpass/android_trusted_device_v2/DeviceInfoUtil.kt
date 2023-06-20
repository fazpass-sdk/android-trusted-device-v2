package com.fazpass.android_trusted_device_v2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.fazpass.android_trusted_device_v2.`object`.DeviceInfo
import java.io.File
import java.io.IOException
import java.util.Scanner


class DeviceInfoUtil {

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
                    val vals = scanner.nextLine().split(": ")
                    if (vals.size > 1) map[vals[0].lowercase().trim { it <= ' ' }] = vals[1].trim { it <= ' ' }
                }

                Log.d("CPU INFO", map.toString())
                if (map.contains("model name")) {
                    cpuModel = map["model name"]
                }
                else if (map.contains("hardware")) {
                    cpuModel = map["hardware"]
                }
                else if (map.contains("cpu implementer")) {
                    cpuModel = map["cpu implementer"]
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return cpuModel ?: Build.SUPPORTED_ABIS[0]
        }

        fun getSimNumbers(context: Context) : List<String> {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED) {
                return emptyList()
            }

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                sm.activeSubscriptionInfoList
                    .map {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                            && ContextCompat.checkSelfPermission(
                                context, Manifest.permission.READ_PHONE_NUMBERS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            sm.getPhoneNumber(it.subscriptionId)
                        } else {
                            it.number
                        }
                    }
            } else {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                listOf(tm.line1Number)
            }
        }
    }
}