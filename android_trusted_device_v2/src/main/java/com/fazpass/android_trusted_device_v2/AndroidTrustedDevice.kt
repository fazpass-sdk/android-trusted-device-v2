package com.fazpass.android_trusted_device_v2

import android.app.Activity
import android.content.Context
import com.fazpass.android_trusted_device_v2.`object`.CrossDeviceData

internal interface AndroidTrustedDevice {

    /**
     * Mandatory function which you have to call before calling any Fazpass function.
     * Not doing so might result in unexpected thrown exception.
     * Call this method in onCreate method inside your default activity.
     * For your public & private keypair which you
     * get after contacting fazpass, put them in your assets folder. Then write their name with it's extension.
     * @param context Activity context
     * @param publicKeyAssetName Your public key file name which you put on your src 'assets' folder. (Example: "public_key.pub")
     */
    fun init(context: Context, publicKeyAssetName: String)

    /**
     * Collect specific information and generate meta data from it as Base64 string.
     * You can use this meta to hit Fazpass API endpoint. Will launch biometric authentication before
     * generating meta. Result will be empty string if exception is present.
     * @param activity Currently active fragment activity or app compat activity.
     * @param callback Will be invoked when meta is ready as Base64 string or exception is produced.
     */
    fun generateMeta(activity: Activity, callback: (String, FazpassException?) -> Unit)

    /**
     * Sensitive data requires the user to grant certain permissions so it could be collected.
     * All sensitive data collection is disabled by default, which means you have to enable each of
     * them manually. Before enabling any sensitive data collection, however, you have to request
     * the required permissions first. Until their required permissions is granted, sensitive data won't
     * be collected even if they have been enabled. Required permissions for each sensitive data has been
     * listed in [SensitiveData] member's documentation.
     * @param sensitiveData Any sensitive data you want to enable
     */
    fun enableSelected(vararg sensitiveData: SensitiveData)

    fun startListener(context: Context, callback: (data: CrossDeviceData) -> Unit)

    fun stopListener(context: Context)
}