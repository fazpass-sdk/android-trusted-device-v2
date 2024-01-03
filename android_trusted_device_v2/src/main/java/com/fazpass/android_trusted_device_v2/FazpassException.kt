package com.fazpass.android_trusted_device_v2

enum class FazpassException(var exception: Exception) {
    // GENERAL ERRORS
    UninitializedException(Exception("Fazpass init has to be called first!")),
    PublicKeyNotExistException(Exception()),
    EncryptionException(Exception()),
    // BIOMETRIC ERRORS
    BiometricNoneEnrolledError(Exception("User can't authenticate because no biometric " +
            "or device credential is enrolled.")),
    BiometricUnavailableError(
        Exception("User can't authenticate because there is " +
                "no suitable hardware (e.g. no biometric sensor or no keyguard) or the hardware is unavailable.")
    ),
    BiometricSecurityUpdateRequiredError(
        Exception("User can't authenticate because a security vulnerability has " +
                "been discovered with one or more hardware sensors. The affected sensor(s) are " +
                "unavailable until a security update has addressed the issue.")
    ),
    BiometricUnsupportedError(
        Exception("User can't authenticate because the specified options are " +
                "incompatible with the current Android version.")
    ),
    BiometricAuthError(Exception());

    internal fun setException(exception: Exception) {
        this.exception = exception
    }
}