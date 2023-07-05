package com.fazpass.android_trusted_device_v2

import android.app.Activity
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
     * @param context General context
     * @param callback Will be invoked after meta is ready as Base64 string
     */
    fun generateMeta(context: Context, callback: (String) -> Unit)

    /**
     * Request every permissions needed to collect meta data as accurately as possible. This is completely optional,
     * so calling generateMeta() without calling this method first won't throw any exception.
     * @param activity your activity which to request every permissions at
     */
    fun requestPermissions(activity: Activity)
}