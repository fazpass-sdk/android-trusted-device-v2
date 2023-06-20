package com.fazpass.android_trusted_device_v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            Fazpass.instance.init(this, "com.fazpass.android_trusted_device_v2")
            Fazpass.instance.check(this)
        }

        Fazpass.instance.requestPermission(this)
    }
}