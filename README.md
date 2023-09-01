# android-trusted-device-v2

This is the Official android package for Fazpass Trusted Device V2.
For ios counterpart, you can find it here: https://github.com/fazpass-sdk/ios-trusted-device-v2 <br>
Visit [official website](https://fazpass.com) for more information about the product and see documentation at [online documentation](https://doc.fazpass.com) for more technical details.

## Minimum OS
Android 23

## Getting Started
Before using our product, make sure to contact us first to get keypair of public key and private key.
after you have each of them, put the public key into the assets folder.

1. Open android folder, then go to app/src/main/assets/ (if assets folder doesn't exist, create a new one)
2. Put the public key in this folder

This package main purpose is to generate meta which you can use to communicate with Fazpass rest API. But
before calling generate meta method, you have to initialize it first by calling this method:
```kotlin
Fazpass.instance.init(this, "YOUR_PUBLIC_KEY_NAME");
```

## Usage
```kotlin
Fazpass.instance.generateMeta(this) { meta, exception ->
    when (exception) {
        is BiometricAuthError -> TODO()
        is BiometricHardwareUnavailableError -> TODO()
        is BiometricNoHardwareError -> TODO()
        is BiometricNoneEnrolledError -> TODO()
        is BiometricSecurityUpdateRequiredError -> TODO()
        is BiometricUnsupportedError -> TODO()
        is EncryptionException -> TODO()
        is PublicKeyNotExistException -> TODO()
        is UninitializedException -> TODO()
        null -> { 
            print(meta) 
        }
    }
}
```