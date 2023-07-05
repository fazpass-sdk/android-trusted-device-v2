package com.fazpass.android_trusted_device_v2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Build
import android.os.IBinder

@Suppress("DEPRECATION")
internal class LocationUtil(private val context: Context) {

    private val serviceIntent = Intent(context, LocationService::class.java)

    companion object {
        var lastRequestedLocation: Location? = null
    }

    fun getLastKnownLocation(callback: (Location?) -> Unit) {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as LocationService.LocationBinder
                binder.getService().getLastKnownLocation(lastRequestedLocation) {
                    lastRequestedLocation = it
                    callback(it)
                    context.unbindService(this)
                    context.stopService(serviceIntent)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        }
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    fun isMockLocationOn(location: Location?) : Boolean {
        if (location == null) {
            return false
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            location.isMock
        else
            location.isFromMockProvider
    }
}