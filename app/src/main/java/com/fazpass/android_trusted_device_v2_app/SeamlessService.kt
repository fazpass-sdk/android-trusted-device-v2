package com.fazpass.android_trusted_device_v2_app

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.Scanner
import javax.net.ssl.HttpsURLConnection

class SeamlessService {

    companion object {
        private const val BASE_URL = "https://api.fazpas.com/v2/trusted-device"
        private const val PIC_ID = "anvarisy@gmail.com"
        private const val MERCHANT_APP_ID = "afb2c34a-4c4f-4188-9921-5c17d81a3b3d"
        //"afb2c34a-4c4f-4188-9921-5c17d81a3b3d"
        //"e30e8ae2-1557-46f6-ba3a-755b57ce4c44"
        private const val BEARER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozNn0.mfny8amysdJQYlCrUlYeA-u4EG1Dw9_nwotOl-0XuQ8"
        //"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozNn0.mfny8amysdJQYlCrUlYeA-u4EG1Dw9_nwotOl-0XuQ8"
        //"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjo0fQ.WEV3bCizw9U_hxRC6DxHOzZthuJXRE8ziI3b6bHUpEI"
    }

    fun check(
        meta: String,
        callback: (String, Boolean) -> Unit
    ) {
        val url = "$BASE_URL/check"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "pic_id" to PIC_ID
        )
        fetch(url, body, callback)
    }

    fun enroll(
        meta: String,
        challenge: String,
        callback: (String, Boolean) -> Unit
    ) {
        val url = "$BASE_URL/enroll"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "pic_id" to PIC_ID,
            "challenge" to challenge
        )
        fetch(url, body, callback)
    }

    fun validate(
        meta: String,
        fazpassId: String,
        challenge: String,
        callback: (String, Boolean) -> Unit
    ) {
        val url = "$BASE_URL/validate"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "fazpass_id" to fazpassId,
            "challenge" to challenge
        )
        fetch(url, body, callback)
    }

    fun remove(
        meta: String,
        fazpassId: String,
        challenge: String,
        callback: (String, Boolean) -> Unit
    ) {
        val url = "$BASE_URL/remove"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "fazpass_id" to fazpassId,
            "challenge" to challenge
        )
        fetch(url, body, callback)
    }

    fun sendNotification(
        meta: String,
        selectedDevice: String,
        callback: (String, Boolean) -> Unit
    ) {
        val url = "$BASE_URL/send/notification"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "pic_id" to PIC_ID,
            "selected_device" to selectedDevice
        )
        fetch(url, body, callback)
    }

    fun validateNotification(
        meta: String,
        notificationId: String,
        answer: Boolean,
        callback: (String, Boolean) -> Unit
    ) {
        val url = "$BASE_URL/validate/notification"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "notification_id" to notificationId,
            "result" to answer
        )
        fetch(url, body, callback)
    }

    private fun fetch(url: String, body: Map<String, Any>, callback: (String, Boolean) -> Unit) {
        Thread {
            var s: Scanner? = null
            var response = ""
            var status = false

            val conn = URL(url).openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $BEARER_TOKEN")
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.setRequestProperty("Accept", "*/*")
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br")
            conn.setRequestProperty("Connection", "keep-alive")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.write(JSONObject(body).toString().toByteArray(charset = Charsets.UTF_8))

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

            Handler(Looper.getMainLooper()).post {
                Log.i("API Request", "url=$url\nbody=$body\nresponse=$response")
                callback(response, status)
            }
        }.start()
    }
}