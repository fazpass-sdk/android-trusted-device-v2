package com.fazpass.android_trusted_device_v2

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fazpass.android_trusted_device_v2.`object`.CrossDeviceData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        try {
            if (Fazpass.IS_DEBUG) Log.i("Notification title", message.notification?.title ?: "None")
            if (Fazpass.IS_DEBUG) Log.i("Notification data", "${message.data}")

            val intent = Intent(NotificationUtil.fcmCrossDeviceChannel)
            intent.putExtra("data", CrossDeviceData(message.data))
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } catch (e: Exception) {
            if (Fazpass.IS_DEBUG) Log.e("onMessageReceived ERROR", e.message!!)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val intent = Intent(NotificationUtil.fcmTokenChannel)
        intent.putExtra("token", token)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}