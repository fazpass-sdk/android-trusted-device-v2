package com.fazpass.android_trusted_device_v2

/**
 * Sensitive data requires the user to grant certain permissions so they could be collected.
 * All sensitive data collection is disabled by default, which means you have to enable each of
 * them manually. Until their required permissions are granted, sensitive data won't
 * be collected even if they have been enabled. Required permissions for each sensitive data have been
 * listed in [SensitiveData] member's documentation.
 */
enum class SensitiveData {
    /**
     * REQUIRED PERMISSIONS:
     * - android.permission.ACCESS_COARSE_LOCATION or android.permission.ACCESS_FINE_LOCATION
     * - android.permission.FOREGROUND_SERVICE
     *
     * After you enabled location data collection and user have granted the location permission, you have to
     * make sure the user have enabled their location/gps settings before you call generateMeta() method.
     */
    location,
    /**
     * REQUIRED PERMISSIONS:
     * - android.permission.READ_PHONE_NUMBERS
     * - android.permission.READ_PHONE_STATE
     */
    simNumbersAndOperators,
}