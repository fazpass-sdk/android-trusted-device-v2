package com.fazpass.android_trusted_device_v2

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.fazpass.android_trusted_device_v2.`object`.CrossDeviceRequest
import com.fazpass.android_trusted_device_v2.`object`.CrossDeviceRequestStream
import com.fazpass.android_trusted_device_v2.`object`.FazpassSettings

internal interface AndroidTrustedDevice {

    /**
     * Mandatory function which you have to call before calling any Fazpass function.
     * Not doing so might result in unexpected thrown exception.
     * Call this method in onCreate method inside your default activity.
     * For your public & private keypair which you get after contacting fazpass,
     * put the public key in your assets folder. Then write their name with it's extension.
     * @param context Activity context
     * @param publicKeyAssetName Your public key file name which you put in your src 'assets' folder. (Example: "public_key.pub")
     */
    fun init(context: Context, publicKeyAssetName: String)

    /**
     * Collect specific information and generate meta data from it as Base64 string.
     * You can use this meta to hit Fazpass API endpoint. Will launch biometric authentication before
     * generating meta. Will apply settings that have been set in method [setSettingsForAccountIndex].
     * Result will be empty string if exception is present.
     * @param activity Currently active fragment activity or app compat activity.
     * @param accountIndex Apply settings for this account index if settings have been set. Default to -1.
     * @param callback Will be invoked when meta is ready as Base64 string or exception is produced.
     */
    fun generateMeta(activity: Activity, accountIndex: Int = -1, callback: (String, FazpassException?) -> Unit)

    fun generateSecretKeyForHighLevelBiometric(context: Context)

    /**
     * This method will help you to control how meta is generated in [generateMeta] method depending on which
     * account index is selected. Settings will be saved in fazpass shared preferences and will be loaded again
     * when [init] method is called. Passing null in [settings] argument will delete it.
     *
     * Notes: All saved settings WILL be deleted when user cleared app's data or uninstalled the app.
     *
     * @param context Activity context.
     * @param accountIndex Which account index to save settings into.
     * @param settings Settings that will be saved.
     */
    fun setSettingsForAccountIndex(context: Context, accountIndex: Int, settings: FazpassSettings?)

    /**
     * Get saved settings that has been set in [setSettingsForAccountIndex] method.
     *
     * @param accountIndex Which account index to get settings from.
     * @return [FazpassSettings] instance of that [accountIndex] if it has been set. Returns null otherwise.
     */
    fun getSettingsForAccountIndex(accountIndex: Int) : FazpassSettings?

    /**
     * To start listening to cross device login request, you have to call this method to get the stream instance,
     * then call the listen(callback) method. To stop listening, call the close() method.
     *
     * Before you listen to cross device login request, make sure these requirements
     * have been met:
     * - Device has been enrolled
     * - Device is trusted
     * - Device fazpass_id is still active
     * - Application is in "Logged In" state
     *
     * @param context Activity context.
     */
    fun getCrossDeviceRequestStreamInstance(context: Context) : CrossDeviceRequestStream

    /**
     * To get the cross device login request when user launched the application from notification,
     * you have to call this method to get the request and use the first activity's intent as an argument.
     *
     * @param intent Intent from getIntent() method of your first activity since app launch.
     * @return An instance of [CrossDeviceRequest] from cross device request notification.
     * Will be null if app isn't launched from the notification.
     */
    fun getCrossDeviceRequestFromNotification(intent: Intent?) : CrossDeviceRequest?
}