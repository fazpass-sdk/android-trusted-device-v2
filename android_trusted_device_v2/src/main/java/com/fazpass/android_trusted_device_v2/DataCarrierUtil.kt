package com.fazpass.android_trusted_device_v2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

@Suppress("DEPRECATION")
internal class DataCarrierUtil(private val context: Context) {

    private val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    val simNumbers: List<String>
        get() {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED) {
                return emptyList()
            }

            return sm.activeSubscriptionInfoList
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
        }

    val simOperators: List<String>
        get() {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED) {
                return emptyList()
            }

            return sm.activeSubscriptionInfoList
                .map { it.carrierName.toString() }
        }
}