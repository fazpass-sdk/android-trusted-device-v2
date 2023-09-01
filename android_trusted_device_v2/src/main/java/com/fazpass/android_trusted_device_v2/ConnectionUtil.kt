package com.fazpass.android_trusted_device_v2

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

internal class ConnectionUtil(private val context: Context) {

    val isVpnConnectionAvailable: Boolean
        get() {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            return caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        }
}