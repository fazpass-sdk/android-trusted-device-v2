package com.fazpass.android_trusted_device_v2.`object`

import java.io.Serializable

class CrossDeviceData internal constructor(
    val notificationId : String,
    val device : String,
    val location : String
) : Serializable
