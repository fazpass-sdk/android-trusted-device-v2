package com.fazpass.android_trusted_device_v2

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest

@Suppress("DEPRECATION")
@SuppressLint("PackageManagerGetSignatures")
internal class AppSignatureUtil {

    companion object {

        fun getSignatures(context: Context) : List<String> {
            return try {
                val packageManager = context.packageManager

                val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val signingInfo = packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo

                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                } else {
                    packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
                }

                if (AndroidTrustedDevice.IS_DEBUG) {
                    Log.i(
                        "META-ORI-SIGNATURES",
                        signatures.map { it.toCharsString() }.toList().toString()
                    )
                }

                signatures.map {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(it.toByteArray())
                    Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                }
            } catch (e: Exception) {
                if (AndroidTrustedDevice.IS_DEBUG) e.printStackTrace()
                listOf()
            }
        }
    }
}