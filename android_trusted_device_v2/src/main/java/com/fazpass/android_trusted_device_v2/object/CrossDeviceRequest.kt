package com.fazpass.android_trusted_device_v2.`object`

import java.io.Serializable

class CrossDeviceRequest internal constructor(
    val data : Map<String, String>
) : Serializable {
    val merchantAppId : String = data["merchant_app_id"] as String
    val expired : Int = (data["expired"] as String).toInt()
    val deviceReceive : String = data["device_receive"] as String //vivo/user/MT6769V/CZ/Android 31
    val deviceRequest : String =
        data["device_request"] as String //google/userdebug/11th Gen Intel(R) Core(TM) i5-11400H @ 2.70GHz/Android 31
    val deviceIdReceive : String =
        data["device_id_receive"] as String //390614ec-a507-4a49-b987-5547ce874ce5
    val deviceIdRequest : String =
        data["device_id_request"] as String //42bb672c-8eef-48c8-b4e3-40617dcb7f41

}
