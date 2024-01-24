package com.fazpass.android_trusted_device_v2.`object`

import android.os.Bundle
import java.io.Serializable
import com.fazpass.android_trusted_device_v2.Fazpass

/**
 * An object containing data from cross device notification request.
 *
 * This object is only used as data retrieved from [Fazpass.getCrossDeviceRequestStreamInstance]
 * and [Fazpass.getCrossDeviceRequestFromNotification].
 */
class CrossDeviceRequest : Serializable {
    val merchantAppId : String
    val expired : Int // 300
    val deviceReceive : String //vivo/user/MT6769V/CZ/Android 31
    val deviceRequest : String //google/userdebug/11th Gen Intel(R) Core(TM) i5-11400H @ 2.70GHz/Android 31
    val deviceIdReceive : String //390614ec-a507-4a49-b987-5547ce874ce5
    val deviceIdRequest : String //42bb672c-8eef-48c8-b4e3-40617dcb7f41

    internal constructor(data : Map<String, String>) {
        merchantAppId = data["merchant_app_id"] as String
        expired = (data["expired"] as String).toInt()
        deviceReceive = data["device_receive"] as String
        deviceRequest = data["device_request"] as String
        deviceIdReceive = data["device_id_receive"] as String
        deviceIdRequest = data["device_id_request"] as String
    }

    internal constructor(bundle: Bundle) {
        merchantAppId = bundle.getString("merchant_app_id") ?: ""
        expired = bundle.getString("expired")?.toInt() ?: -1
        deviceReceive = bundle.getString("device_receive") ?: ""
        deviceRequest = bundle.getString("device_request") ?: ""
        deviceIdReceive = bundle.getString("device_id_receive") ?: ""
        deviceIdRequest = bundle.getString("device_id_request") ?: ""
    }
}
