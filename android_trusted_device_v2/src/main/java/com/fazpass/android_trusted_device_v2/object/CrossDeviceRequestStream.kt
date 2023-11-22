package com.fazpass.android_trusted_device_v2.`object`

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager

@Suppress("DEPRECATION")
class CrossDeviceRequestStream internal constructor(
    val context: Context,
    val channel: String
) {
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("data", CrossDeviceRequest::class.java)
            } else {
                intent.getSerializableExtra("data") as CrossDeviceRequest
            }
            if (data != null && callback != null) {
                callback!!(data)
            }
        }
    }

    var callback: ((CrossDeviceRequest) -> Unit)? = null

    fun listen(callback: (CrossDeviceRequest) -> Unit) {
        if (this.callback != null) {
            close()
        }

        this.callback = callback
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(broadcastReceiver, IntentFilter(channel))
    }

    fun close() {
        this.callback = null
        LocalBroadcastManager.getInstance(context)
            .unregisterReceiver(broadcastReceiver)
    }
}