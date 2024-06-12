package com.fazpass.android_trusted_device_v2.`object`

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fazpass.android_trusted_device_v2.Fazpass

/**
 * An instance acquired from [Fazpass.getCrossDeviceDataStreamInstance] to start listening for
 * incoming cross device notification data.
 *
 * call [listen] method to start listening, and call [close] to stop.
 */
@Suppress("DEPRECATION")
class CrossDeviceDataStream internal constructor(
    val context: Context,
    val channel: String
) {
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("data", CrossDeviceData::class.java)
            } else {
                intent.getSerializableExtra("data") as CrossDeviceData
            }
            if (data != null && callback != null) {
                callback!!(data)
            }
        }
    }

    private var callback: ((CrossDeviceData) -> Unit)? = null

    fun listen(callback: (CrossDeviceData) -> Unit) {
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