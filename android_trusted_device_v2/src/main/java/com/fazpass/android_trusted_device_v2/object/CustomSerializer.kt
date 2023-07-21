package com.fazpass.android_trusted_device_v2.`object`

open class CustomSerializer {

    var result = ""
        private set

    fun writeStartObject() {
        result += "{"
    }

    fun writeEndObject() {
        finalize()
        result += "},"
    }

    fun writeStringField(field: String, value: String) {
        result += "\"$field\":\"$value\","
    }

    fun writeBooleanField(field: String, value: Boolean) {
        result += "\"$field\":$value,"
    }

    fun writeArrayFieldStart(field: String) {
        result += "\"$field\":["
    }

    fun writeString(value: String) {
        result += "\"$value\","
    }

    fun writeEndArray() {
        finalize()
        result += "],"
    }

    fun writeObjectFieldStart(field: String) {
        result += "\"$field\":{"
    }

    fun finalize() {
        if (result.endsWith(",")) {
            result = result.substring(0, result.length-1)
        }
    }
}