package com.fazpass.android_trusted_device_v2

/** Enums of exception to be generated when an error occurs in the [Fazpass.generateMeta] method. */
enum class FazpassException(var exception: Exception) {

    /** Thrown when [Fazpass.init] method hasn't been called once. */
    UninitializedException(Exception("Fazpass init has to be called first!")),

    /** Thrown when public key doesn't exist in the src/assets folder */
    PublicKeyNotExistException(Exception()),

    /** Thrown to indicate that encryption process is failed. Likely because you used the wrong public key. */
    EncryptionException(Exception()),

    /** Thrown when device hasn't set a password / enrolled a biometric information. */
    BiometricNoneEnrolledError(Exception("User can't authenticate because no biometric " +
            "or device credential is enrolled.")),

    /** Thrown to indicate that biometric is not available at the moment. */
    BiometricUnavailableError(
        Exception("User can't authenticate because there is " +
                "no suitable hardware (e.g. no biometric sensor or no keyguard) or the hardware is unavailable.")
    ),

    /** Thrown when there is a major security update for user's device and user is required to update immediately. */
    BiometricSecurityUpdateRequiredError(
        Exception("User can't authenticate because a security vulnerability has " +
                "been discovered with one or more hardware sensors. The affected sensor(s) are " +
                "unavailable until a security update has addressed the issue.")
    ),

    /** Thrown when biometric is not supported by the device. */
    BiometricUnsupportedError(
        Exception("User can't authenticate because the specified options are " +
                "incompatible with the current Android version.")
    ),

    /** Thrown when local authentication is cancelled by user. */
    BiometricAuthError(Exception());

    internal fun setException(exception: Exception) {
        this.exception = exception
    }
}