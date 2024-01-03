package com.fazpass.android_trusted_device_v2_app

import android.os.Handler
import android.os.Looper
import java.io.IOException
import java.net.URL
import java.util.Scanner
import javax.net.ssl.HttpsURLConnection

class APIRequest(private val url: String, private val data: String) {

    fun fetch(callback: (String, Boolean) -> Unit) {
        Thread {
            var s: Scanner? = null
            var response = ""
            var status = false

            val conn = URL(url).openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer ${MainActivity.BEARER_TOKEN}")
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.setRequestProperty("Accept", "*/*")
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br")
            conn.setRequestProperty("Connection", "keep-alive")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.write(data.toByteArray(charset = Charsets.UTF_8))

            try {
                s = if (conn.responseCode == 200) {
                    status = true
                    Scanner(conn.inputStream, "UTF-8")
                        .useDelimiter("\\A")
                } else {
                    status = false
                    Scanner(conn.errorStream, "UTF-8")
                        .useDelimiter("\\A")
                }
                response = s.next()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                conn.disconnect()
                s?.close()
            }

            Handler(Looper.getMainLooper()).post { callback(response, status) }
        }.start()
    }
}