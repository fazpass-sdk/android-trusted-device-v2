package com.fazpass.android_trusted_device_v2

import android.app.NotificationManager
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
            Log.i("Notification notification title", message.notification?.title ?: "None")
            Log.i("Notification data", "${message.data}")

            showNotification(message.notification)

            /*val data = message.data
            val intent = Intent(NotificationUtil.fcmMessageReceiverChannel)
            intent.putExtra("data", CrossDeviceData(
                notificationId = data["notification_id"] ?: "",
                device = data["device"] ?: "",
                location = data["location"] ?: ""
            ))
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)*/
        } catch (e: Exception) {
            Log.e("onMessageReceived ERROR", e.message!!)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val intent = Intent(NotificationUtil.fcmTokenReceiverChannel)
        intent.putExtra("token", token)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun showNotification(notification: RemoteMessage.Notification?) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationUtil.fcmNotificationId, NotificationUtil(this).fcmNotification(notification))
    }
}