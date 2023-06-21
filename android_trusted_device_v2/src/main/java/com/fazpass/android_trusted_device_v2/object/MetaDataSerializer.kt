package com.fazpass.android_trusted_device_v2.`object`

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class MetaDataSerializer @JvmOverloads constructor(t: Class<MetaData>? = null)
    : StdSerializer<MetaData>(t) {

    override fun serialize(value: MetaData?, gen: JsonGenerator?, provider: SerializerProvider?) {
        if (value != null && gen != null && provider != null) {
            gen.apply {
                writeStartObject()

                writeStringField("platform", value.platform)
                writeBooleanField("is_rooted", value.isRooted)
                writeBooleanField("is_emulator", value.isEmulator)
                writeBooleanField("is_gps_spoof", value.isMockLocation)

                writeArrayFieldStart("signature")
                value.signatures?.forEach { writeString(it) }
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

                writeObjectFieldStart("geolocation")
                writeStringField("lat", value.coordinate.lat.toString())
                writeStringField("lng", value.coordinate.lng.toString())
                writeEndObject()

                writeEndObject()
            }
        }
    }
}