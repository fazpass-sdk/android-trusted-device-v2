package com.fazpass.android_trusted_device_v2.`object`

class MetaDataSerializer(value: MetaData) : CustomSerializer() {

    /**
     * JSON RESULT:
     * {
     *  "platform":"android/ios",
     *  "is_rooted":true/false,
     *  "is_emulator":true/false,
     *  "is_gps_spoof":true/false,
     *  "signature":["signature_key","signature_key"],
     *  "is_vpn":true/false,
     *  "is_clone_app":true/false,
     *  "is_screen_sharing":true/false,
     *  "is_debug":true/false,
     *  "application":"com.fazpass.android_trusted_device_v2_app"
     *  "device_id":{
     *      "name":"Samsung",
     *      "os_version":"Q",
     *      "series":"A30",
     *      "cpu":"Mediatek"
     *  },
     *  "sim_serial":["abcde12345","zyxwv9875"],
     *  "sim_operator":["indosat","tsel"],
     *  "geolocation":{"lat":"2.90887363", "lng":"4.9099876"},
     *  "client_ip":"127.0.0.1"
     * }
     */
    init {
        writeStartObject()

        writeStringField("platform", value.platform)
        writeBooleanField("is_rooted", value.isRooted)
        writeBooleanField("is_emulator", value.isEmulator)
        writeBooleanField("is_gps_spoof", value.isMockLocation)

        writeArrayFieldStart("signature")
        value.signatures.forEach { writeString(it) }
        writeEndArray()

        writeBooleanField("is_vpn", value.isVpn)
        writeBooleanField("is_clone_app", value.isCloned)
        writeBooleanField("is_screen_sharing", value.isScreenMirroring)
        writeBooleanField("is_debug", value.isDebuggable)
        writeStringField("application", value.packageName)

        writeObjectFieldStart("device_id")
        writeStringField("name", value.deviceInfo.brand)
        writeStringField("os_version", value.deviceInfo.os)
        writeStringField("series", value.deviceInfo.type)
        writeStringField("cpu", value.deviceInfo.cpu)
        writeEndObject()

        writeArrayFieldStart("sim_serial")
        value.simNumbers.forEach { writeString(it) }
        writeEndArray()

        writeArrayFieldStart("sim_operator")
        value.simOperators.forEach { writeString(it) }
        writeEndArray()

        writeObjectFieldStart("geolocation")
        writeStringField("lat", value.coordinate.lat.toString())
        writeStringField("lng", value.coordinate.lng.toString())
        writeEndObject()

        writeStringField("client_ip", value.ipAddress)

        writeEndObject()

        finalize()
    }
}