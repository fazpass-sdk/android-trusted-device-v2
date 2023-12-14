package com.fazpass.android_trusted_device_v2.`object`

import com.fazpass.android_trusted_device_v2.SensitiveData

/**
 * Control how meta is generated based on this settings.
 *
 * @sample settings
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

    class Builder {
        var sensitiveDataList: ArrayList<SensitiveData> = arrayListOf()
        var isBiometricLevelHigh: Boolean = false

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

private val settings = FazpassSettings.Builder()
    .enableSelectedSensitiveData(SensitiveData.location, SensitiveData.simNumbersAndOperators)
    .setBiometricLevelToHigh()
    .build()
