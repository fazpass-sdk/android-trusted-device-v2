package com.fazpass.android_trusted_device_v2_app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.fazpass.android_trusted_device_v2.Fazpass

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PUBLIC_KEY_ASSET_FILENAME = "myPublicKey.pub"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fazpass.instance.init(this, PUBLIC_KEY_ASSET_FILENAME)

        val infoView = findViewById<LinearLayout>(R.id.ma_info_view)

        val reqPermissionBtn = findViewById<Button>(R.id.ma_reqpermission_btn)
        reqPermissionBtn.setOnClickListener {
            Fazpass.instance.requestPermissions(this)
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
}