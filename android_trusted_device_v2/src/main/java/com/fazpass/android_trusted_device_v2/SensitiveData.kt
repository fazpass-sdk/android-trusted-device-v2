package com.fazpass.android_trusted_device_v2

/**
 * Enum class which contains all kinds of data that requires certain permissions before it
 * could be collected.
 */
enum class SensitiveData {
    /**
     * REQUIRED PERMISSIONS:
     * - android.permission.ACCESS_COARSE_LOCATION or android.permission.ACCESS_FINE_LOCATION
     * - android.permission.FOREGROUND_SERVICE
     *
     * After you enabled location data collection and user has granted the location permission, you have to
     * make sure the user has enabled their location/gps settings before you call generateMeta() method.
     */
    location,
    /**
     * REQUIRED PERMISSIONS:
     * - android.permission.READ_PHONE_NUMBERS
     * - android.permission.READ_PHONE_STATE
     */
    simNumbersAndOperators,
}