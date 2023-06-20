package com.fazpass.android_trusted_device_v2

import android.content.Context
import java.lang.Exception

internal class CloningUtil(private val context: Context) {

    companion object {

        private const val DUAL_APP_ID_999 = "999"
        private const val MAX_ACCOUNT_INDEX = 10
        private const val DOT = '.'
        private var APP_PACKAGE_DOT_COUNT: Int? = null

        fun init(originalPackageName: String) {
            var dotCount = 0
            originalPackageName.forEach {
                if (it == DOT) dotCount++
            }
            APP_PACKAGE_DOT_COUNT = dotCount
        }
    }

    val isAppCloned: Boolean
        get() {
            if (APP_PACKAGE_DOT_COUNT == null) throw Exception("Fazpass has to be initialized first!")

            val path: String = context.filesDir.path
            return path.contains("/$DUAL_APP_ID_999/")
                    || getDotCount(path) > APP_PACKAGE_DOT_COUNT!!
                    || getAccountIndex(path) > MAX_ACCOUNT_INDEX
        }

    private fun getDotCount(path: String): Int {
        var count = 0
        for (element in path) {
            if (count > APP_PACKAGE_DOT_COUNT!!) {
                break
            }
            if (element == DOT) {
                count++
            }
        }
        return count
    }

    private fun getAccountIndex(path: String): Int {
        val accountIndexStr = path.substringAfter("data/user/").substringBefore("/")
        return accountIndexStr.toIntOrNull() ?: 0
    }
}