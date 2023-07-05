package com.fazpass.android_trusted_device_v2_app

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class EntryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private var _name: String
    private var _value: String

    var name: String
        get() = _name
        set(value) {
            _name = value
            updateView()
        }

    var value: String
        get() = _value
        set(value) {
            _value = value
            updateView()
        }

    private val nameView: TextView
    private val valueView: EditText

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.EntryView, defStyle, 0)
        _name = a.getString(R.styleable.EntryView_name) ?: ""
        _value = a.getString(R.styleable.EntryView_value) ?: ""
        a.recycle()

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.entry_view, this)

        nameView = findViewById(R.id.ev_name)
        valueView = findViewById(R.id.ev_value)
        valueView.keyListener = null

        orientation = VERTICAL

        updateView()
    }

    private fun updateView() {
        nameView.text = _name
        valueView.setText(_value)
    }
}