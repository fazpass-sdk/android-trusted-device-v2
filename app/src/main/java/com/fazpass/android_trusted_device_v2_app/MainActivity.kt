package com.fazpass.android_trusted_device_v2_app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.JsonReader
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fazpass.android_trusted_device_v2.Fazpass
import com.fazpass.android_trusted_device_v2.FazpassHelper
import com.fazpass.android_trusted_device_v2.SensitiveData
import com.fazpass.android_trusted_device_v2.`object`.FazpassSettings
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PUBLIC_KEY_ASSET_FILENAME = "new-public-key.pub"
        //"staging-public_key.pub"
        //"my_public_key.pub"
        private const val PRIVATE_KEY_ASSET_FILENAME = "staging-private_key.key"
        //"staging-private_key.key"
        //"my_private_key.key"
        private const val MERCHANT_APP_ID = "afb2c34a-4c4f-4188-9921-5c17d81a3b3d"
        //"afb2c34a-4c4f-4188-9921-5c17d81a3b3d"
        //"e30e8ae2-1557-46f6-ba3a-755b57ce4c44"
        const val BEARER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozNn0.mfny8amysdJQYlCrUlYeA-u4EG1Dw9_nwotOl-0XuQ8"
        //"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozNn0.mfny8amysdJQYlCrUlYeA-u4EG1Dw9_nwotOl-0XuQ8"
        //"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjo0fQ.WEV3bCizw9U_hxRC6DxHOzZthuJXRE8ziI3b6bHUpEI"
    }

    private lateinit var infoView: LinearLayout

    private var meta: String? = null
    private var fazpassIdIsShown = false

    private var fazpassId: String? = null
    private var challenge: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fazpass.instance.init(this, PUBLIC_KEY_ASSET_FILENAME)

        Log.i("APP SIGNATURES", Fazpass.helper.getAppSignatures(this).toString())

        infoView = findViewById(R.id.ma_info_view)

        val settingsBtn = findViewById<Button>(R.id.ma_settings_btn)
        settingsBtn.setOnClickListener {
            // load fazpass settings
            val fazpassSettings = Fazpass.instance.getSettings(-1)
            val oldSettings = if (fazpassSettings != null) booleanArrayOf(
                fazpassSettings.sensitiveData.contains(SensitiveData.location),
                fazpassSettings.sensitiveData.contains(SensitiveData.simNumbersAndOperators),
                fazpassSettings.isBiometricLevelHigh
            ) else null
            val newSettings = arrayOf<Boolean?>(null, null, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle("Settings")
                .setMultiChoiceItems(
                    arrayOf(
                        "Enable location",
                        "Enable Sim Information",
                        "High Biometric Level"),
                    oldSettings) { _, i, bool ->
                    newSettings[i] = bool
                }
                .setPositiveButton("Save") { dialog, _ ->
                    val newFazpassSettings = FazpassSettings.Builder()

                    for (i in newSettings.indices) {
                        if (newSettings[i] == null) {
                            newSettings[i] = oldSettings?.get(i) ?: false
                        }
                    }

                    if (newSettings[0]!!) {
                        newFazpassSettings.enableSelectedSensitiveData(SensitiveData.location)
                    } else {
                        newFazpassSettings.disableSelectedSensitiveData(SensitiveData.location)
                    }

                    if (newSettings[1]!!) {
                        newFazpassSettings.enableSelectedSensitiveData(SensitiveData.simNumbersAndOperators)
                    } else {
                        newFazpassSettings.disableSelectedSensitiveData(SensitiveData.simNumbersAndOperators)
                    }

                    if (newSettings[2]!!) {
                        Fazpass.instance.generateNewSecretKey(this)
                        newFazpassSettings.setBiometricLevelToHigh()
                    } else {
                        newFazpassSettings.setBiometricLevelToLow()
                    }

                    Fazpass.instance.setSettings(this, -1, newFazpassSettings.build())

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .create()
            dialog.show()
        }

        val genMetaBtn = findViewById<Button>(R.id.ma_genmeta_btn)
        genMetaBtn.setOnClickListener {
            infoView.removeAllViews()
            fazpassIdIsShown = false

            try {
                Fazpass.instance.generateMeta(this) { meta, fazpassException ->
                    when (fazpassException) {
                        null -> onMetaGenerated(meta)
                        else -> onErrorOccurred(fazpassException.exception)
                    }
                }
            } catch (e: Exception) {
                onErrorOccurred(e)
            }
        }

        requestPermissions()
    }

    private fun onErrorOccurred(e: Exception) {
        infoView.addView(EntryView(this).apply {
            name = "Error"
            value = "${e.javaClass.name}\n" +
                    "${e.message}\n" +
                    e.stackTraceToString()
        })

        infoView.addView(TextView(this).apply {
            setTextColor(Color.RED)
            text = "*Press 'Generate Meta' button to reset"
        })
    }

    private fun onMetaGenerated(meta: String) {
        this.meta = meta

        runOnUiThread {
            infoView.addView(EntryView(this).apply {
                name = "Generated Meta"
                value = meta
            })

            infoView.addView(checkBtn)
        }
    }

    private fun onCheckResponse(response: String, status: Boolean) {
        infoView.removeView(checkBtn)

        infoView.addView(EntryView(this).apply {
            name = "Check Response"
            value = response
        })

        if (!status) return

        readDataFromResponse(response)
        if (fazpassId?.isNotBlank() == true) {
            infoView.addView(EntryView(this).apply {
                name = "Fazpass ID"
                value = fazpassId!!
            })
            fazpassIdIsShown = true
        }
        if (challenge.isNotBlank()) {
            infoView.addView(EntryView(this).apply {
                name = "Challenge"
                value = challenge
            })
        }

        infoView.addView(actionBtn)
    }

    private fun onEnrollResponse(response: String, status: Boolean) {
        infoView.removeViewAt(infoView.childCount-1)

        infoView.addView(EntryView(this).apply {
            name = "Enroll Response"
            value = response
        })

        if (status) {
            readDataFromResponse(response)
            if (!fazpassIdIsShown && fazpassId?.isNotBlank() == true) {
                infoView.addView(EntryView(this).apply {
                    name = "Fazpass ID"
                    value = fazpassId!!
                })
                fazpassIdIsShown = true
            }
        }

        infoView.addView(TextView(this).apply {
            setTextColor(Color.RED)
            text = "*Press 'Generate Meta' button to reset"
        })
    }

    private fun onValidateResponse(response: String, status: Boolean) {
        infoView.removeViewAt(infoView.childCount-1)

        infoView.addView(EntryView(this).apply {
            name = "Validate Response"
            value = response
        })

        infoView.addView(TextView(this).apply {
            setTextColor(Color.RED)
            text = "*Press 'Generate Meta' button to reset"
        })
    }

    private fun onRemoveResponse(response: String, status: Boolean) {
        infoView.removeViewAt(infoView.childCount-1)

        infoView.addView(EntryView(this).apply {
            name = "Remove Response"
            value = response
        })

        infoView.addView(TextView(this).apply {
            setTextColor(Color.RED)
            text = "*Press 'Generate Meta' button to reset"
        })
    }

    private val checkBtn: Button
        get() = Button(this).apply {
            text = "Check"
            setOnClickListener {
                val data = """{
                    "merchant_app_id": "$MERCHANT_APP_ID",
                    "meta": "$meta",
                    "pic_id": "anvarisy@gmail.com"
                }""".trimIndent()
                APIRequest("https://api.fazpas.com/v2/trusted-device/check", data).fetch(this@MainActivity::onCheckResponse)
            }
        }

    private val actionBtn: Button
        get() = Button(this).apply {
            text = "Action"
            setOnClickListener {
                val dialog = AlertDialog.Builder(this@MainActivity)
                    .setTitle("Pick one")
                    .setItems(
                        arrayOf("Enroll", "Validate", "Remove")
                    ) { dialog, which ->
                        when (which) {
                            0 -> {
                                val data = """{
                                    "merchant_app_id": "$MERCHANT_APP_ID",
                                    "meta": "$meta",
                                    "pic_id": "anvarisy@gmail.com",
                                    "challenge": "$challenge"
                                }""".trimIndent()
                                APIRequest("https://api.fazpas.com/v2/trusted-device/enroll",data).fetch(this@MainActivity::onEnrollResponse)
                            }
                            1 -> {
                                val data = """{
                                    "merchant_app_id": "$MERCHANT_APP_ID",
                                    "meta": "$meta",
                                    "fazpass_id": "$fazpassId",
                                    "challenge": "$challenge"
                                }""".trimIndent()
                                APIRequest("https://api.fazpas.com/v2/trusted-device/validate",data).fetch(this@MainActivity::onValidateResponse)
                            }
                            2 -> {
                                val data = """{
                                    "merchant_app_id": "$MERCHANT_APP_ID",
                                    "meta": "$meta",
                                    "fazpass_id": "$fazpassId",
                                    "challenge": "$challenge"
                                }""".trimIndent()
                                APIRequest("https://api.fazpas.com/v2/trusted-device/remove",data).fetch(this@MainActivity::onRemoveResponse)
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                dialog.show()
            }
        }

    private fun requestPermissions() {
        val requiredPermissions = ArrayList(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requiredPermissions.add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }
        val deniedPermissions: MutableList<String> = ArrayList()
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                deniedPermissions.add(permission)
            }
        }
        if (deniedPermissions.size != 0) ActivityCompat.requestPermissions(
            this,
            deniedPermissions.toTypedArray(),
            1
        )
    }

    private fun readDataFromResponse(response: String) {
        var meta = ""
        val responseReader = JsonReader(response.reader())
        responseReader.beginObject()
        while (responseReader.hasNext()) {
            val key = responseReader.nextName()
            if (key == "data") {
                responseReader.beginObject()
                continue
            }
            if (key == "meta") {
                meta = responseReader.nextString()
                break
            }
            responseReader.skipValue()
        }
        responseReader.close()

        val decryptedMetaData = decryptMetaData(meta)
        val reader = JsonReader(decryptedMetaData.reader())
        reader.beginObject()

        var fazpassId : String? = null
        var isActive = false
        var challenge = ""
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "fazpass_id" -> fazpassId = reader.nextString()
                "is_active" -> isActive = reader.nextBoolean()
                "challenge" -> challenge = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.close()

        if (isActive) {
            this.fazpassId = fazpassId
        } else {
            this.fazpassId = null
        }
        this.challenge = challenge
    }

    private fun decryptMetaData(encryptedMetaData: String): String {
        var key = String(assets.open(PRIVATE_KEY_ASSET_FILENAME).readBytes())
        key = key.replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\n", "").replace("\r", "")
        val privateKey: PrivateKey?
        try {
            val keySpec = PKCS8EncodedKeySpec(Base64.decode(key, Base64.DEFAULT))
            val keyFactory = KeyFactory.getInstance("RSA")
            privateKey = keyFactory.generatePrivate(keySpec)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        val decryptedMetaData = Base64.decode(encryptedMetaData, Base64.DEFAULT)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(decryptedMetaData)

        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}