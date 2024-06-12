package com.fazpass.android_trusted_device_v2_app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.JsonReader
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.NullPointerException
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

class MainActivity : AppCompatActivity() {

    private val seamlessService = SeamlessService()
    private val fazpassService = FazpassService(this)
    private val dialogProvider = DialogProvider(this)

    private lateinit var infoView: LinearLayout
    private lateinit var settingsBtn: Button
    private lateinit var genMetaBtn: Button

    private var meta: String = ""

    private var fazpassId: String = ""
    private var fazpassIdIsShown = false

    private var challenge: String = ""

    private var notifiableDevices: ArrayList<NotifiableDeviceModel> = arrayListOf()

    private val checkBtn: Button
        get() = Button(this).apply {
            text = "Check"
            setOnClickListener {
                seamlessService.check(meta, this@MainActivity::onCheckResponse)
            }
        }

    private val actionBtn: Button
        get() = Button(this).apply {
            text = "Action"
            setOnClickListener {
                val items = arrayListOf("Enroll", "Validate", "Remove")
                if (notifiableDevices.isNotEmpty()) {
                    items.add("Send Notification")
                }
                dialogProvider.showActionDialog(items.toTypedArray()) { which ->
                    when (which) {
                        0 -> seamlessService.enroll(meta, challenge, this@MainActivity::onEnrollResponse)
                        1 -> seamlessService.validate(meta, fazpassId, challenge) { response, status ->
                            onDefaultResponse("Validate", response, status)
                        }
                        2 -> seamlessService.remove(meta, fazpassId, challenge) { response, status ->
                            onDefaultResponse("Remove", response, status)
                        }
                        3 -> dialogProvider.showPickNotifiableDeviceDialog(notifiableDevices) { pickedDeviceId ->
                            seamlessService.sendNotification(meta, pickedDeviceId) { response, status ->
                                onDefaultResponse("Send Notification", response, status)
                            }
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fazpassService.init()

        infoView = findViewById(R.id.ma_info_view)
        initSettingsBtn()
        initGenMetaBtn()

        requestPermissions()

        fazpassService.listenToIncomingCrossDeviceData(
            this::onCrossDeviceRequest,
            this::onCrossDeviceValidate
        )
    }

    private fun initSettingsBtn() {
        settingsBtn = findViewById(R.id.ma_settings_btn)
        settingsBtn.setOnClickListener {
            // load fazpass settings
            val oldSettings = fazpassService.getSettings()
            val newSettings = Settings()
            val dialog = AlertDialog.Builder(this)
                .setTitle("Settings")
                .setMultiChoiceItems(
                    arrayOf(
                        "Enable location",
                        "Enable Sim Information",
                        "High Biometric Level"
                    ),
                    booleanArrayOf(
                        oldSettings.isLocationEnabled,
                        oldSettings.isSimInfoEnabled,
                        oldSettings.isHighLevelBiometricEnabled
                    )
                ) { _, i, newValue ->
                    when (i) {
                        0 -> newSettings.isLocationEnabled = newValue
                        1 -> newSettings.isSimInfoEnabled = newValue
                        2 -> newSettings.isHighLevelBiometricEnabled = newValue
                    }
                }
                .setPositiveButton("Save") { dialog, _ ->
                    fazpassService.setSettings(newSettings)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .create()
            dialog.show()
        }
    }

    private fun initGenMetaBtn() {
        genMetaBtn = findViewById(R.id.ma_genmeta_btn)
        genMetaBtn.setOnClickListener {
            infoView.removeAllViews()
            fazpassIdIsShown = false

            try {
                fazpassService.generateMeta { meta, fazpassException ->
                    when (fazpassException) {
                        null -> onMetaGenerated(meta)
                        else -> onErrorOccurred(fazpassException.exception)
                    }
                }
            } catch (e: Exception) {
                onErrorOccurred(e)
            }
        }
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

        infoView.addView(EntryView(this).apply {
            name = "Generated Meta"
            value = meta
        })

        infoView.addView(checkBtn)
    }

    private fun onCheckResponse(response: String, status: Boolean) {
        infoView.removeViewAt(infoView.childCount-1)

        infoView.addView(EntryView(this).apply {
            name = "Check Response"
            value = response
        })

        if (!status) return

        readDataFromResponse(response)
        if (fazpassId.isNotBlank()) {
            infoView.addView(EntryView(this).apply {
                name = "Fazpass ID"
                value = fazpassId
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
            if (!fazpassIdIsShown && fazpassId.isNotBlank()) {
                infoView.addView(EntryView(this).apply {
                    name = "Fazpass ID"
                    value = fazpassId
                })
                fazpassIdIsShown = true
            }
        }

        infoView.addView(TextView(this).apply {
            setTextColor(Color.RED)
            text = "*Press 'Generate Meta' button to reset"
        })
    }

    private fun onDefaultResponse(title: String, response: String, status: Boolean) {
        if (title != "Send Notification" && title != "Validate Notification") {
            infoView.removeViewAt(infoView.childCount-1)
        }

        infoView.addView(EntryView(this).apply {
            name = "$title Response"
            value = response
        })

        infoView.addView(TextView(this).apply {
            setTextColor(Color.RED)
            text = "*Press 'Generate Meta' button to reset"
        })
    }

    private fun onCrossDeviceRequest(deviceName: String, notificationId: String) {
        fun onRespond(answer: Boolean) {
            fazpassService.generateMeta { meta, fazpassException ->
                when (fazpassException) {
                    null -> {
                        seamlessService.validateNotification(meta, notificationId, answer) { response, status ->
                            onDefaultResponse("Validate Notification", response, status)
                        }
                    }
                    else -> onErrorOccurred(fazpassException.exception)
                }
            }
        }

        dialogProvider.showCrossDeviceRequestDialog(
            deviceName,
            onAccept = { onRespond(true) },
            onDeny = { onRespond(false) }
        )
    }

    private fun onCrossDeviceValidate(deviceName: String, action: String) {
        dialogProvider.showCrossDeviceValidateDialog(deviceName, action)
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
        val responseReader = JsonReader(response.reader())
        responseReader.beginObject()
        while (responseReader.hasNext()) {
            when (responseReader.nextName()) {
                "data" -> responseReader.beginObject()
                "meta" -> responseReader.beginObject()
                "fazpass_id" -> this.fazpassId = responseReader.nextString()
                "challenge" -> this.challenge = responseReader.nextString()
                "notifiable_devices" -> {
                    notifiableDevices.clear()
                    responseReader.beginArray()
                    while (responseReader.hasNext()) {
                        notifiableDevices.add(NotifiableDeviceModel(responseReader))
                    }
                    responseReader.endArray()
                }
                else -> responseReader.skipValue()
            }
        }
        responseReader.close()
    }
}