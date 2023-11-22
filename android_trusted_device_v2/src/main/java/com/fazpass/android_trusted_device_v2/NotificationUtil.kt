package com.fazpass.android_trusted_device_v2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("DEPRECATION")
internal class NotificationUtil(private val context: Context) {

    companion object {
        /// change the value of default_notification_channel_id metadata in manifest when this value is changed
        const val channelId = "fazpass_android_trusted_device_v2_notification_channel"
        const val channelName = "Fazpass Notification"

        const val fcmTokenReceiverChannel = "fazpass_fcm_token_receiver_channel"
        const val fcmCrossDeviceRequestReceiverChannel = "fazpass_fcm_cd_request_receiver_channel"

        const val locationNotificationId = 1

        var analytics: FirebaseAnalytics? = null
        var fcmToken : String? = null
    }

    fun initialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH)
            )
        }

        // setup firebase analytics
        analytics = Firebase.analytics

        // listen for current token
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            fcmToken = it
            if (Fazpass.IS_DEBUG) Log.i("FCM Token from getToken", it ?: "none")
        }

        // register a receiver when there is a new fcm token, received from NotificationService
        val updatedFcmTokenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val token = intent.getStringExtra("token")
                fcmToken = token
                if (Fazpass.IS_DEBUG) Log.i("FCM Token from onNewToken", token ?: "none")
            }
        }
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(updatedFcmTokenReceiver, IntentFilter(fcmTokenReceiverChannel))
    }

    private fun createNotificationBuilder() : Notification.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            Notification.Builder(context)
        }
    }

    val requestLocationNotification : Notification
        get() {
            val title = "Requesting Location..."
            val text = "Make sure you have internet connection and turned on location service."

            return createNotificationBuilder()
                .setContentTitle(title)
                .setContentText(text)
                .build()
        }

    /*fun fcmNotification(notification: RemoteMessage.Notification?) : Notification {
        val logo = context.applicationInfo.icon
        return createNotificationBuilder()
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(logo)
            .setContentTitle(notification?.title ?: "Verify Login")
            .setContentText(notification?.body ?: "An unknown device is trying to login with your account. Press this notification to verify.")
            .build()
    }*/
}