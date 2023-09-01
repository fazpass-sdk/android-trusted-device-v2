package com.fazpass.android_trusted_device_v2

sealed class FazpassException(message: String?) : Exception(message)

// GENERAL ERRORS
class UninitializedException
    : FazpassException("Fazpass init has to be called first!")

class PublicKeyNotExistException(name: String)
    : FazpassException("'$name' file doesn't exist in the assets directory!")

class EncryptionException
    : FazpassException("Something went wrong when trying to encrypt data. " +
        "Did you use the correct public key file?")

// BIOMETRIC ERRORS
class BiometricNoneEnrolledError
    : FazpassException("User can't authenticate because no biometric " +
        "or device credential is enrolled.")

class BiometricHardwareUnavailableError
    : FazpassException("User can't authenticate because the hardware is unavailable. " +
        "Try again later.")

class BiometricNoHardwareError
    : FazpassException("User can't authenticate because there is no suitable hardware")

class BiometricSecurityUpdateRequiredError
    : FazpassException("User can't authenticate because a security vulnerability has " +
        "been discovered with one or more hardware sensors. The affected sensor(s) are " +
        "unavailable until a security update has addressed the issue.")

class BiometricUnsupportedError
    : FazpassException("User can't authenticate because the specified options are " +
        "incompatible with the current Android version.")

class BiometricAuthError(message: String?)
    : FazpassException(message)