package com.fazpass.android_trusted_device_v2

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.fazpass.android_trusted_device_v2.`object`.CrossDeviceRequest
import com.fazpass.android_trusted_device_v2.`object`.CrossDeviceRequestStream
import com.fazpass.android_trusted_device_v2.`object`.FazpassSettings

internal interface AndroidTrustedDevice {

    /**
     * Initializes everything.
     *
     * Required to be called once at the start of application, otherwise unexpected error may occur.
     * Put the public key in your assets folder, then fill the [publicKeyAssetName] with
     * your file's name (Example: "public_key.pub").
     *
     * @param context Activity context
     * @param publicKeyAssetName Your public key file name which you put in your src 'assets' folder. (Example: "public_key.pub")
     */
    fun init(context: Context, publicKeyAssetName: String)

    /**
     * Collects specific data according to settings and generate meta from it as Base64 string.
     *
     * You can use this meta to hit Fazpass API endpoint. Calling this method will automatically launch
     * local authentication (biometric / password). Any rules that have been set in method [Fazpass.setSettings]
     * will be applied according to the [accountIndex] parameter.
     *
     * @param activity Currently active fragment activity or app compat activity.
     * @param accountIndex Apply settings for this account index if settings have been set. Default to -1.
     * @param callback Will be invoked when meta is ready as Base64 string. If an error occurred, exception
     * won't be null and meta will be empty string.
     */
    fun generateMeta(activity: Activity, accountIndex: Int = -1, callback: (meta: String, exception: FazpassException?) -> Unit)

    /**
     * Generates new secret key for high level biometric settings.
     *
     * Before generating meta with "High Level Biometric" settings, You have to generate secret key first by
     * calling this method. This secret key will be invalidated when there is a new biometric enrolled or all
     * biometric is cleared, which makes your active fazpass id to get revoked when you hit Fazpass Check API
     * using meta generated with "High Level Biometric" settings. When secret key has been invalidated, you have
     * to call this method to generate new secret key and enroll your device with Fazpass Enroll API to make
     * your device trusted again.
     *
     * @throws Exception General exception when generating new secret key. Report this exception as a bug when
     * this method throws.
     */
    fun generateNewSecretKey(context: Context)

    /**
     * Sets rules for data collection in [Fazpass.generateMeta] method.
     *
     * Sets which sensitive information is collected in [Fazpass.generateMeta] method
     * and applies them according to [accountIndex] parameter. Accepts [FazpassSettings] for [settings]
     * parameter. Settings will be stored in SharedPreferences, so it will
     * not persist when application data is cleared / application is uninstalled. To delete
     * stored settings, pass null on [settings] parameter.
     *
     * @param context Activity context.
     * @param accountIndex Which account index to save settings into.
     * @param settings Settings that will be saved.
     */
    fun setSettings(context: Context, accountIndex: Int, settings: FazpassSettings?)

    /**
     * Retrieves the rules that has been set in [Fazpass.setSettings] method.
     *
     * @param accountIndex Which account index to get settings from.
     * @return stored [FazpassSettings] object based on the [accountIndex] parameter. Otherwise
     * null if there is no stored settings for this [accountIndex].
     */
    fun getSettings(accountIndex: Int) : FazpassSettings?

    /**
     * Retrieves the stream instance of cross device request.
     *
     * Before you listen to cross device login request stream, make sure these requirements
     * have been met:
     * - Device has been enrolled.
     * - Device is currently trusted (See Fazpass documentation for the definition of "trusted").
     * - Application is in "Logged In" state.
     *
     * @param context Activity context.
     */
    fun getCrossDeviceRequestStreamInstance(context: Context) : CrossDeviceRequestStream

    /**
     * Retrieves a [CrossDeviceRequest] object obtained from notification.
     *
     * @param intent Intent from getIntent() method of your first activity since app launch.
     * @return An instance of [CrossDeviceRequest] from cross device request notification.
     * Will be null if app isn't launched from the notification.
     */
    fun getCrossDeviceRequestFromNotification(intent: Intent?) : CrossDeviceRequest?
}