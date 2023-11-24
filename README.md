# android-trusted-device-v2

This is the Official android package for Fazpass Trusted Device V2.
For ios counterpart, you can find it here: https://github.com/fazpass-sdk/ios-trusted-device-v2 <br>
Visit [official website](https://fazpass.com) for more information about the product and see documentation at [online documentation](https://doc.fazpass.com) for more technical details.

## Minimum OS

Android 23

## installation

### 1. Add Jitpack repo in your gradle repositories

If your gradle use buildscript syntax, add Jitpack repo in your root-level build.gradle (*project*/build.gradle):
```gradle
buildscript {
	//...
}

allprojects {
	repositories {
		// Another repo...
		maven { url 'https://jitpack.io' }
	}
}
```
If your gradle use plugin syntax, add Jitpack repo in your settings.gradle (*project*/settings.gradle):
```gradle
pluginManagement {
    repositories {
        // Another repo...
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Another repo...
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Add the dependency in your app-level build.gradle (*project*/app/build.gradle)

```gradle
dependencies {
	// Another dependencies...
	implementation 'com.github.fazpass-sdk:android-trusted-device-v2:Tag'
}
```

### 3. Sync project with gradle files

## Getting Started

Before using our product, make sure to contact us first to get keypair of public key and private key.
after you have each of them, put the public key into the assets folder.

1. Open android folder, then go to app/src/main/assets/ (if assets folder doesn't exist, create a new one)
2. Put the public key in this folder

This package main purpose is to generate meta which you can use to communicate with Fazpass rest API. But
before calling generate meta method, you have to initialize it first by calling this method:
```kotlin
Fazpass.instance.init(this, "YOUR_PUBLIC_KEY_ASSET_NAME")
```

## Usage

Call `generateMeta(activity: Activity, callback: (String, FazpassException?) -> Unit)` method to generate meta. This method
collects specific information and generates meta data as Base64 string.
You can use this meta to hit Fazpass API endpoint. **Will launch biometric authentication before
generating meta**. Meta will be empty string if exception is present.
```kotlin
Fazpass.instance.generateMeta(this) { meta, exception ->
    when (exception) {
        is BiometricAuthError -> TODO()
        is BiometricUnavailableError -> TODO()
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

## Exceptions

#### UninitializedException

Produced when fazpass init method hasn't been called once.

#### PublicKeyNotExistException

Produced when public key with the name registered in init method doesn't exist in the assets directory.

#### EncryptionException

Produced when encryption went wrong because you used the wrong public key.

#### BiometricAuthError

Produced when biometric authentication is finished with an error. (example: User cancelled biometric auth, User failed biometric auth too many times, and many more).

#### BiometricUnavailableError

Produced when android can't start biometric authentication because there is no suitable hardware (e.g. no biometric sensor or no keyguard) or the hardware is unavailable.

#### BiometricNoneEnrolledError

Produced when android can't start biometric authentication because there is no biometric (e.g. Fingerprint, Face, Iris) or device credential (e.g. PIN, Password, Pattern) enrolled.

#### BiometricSecurityUpdateRequiredError

Produced when android can't start biometric authentication because a security vulnerability has been discovered with one or
more hardware sensors. The affected sensor(s) are unavailable until a security update has addressed the issue.

#### BiometricUnsupportedError

Produced when android can't start biometric authentication because the specified options are incompatible with the current Android version.

## Data Collection

Data collected and stored in generated meta. Based on data sensitivity, data type is divided into two: General data and Sensitive data.
General data is always collected while Sensitive data requires you to enable it and user to grant the required permission before it can be collected.

To enable Sensitive data collection, after calling fazpass init method, you need to call `enableSelected(vararg sensitiveData: SensitiveData)` method and
specifies which sensitive data you want to collect.
```kotlin
Fazpass.instance.enableSelected(
    SensitiveData.location,
    SensitiveData.simNumbersAndOperators
)
```
After enabling specified Sensitive data, you have to ask for user permissions before you call generate meta method. Specified Sensitive data will be collected
if user granted the required permissions, otherwise it won't be collected and no error will be produced.

### General data collected

* Your device platform name (Value will always be "android").
* Your app package name.
* Your app debug status.
* Your device rooted status.
* Your device emulator status.
* Your app cloned status.
* Your device mirroring or projecting status.
* Your app signatures.
* Your device information (Android version, phone brand, phone type, phone cpu).
* Your network IP Address and vpn status.

### Sensitive data collected

#### Your device location and mock location status

Required Permissions:
* android.permission.ACCESS_COARSE_LOCATION or android.permission.ACCESS_FINE_LOCATION
* android.permission.FOREGROUND_SERVICE

After you enabled location data collection and user has granted the location permission, you have to
make sure the user has enabled their location/gps settings before you call generateMeta() method.

#### Your device SIM numbers and operators (if available)

Required Permissions:
* android.permission.READ_PHONE_NUMBERS
* android.permission.READ_PHONE_STATE
