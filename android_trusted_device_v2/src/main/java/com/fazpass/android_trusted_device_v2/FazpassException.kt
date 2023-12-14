package com.fazpass.android_trusted_device_v2

sealed class FazpassException(e: Exception) : Exception(e) {
    constructor(message: String?) : this(Exception(message))
}

// GENERAL ERRORS
class UninitializedException
    : FazpassException("Fazpass init has to be called first!")

class PublicKeyNotExistException(name: String)
    : FazpassException("'$name' file doesn't exist in the assets directory!")

class EncryptionException(val e: Exception)
    : FazpassException(e)

// BIOMETRIC ERRORS
class BiometricNoneEnrolledError
    : FazpassException("User can't authenticate because no biometric " +
        "or device credential is enrolled.")

class BiometricUnavailableError
    : FazpassException("User can't authenticate because there is " +
        "no suitable hardware (e.g. no biometric sensor or no keyguard) or the hardware is unavailable.")

class BiometricSecurityUpdateRequiredError
    : FazpassException("User can't authenticate because a security vulnerability has " +
        "been discovered with one or more hardware sensors. The affected sensor(s) are " +
        "unavailable until a security update has addressed the issue.")

class BiometricUnsupportedError
    : FazpassException("User can't authenticate because the specified options are " +
        "incompatible with the current Android version.")

class BiometricAuthError(message: String?)
    : FazpassException(message)