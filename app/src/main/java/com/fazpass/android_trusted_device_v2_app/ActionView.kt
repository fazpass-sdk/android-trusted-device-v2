package com.fazpass.android_trusted_device_v2_app

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class ActionView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    fun setOnCheck(callback: (View) -> Unit) {
        checkBtn.setOnClickListener(callback)
    }

    fun setOnValidate(callback: (View) -> Unit) {
        validateBtn.setOnClickListener(callback)
    }

    private val checkBtn: Button
    private val validateBtn: Button

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.action_view, this)

        checkBtn = findViewById(R.id.av_check)
        validateBtn = findViewById(R.id.av_validate)

        orientation = HORIZONTAL
    }
}