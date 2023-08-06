package com.fazpass.android_trusted_device_v2

import android.content.Context
import android.util.Log

/**
 * Original File Path: /data/user/0/com.example.app/files
 *
 * App Cloned by Third Party App File Path: /data/data/com.ludashi.dualspace/virtual/data/user/0/com.example.app/files
 *
 * App Cloned by OEM's App Cloner: /data/user/999/com.example.app/files
 */
internal class CloningUtil(private val context: Context) {

    companion object {
        private const val DUAL_APP_ID_999 = "999"
        private const val MAX_ACCOUNT_INDEX = 5
    }

    val isAppCloned: Boolean
        get() {
            val path: String = context.filesDir.path
            if (Fazpass.IS_DEBUG) Log.i("FILES-DIR-PATH", path)
            return hasDualAppIdOnPath(path)
                    || isAccountIndexMoreThanMax(path)
                    || hasAnotherPackageOnPath(path)
        }

    private fun hasDualAppIdOnPath(path: String): Boolean {
        return path.contains("/$DUAL_APP_ID_999/")
    }

    private fun isAccountIndexMoreThanMax(path: String): Boolean {
        val accountIndexStr = path.substringAfter("data/user/").substringBefore("/")
        val accountIndex = accountIndexStr.toIntOrNull() ?: 0
        return accountIndex > MAX_ACCOUNT_INDEX
    }

    private fun hasAnotherPackageOnPath(path: String): Boolean {
        val packageName : String = context.packageName
        val strBefore = path.substringBefore("/$packageName/")
        val strAfter = path.substringAfter("/$packageName/")

        val pathSegments = arrayListOf<String>()
        pathSegments.apply {
            addAll(strBefore.split("/"))
            addAll(strAfter.split("/"))
        }

        var pathsPackageCount = 0
        pathSegments.forEach {
            if (it.contains("."))  pathsPackageCount++
        }

        return pathsPackageCount != 0
    }
}