package com.fazpass.android_trusted_device_v2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build

@Suppress("DEPRECATION")
internal class NotificationUtil(private val context: Context) {

    companion object {
        const val channelId = "fazpass_android_trusted_device_v2"
        const val channelName = "Fazpass Notification Channel"

        const val locationNotificationId = 1
    }

    fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    val requestLocationNotification : Notification
        get() {
            val title = "Requesting location..."

            return createNotificationBuilder()
                .setContentTitle(title)
                .build()
        }

    private fun createNotificationBuilder() : Notification.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            Notification.Builder(context)
        }
    }
}