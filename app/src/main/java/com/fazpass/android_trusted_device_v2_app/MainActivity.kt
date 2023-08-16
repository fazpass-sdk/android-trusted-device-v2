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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fazpass.android_trusted_device_v2.Fazpass
import com.fazpass.android_trusted_device_v2.SensitiveData
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PUBLIC_KEY_ASSET_FILENAME = "my_public_key.pub"
        private const val PRIVATE_KEY_ASSET_FILENAME = "my_private_key.key"
    }

    private lateinit var infoView: LinearLayout

    private var meta: String? = null
    private var fazpassId: String? = null
    private var fazpassIdIsShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fazpass.instance.init(this, PUBLIC_KEY_ASSET_FILENAME)

        infoView = findViewById(R.id.ma_info_view)

        val reqPermissionBtn = findViewById<Button>(R.id.ma_reqpermission_btn)
        reqPermissionBtn.setOnClickListener {
            Fazpass.instance.enableSelected(
                SensitiveData.location,
                SensitiveData.simNumbersAndOperators
            )
            requestPermissions()
        }

        val genMetaBtn = findViewById<Button>(R.id.ma_genmeta_btn)
        genMetaBtn.setOnClickListener {
            infoView.removeAllViews()
            fazpassIdIsShown = false

            Fazpass.instance.generateMeta(this) { meta, e ->
                e?.printStackTrace()
                if (e == null) onMetaGenerated(meta)
            }
        }
    }

    private fun onMetaGenerated(meta: String) {
        infoView.addView(EntryView(this).apply {
            name = "Generated Meta"
            value = meta
        })

        this.meta = meta

        infoView.addView(checkBtn)
    }

    private fun onCheckResponse(response: String, status: Boolean) {
        infoView.addView(EntryView(this).apply {
            name = "Check Response"
            value = response
        })

        if (!status) return

        fazpassId = getFazpassId(response)
        if (fazpassId!!.isNotBlank()) {
            infoView.addView(EntryView(this).apply {
                name = "Fazpass ID"
                value = fazpassId!!
            })
            fazpassIdIsShown = true
        }

        infoView.addView(enrollBtn)
    }

    private fun onEnrollResponse(response: String, status: Boolean) {
        infoView.addView(EntryView(this).apply {
            name = "Enroll Response"
            value = response
        })

        if (status) {
            fazpassId = getFazpassId(response).ifBlank { fazpassId }
            if (!fazpassIdIsShown) {
                infoView.addView(EntryView(this).apply {
                    name = "Fazpass ID"
                    value = fazpassId!!
                })
                fazpassIdIsShown = true
            }
        }

        infoView.addView(validateBtn)
    }

    private fun onValidateResponse(response: String, status: Boolean) {
        infoView.addView(EntryView(this).apply {
            name = "Validate Response"
            value = response
        })

        infoView.addView(removeBtn)
    }

    private fun onRemoveResponse(response: String, status: Boolean) {
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
                    "merchant_app_id": "e30e8ae2-1557-46f6-ba3a-755b57ce4c44",
                    "meta": "$meta",
                    "pic_id": "anvarisy@gmail.com"
                }""".trimIndent()
                APIRequest("https://api.fazpas.com/v2/trusted-device/check", data).fetch(this@MainActivity::onCheckResponse)
            }
        }

    private val enrollBtn: Button
        get() = Button(this).apply {
            text = "Enroll"
            setOnClickListener {
                val data = """{
                    "merchant_app_id": "e30e8ae2-1557-46f6-ba3a-755b57ce4c44",
                    "meta": "$meta",
                    "pic_id": "anvarisy@gmail.com"
                }""".trimIndent()
                APIRequest("https://api.fazpas.com/v2/trusted-device/enroll",data).fetch(this@MainActivity::onEnrollResponse)
            }
        }

    private val validateBtn: Button
        get() = Button(this).apply {
            text = "Validate"
            setOnClickListener {
                val data = """{
                    "merchant_app_id": "e30e8ae2-1557-46f6-ba3a-755b57ce4c44",
                    "meta": "$meta",
                    "fazpass_id": "$fazpassId"
                }""".trimIndent()
                APIRequest("https://api.fazpas.com/v2/trusted-device/validate",data).fetch(this@MainActivity::onValidateResponse)
            }
        }

    private val removeBtn: Button
        get() = Button(this).apply {
            text = "Remove"
            setOnClickListener {
                val data = """{
                    "merchant_app_id": "e30e8ae2-1557-46f6-ba3a-755b57ce4c44",
                    "meta": "$meta",
                    "fazpass_id": "$fazpassId"
                }""".trimIndent()
                APIRequest("https://api.fazpas.com/v2/trusted-device/remove",data).fetch(this@MainActivity::onRemoveResponse)
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

    private fun getFazpassId(response: String): String {
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

        var fazpassId = ""
        while (reader.hasNext()) {
            val key = reader.nextName()
            if (key == "fazpass_id") {
                fazpassId = reader.nextString()
                break
            }
            reader.skipValue()
        }
        reader.close()

        return fazpassId
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