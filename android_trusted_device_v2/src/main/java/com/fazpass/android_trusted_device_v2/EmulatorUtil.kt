package com.fazpass.android_trusted_device_v2

import android.opengl.GLES20
import android.os.Build
import android.os.Environment
import java.io.File

internal class EmulatorUtil {

    val isEmulator : Boolean
        get() {
            val simpleCheck = simpleCheckMethod()
            if (simpleCheck) {
                return true
            }
            return detailedCheckMethod()
        }

    private fun simpleCheckMethod() : Boolean {
        return ((Build.MANUFACTURER == "Google" && Build.BRAND == "google" &&
                ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                        && Build.FINGERPRINT.endsWith(":user/release-keys")
                        && Build.PRODUCT.startsWith("sdk_gphone_")
                        && Build.MODEL.startsWith("sdk_gphone_"))
                        //alternative
                        || (Build.FINGERPRINT.startsWith("google/sdk_gphone64_")
                        && (Build.FINGERPRINT.endsWith(":userdebug/dev-keys") || Build.FINGERPRINT.endsWith(":user/release-keys"))
                        && Build.PRODUCT.startsWith("sdk_gphone64_")
                        && Build.MODEL.startsWith("sdk_gphone64_"))))
                //
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                //bluestacks
                || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)
                //bluestacks
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HOST.startsWith("Build")
                //MSI App Player
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT == "google_sdk"
                // another Android SDK emulator check
                || SystemProperties.getProp("ro.kernel.qemu") == "1")
    }

    private fun detailedCheckMethod() : Boolean {
        var rating = 0

        if (Build.PRODUCT.contains("sdk") ||
            Build.PRODUCT.contains("Andy") ||
            Build.PRODUCT.contains("ttVM_Hdragon") ||
            Build.PRODUCT.contains("google_sdk") ||
            Build.PRODUCT.contains("Droid4X") ||
            Build.PRODUCT.contains("nox") ||
            Build.PRODUCT.contains("sdk_x86") ||
            Build.PRODUCT.contains("sdk_google") ||
            Build.PRODUCT.contains("vbox86p")
        ) {
            rating++
        }
        if (Build.MANUFACTURER == "unknown" || Build.MANUFACTURER == "Genymotion" ||
            Build.MANUFACTURER.contains("Andy") ||
            Build.MANUFACTURER.contains("MIT") ||
            Build.MANUFACTURER.contains("nox") ||
            Build.MANUFACTURER.contains("TiantianVM")
        ) {
            rating++
        }
        if (Build.BRAND == "generic" || Build.BRAND == "generic_x86" || Build.BRAND == "TTVM" ||
            Build.BRAND.contains("Andy")
        ) {
            rating++
        }
        if (Build.DEVICE.contains("generic") ||
            Build.DEVICE.contains("generic_x86") ||
            Build.DEVICE.contains("Andy") ||
            Build.DEVICE.contains("ttVM_Hdragon") ||
            Build.DEVICE.contains("Droid4X") ||
            Build.DEVICE.contains("nox") ||
            Build.DEVICE.contains("generic_x86_64") ||
            Build.DEVICE.contains("vbox86p")
        ) {
            rating++
        }
        if (Build.MODEL == "sdk" || Build.MODEL == "google_sdk" ||
            Build.MODEL.contains("Droid4X") ||
            Build.MODEL.contains("TiantianVM") ||
            Build.MODEL.contains("Andy") || Build.MODEL == "Android SDK built for x86_64" || Build.MODEL == "Android SDK built for x86"
        ) {
            rating++
        }
        if (Build.HARDWARE == "goldfish" || Build.HARDWARE == "vbox86" ||
            Build.HARDWARE.contains("nox") ||
            Build.HARDWARE.contains("ttVM_x86")
        ) {
            rating++
        }
        if (Build.FINGERPRINT.contains("generic/sdk/generic") ||
            Build.FINGERPRINT.contains("generic_x86/sdk_x86/generic_x86") ||
            Build.FINGERPRINT.contains("Andy") ||
            Build.FINGERPRINT.contains("ttVM_Hdragon") ||
            Build.FINGERPRINT.contains("generic_x86_64") ||
            Build.FINGERPRINT.contains("generic/google_sdk/generic") ||
            Build.FINGERPRINT.contains("vbox86p") ||
            Build.FINGERPRINT.contains("generic/vbox86p/vbox86p")
        ) {
            rating++
        }
        try {
            val opengl = GLES20.glGetString(GLES20.GL_RENDERER)
            if (opengl != null) {
                if (opengl.contains("Bluestacks") ||
                    opengl.contains("Translator")
                ) rating += 10
            }
        } catch (e: Exception) {
            if (Fazpass.IS_DEBUG) e.printStackTrace()
        }
        try {
            val sharedFolder = File(
                Environment
                    .getExternalStorageDirectory().toString()
                        + File.separatorChar
                        + "windows"
                        + File.separatorChar
                        + "BstSharedFolder"
            )
            if (sharedFolder.exists()) {
                rating += 10
            }
        } catch (e: Exception) {
            if (Fazpass.IS_DEBUG) e.printStackTrace()
        }

        return rating > 3
    }
}