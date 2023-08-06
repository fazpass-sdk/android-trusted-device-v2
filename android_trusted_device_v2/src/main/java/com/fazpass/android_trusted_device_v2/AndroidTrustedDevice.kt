package com.fazpass.android_trusted_device_v2

import android.app.Activity
import android.content.Context

internal interface AndroidTrustedDevice {

    /**
     * Mandatory function which you have to call before calling any Fazpass function.
     * Not doing so might result in unexpected thrown exception. For your public & private keypair which you
     * get after contacting fazpass, put them in your assets folder. Then write their name with it's extension.
     * @param context Activity context
     * @param publicKeyAssetName Your public key file name which you put on your src 'assets' folder. (Example: "public_key.pub")
     * @param privateKeyAssetName Your private key file name which you put on your src 'assets' folder. (Example: "private_key.key")
     */
    fun init(context: Context, publicKeyAssetName: String, privateKeyAssetName: String)

    /**
     * Collect specific information and generate meta data from it as Base64 string.
     * You can use this meta to hit Fazpass API endpoint. Will launch biometric authentication before
     * generating meta. Result will be empty string if biometric authentication is failed or exception is present.
     * @param activity Currently active fragment activity or app compat activity
     * @param callback Will be invoked after meta is ready as Base64 string or exception is produced.
     */
    fun generateMeta(activity: Activity, callback: (String, Exception?) -> Unit)

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

    /**
     * This method will help you to collect fazpass id from fazpass trusted device rest APIs check and enroll. Simply
     * put the response in the [response] parameter.
     * @param response String response from check or enroll
     */
    fun getFazpassId(response: String) : String
}