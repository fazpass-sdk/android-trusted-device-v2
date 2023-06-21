package com.fazpass.android_trusted_device_v2_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.fazpass.android_trusted_device_v2.Fazpass

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PUBLIC_KEY_ASSET_FILENAME = "myPublicKey.pub"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fazpass.instance.init(this, PUBLIC_KEY_ASSET_FILENAME)

        val text = findViewById<TextView>(R.id.ek_text)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            Fazpass.instance.generateMeta(this) {
                text.text = it
            }
        }

        Fazpass.instance.requestPermissions(this)
    }
}