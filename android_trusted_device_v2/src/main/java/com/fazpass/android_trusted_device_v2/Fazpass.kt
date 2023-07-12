package com.fazpass.android_trusted_device_v2

import android.content.Context

interface Fazpass {

    companion object {
        val instance : Fazpass by lazy { AndroidTrustedDevice() }
    }

    /**
     * Mandatory function which you have to call before calling any Fazpass function.
     * Not doing so might result in unexpected thrown exception.
     * @param context Activity context
     * @param keyAssetName Your *.pub public key file name which you put on your src 'assets' folder.
     * Assuming your public key file name is 'my_public_key.pub', then write the name as it is
     */
    fun init(context: Context, keyAssetName: String)

    /**
     * Collect specific information and generate meta data from it as Base64 string.
     * You can use this meta to hit Fazpass API endpoint.
     * @param context Activity context
     * @param callback Will be invoked after meta is ready as Base64 string
     */
    fun generateMeta(context: Context, callback: (String) -> Unit)

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
}