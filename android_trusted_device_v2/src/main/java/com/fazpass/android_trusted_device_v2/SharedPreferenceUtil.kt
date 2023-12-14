package com.fazpass.android_trusted_device_v2

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import com.fazpass.android_trusted_device_v2.`object`.FazpassSettings
import javax.crypto.spec.IvParameterSpec

internal class SharedPreferenceUtil(context: Context) {

    private val prefs : SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    companion object {

        private const val PREFS_NAME = "com.fazpass.trusted_device:shared_prefs"

        private const val KEY_ACCOUNT_INDEX_LIST = "account_index_list"

        private const val KEY_ENCRYPTED_STRING = "encrypted_string"
        private const val KEY_IV = "iv"
        private const val KEY_SETTINGS = "settings"
        private fun formatKey(key: String, accountIndex: Int) = "$key:$accountIndex"
    }

    private fun saveAccountIndex(accountIndex: Int) {
        val key = KEY_ACCOUNT_INDEX_LIST
        val oldSet = prefs.getStringSet(key, setOf<String>())!!
        val newSet = setOf<String>(*oldSet.toTypedArray(), accountIndex.toString())
        prefs.edit(commit = true) {
            putStringSet(key, newSet)
        }
    }

    private fun removeAccountIndex(accountIndex: Int) {
        val oldSet = prefs.getStringSet(KEY_ACCOUNT_INDEX_LIST, setOf<String>())!!
        val newSet = hashSetOf<String>(*oldSet.toTypedArray()).apply {
            remove(accountIndex.toString())
        }.toSet()

        prefs.edit(commit = true) {
            putStringSet(KEY_ACCOUNT_INDEX_LIST, newSet)
            remove(formatKey(KEY_ENCRYPTED_STRING, accountIndex))
            remove(formatKey(KEY_IV, accountIndex))
            remove(formatKey(KEY_SETTINGS, accountIndex))
        }
    }

    fun getAccountIndexSet() : Set<String> =
        prefs.getStringSet(KEY_ACCOUNT_INDEX_LIST, setOf<String>())!!

    fun saveEncryptedString(accountIndex: Int, encryptedString: String) {
        saveAccountIndex(accountIndex)
        val key = formatKey(KEY_ENCRYPTED_STRING, accountIndex)
        prefs.edit(commit = true) {
            putString(key, encryptedString)
        }
    }

    fun loadEncryptedString(accountIndex: Int): String? {
        val key = formatKey(KEY_ENCRYPTED_STRING, accountIndex)
        return prefs.getString(key, null)
    }

    fun removeEncryptedString(accountIndex: Int) {
        val key = formatKey(KEY_ENCRYPTED_STRING, accountIndex)
        prefs.edit(commit = true) {
            remove(key)
        }
    }

    fun saveIVParameter(accountIndex: Int, iv: IvParameterSpec) {
        saveAccountIndex(accountIndex)
        val key = formatKey(KEY_IV, accountIndex)
        val stringIv = Base64.encodeToString(iv.iv, Base64.DEFAULT)
        prefs.edit(commit = true) {
            putString(key, stringIv)
        }
    }

    fun loadIvParameter(accountIndex: Int): IvParameterSpec? {
        val key = formatKey(KEY_IV, accountIndex)
        val encryptedIv = prefs.getString(key, null) ?: return null
        return IvParameterSpec(Base64.decode(encryptedIv, Base64.DEFAULT))
    }

    fun removeIVParameter(accountIndex: Int) {
        val key = formatKey(KEY_IV, accountIndex)
        prefs.edit(commit = true) {
            remove(key)
        }
    }

    fun saveFazpassSettings(accountIndex: Int, fazpassSettings: FazpassSettings?) {
        val key = formatKey(KEY_SETTINGS, accountIndex)
        if (fazpassSettings != null) {
            saveAccountIndex(accountIndex)
            prefs.edit(commit = true) {
                putString(key, fazpassSettings.toString())
            }
        } else {
            removeAccountIndex(accountIndex)
        }
    }

    fun getFazpassSettings(accountIndex: Int) : FazpassSettings? {
        val key = formatKey(KEY_SETTINGS, accountIndex)
        val settingsString = prefs.getString(key, null) ?: return null
        return FazpassSettings.fromString(settingsString)
    }

}