package com.fazpass.android_trusted_device_v2.`object`

import com.fazpass.android_trusted_device_v2.Fazpass
import com.fazpass.android_trusted_device_v2.SensitiveData

/**
 * An object to be used as settings for [Fazpass.setSettings] method.
 *
 * This object isn't meant to be created by itself as it's only constructor,
 * [FazpassSettings.fromString], isn't meant to be called independently.
 * Use [FazpassSettings.Builder] class instead.
 */
class FazpassSettings private constructor(
    val sensitiveData: List<SensitiveData>,
    val isBiometricLevelHigh: Boolean) {

    companion object {

        fun fromString(settingsString: String) : FazpassSettings {
            val splitter = settingsString.split(";")
            val sensitiveData = splitter[0].split(",")
                .takeWhile { it.isNotBlank() }
                .map {
                    SensitiveData.valueOf(it)
                }.toList()
            val isBiometricLevelHigh = splitter[1].toBoolean()

            return FazpassSettings(sensitiveData, isBiometricLevelHigh)
        }
    }

    override fun toString(): String {
        return "${sensitiveData.joinToString(separator = ",") { it.name }};$isBiometricLevelHigh"
    }

    /**
     * A builder to create [FazpassSettings] object.
     *
     * To enable specific sensitive data collection, call [enableSelectedSensitiveData] method
     * and specify which data you want to collect.
     * Otherwise call [disableSelectedSensitiveData] method
     * and specify which data you don't want to collect.
     * To set biometric level to high, call [setBiometricLevelToHigh]. Otherwise call
     * [setBiometricLevelToLow].
     * To create [FazpassSettings] object with this builder configuration, call [build] method.
     *
     * You can also copy settings from [FazpassSettings] by using the secondary constructor of this builder.
     *
     * @sample settings
     * @sample builderFromSettings
     *
     */
    class Builder() {
        var sensitiveDataList: ArrayList<SensitiveData> = arrayListOf()
            private set
        var isBiometricLevelHigh: Boolean = false
            private set

        constructor(settings: FazpassSettings) : this() {
            sensitiveDataList.addAll(settings.sensitiveData)
            isBiometricLevelHigh = settings.isBiometricLevelHigh
        }

        fun enableSelectedSensitiveData(vararg sensitiveData: SensitiveData) : Builder {
            for (data in sensitiveData) {
                if (sensitiveDataList.contains(data)) {
                    continue
                } else {
                    sensitiveDataList.add(data)
                }
            }
            return this
        }

        fun disableSelectedSensitiveData(vararg sensitiveData: SensitiveData) : Builder {
            for (data in sensitiveData) {
                if (sensitiveDataList.contains(data)) {
                    sensitiveDataList.remove(data)
                } else {
                    continue
                }
            }
            return this
        }

        fun setBiometricLevelToHigh() : Builder {
            this.isBiometricLevelHigh = true
            return this
        }

        fun setBiometricLevelToLow() : Builder {
            this.isBiometricLevelHigh = false
            return this
        }

        fun build() : FazpassSettings = FazpassSettings(
            sensitiveDataList,
            isBiometricLevelHigh)
    }
}

private val settings: FazpassSettings = FazpassSettings.Builder()
    .enableSelectedSensitiveData(SensitiveData.location, SensitiveData.simNumbersAndOperators)
    .setBiometricLevelToHigh()
    .build()

private val builderFromSettings: FazpassSettings.Builder = FazpassSettings.Builder(settings)