package com.fazpass.android_trusted_device_v2_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fazpass.android_trusted_device_v2.Fazpass
import com.fazpass.android_trusted_device_v2.SensitiveData

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PUBLIC_KEY_ASSET_FILENAME = "my_public_key.pub"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fazpass.instance.init(this, PUBLIC_KEY_ASSET_FILENAME)

        val infoView = findViewById<LinearLayout>(R.id.ma_info_view)

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

            Fazpass.instance.generateMeta(this) { meta ->
                infoView.addView(EntryView(this).apply {
                    name = "Generated Meta"
                    value = meta
                })
            }
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
}