package com.fazpass.android_trusted_device_v2.`object`

data class MetaData(
    val platform : String,
    val isRooted : Boolean,
    val isEmulator : Boolean,
    val isVpn : Boolean,
    val isCloned : Boolean,
    val isScreenMirroring : Boolean,
    val isDebuggable : Boolean,
    val signatures : List<String>?,
    val deviceInfo : DeviceInfo,
    val simNumbers : List<String>,
    val coordinate : Coordinate,
    val isMockLocation : Boolean,
    val packageName : String,
)
