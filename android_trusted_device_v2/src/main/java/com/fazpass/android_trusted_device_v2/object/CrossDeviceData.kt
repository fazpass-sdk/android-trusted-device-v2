package com.fazpass.android_trusted_device_v2.`object`

import android.os.Bundle
import com.fazpass.android_trusted_device_v2.Fazpass
import java.io.Serializable

/**
 * An object containing data from cross device notification 
 *
 * This object is only used as data retrieved from [Fazpass.getCrossDeviceDataStreamInstance]
 * and [Fazpass.getCrossDeviceDataFromNotification].
 */
class CrossDeviceData: Serializable {

    companion object {
        private const val KEY_MERCHANT_APP_ID = "merchant_app_id"
        private const val KEY_DEVICE_RECEIVE = "device_receive"
        private const val KEY_DEVICE_REQUEST = "device_request"
        private const val KEY_DEVICE_ID_RECEIVE = "device_id_receive"
        private const val KEY_DEVICE_ID_REQUEST = "device_id_request"
        private const val KEY_EXPIRED = "expired"
        private const val KEY_STATUS = "status"
        private const val KEY_NOTIFICATION_ID = "notification_id"
        private const val KEY_ACTION = "action"
    }

    val merchantAppId: String
    /** example: Google;V3;MT6769V/CZ;Android 31 */
    val deviceReceive: String
    /** example: Google;V3;MT6769V/CZ;Android 31 */
    val deviceRequest: String
    val deviceIdReceive: String //390614ec-a507-4a49-b987-5547ce874ce5
    val deviceIdRequest: String //42bb672c-8eef-48c8-b4e3-40617dcb7f41
    val expired: String // 300
    /** either 'request' or 'validate' */
    val status: String //request/validate
    /** if status is 'request' only */
    val notificationId: String?
    /** if status is 'validate' only */
    val action: String?

    internal constructor(map: Map<String, String>) {
        merchantAppId = map[KEY_MERCHANT_APP_ID] ?: ""
        deviceReceive = map[KEY_DEVICE_RECEIVE] ?: ""
        deviceRequest = map[KEY_DEVICE_REQUEST] ?: ""
        deviceIdReceive = map[KEY_DEVICE_ID_RECEIVE] ?: ""
        deviceIdRequest = map[KEY_DEVICE_ID_REQUEST] ?: ""
        expired = map[KEY_EXPIRED] ?: ""
        status = map[KEY_STATUS] ?: ""
        notificationId = map[KEY_NOTIFICATION_ID]
        action = map[KEY_ACTION]
    }

    internal constructor(bundle: Bundle) {
        merchantAppId = bundle.getString(KEY_MERCHANT_APP_ID, "")
        deviceReceive = bundle.getString(KEY_DEVICE_RECEIVE, "")
        deviceRequest = bundle.getString(KEY_DEVICE_REQUEST, "")
        deviceIdReceive = bundle.getString(KEY_DEVICE_ID_RECEIVE, "")
        deviceIdRequest = bundle.getString(KEY_DEVICE_ID_REQUEST, "")
        expired = bundle.getString(KEY_EXPIRED, "")
        status = bundle.getString(KEY_STATUS) ?: ""
        notificationId = bundle.getString(KEY_NOTIFICATION_ID)
        action = bundle.getString(KEY_ACTION)
    }

    fun isNotNull(): Boolean =
        merchantAppId != ""
                && deviceReceive != ""
                && deviceRequest != ""
                && deviceIdReceive != ""
                && deviceIdRequest != ""
                && expired != ""
                && status != ""

    fun toMap(): Map<String, String?> = mapOf(
        KEY_MERCHANT_APP_ID to merchantAppId,
        KEY_DEVICE_RECEIVE to deviceReceive,
        KEY_DEVICE_REQUEST to deviceRequest,
        KEY_DEVICE_ID_RECEIVE to deviceIdReceive,
        KEY_DEVICE_ID_REQUEST to deviceIdRequest,
        KEY_EXPIRED to expired,
        KEY_STATUS to status,
        KEY_NOTIFICATION_ID to notificationId,
        KEY_ACTION to action
    )
}