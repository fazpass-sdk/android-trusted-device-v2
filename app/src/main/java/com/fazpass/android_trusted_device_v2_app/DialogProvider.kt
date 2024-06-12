package com.fazpass.android_trusted_device_v2_app

import android.app.Activity
import androidx.appcompat.app.AlertDialog

class DialogProvider(private val activity: Activity) {

    fun showActionDialog(
        actions: Array<String>,
        onPickAction: (Int) -> Unit
    ) {
        AlertDialog.Builder(activity).apply {
            setTitle("Pick one action")
            setItems(actions) { dialog, which ->
                onPickAction(which)
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

    fun showPickNotifiableDeviceDialog(
        notifiableDevices: ArrayList<NotifiableDeviceModel>,
        callback: (String) -> Unit
    ) {
        val ids = arrayListOf<String>()
        val names = arrayListOf<String>()
        notifiableDevices.forEach {
            ids.add(it.id)
            names.add("${it.name}, ${it.series}")
        }
        AlertDialog.Builder(activity).apply {
            setTitle("Pick one device")
            setItems(names.toTypedArray()) { dialog, which ->
                callback(ids[which])
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

    fun showCrossDeviceRequestDialog(
        deviceName: String,
        onAccept: () -> Unit,
        onDeny: () -> Unit
    ) {
        AlertDialog.Builder(activity).apply {
            setTitle("Cross Device Request")
            setMessage("Device $deviceName is asking to login into your account. Authorize?")
            setPositiveButton("YES") { dialog, _ ->
                onAccept()
                dialog.dismiss()
            }
            setNegativeButton("NO") { dialog, _ ->
                onDeny()
                dialog.dismiss()
            }
        }.show()
    }

    fun showCrossDeviceValidateDialog(
        deviceName: String,
        action: String
    ) {
        AlertDialog.Builder(activity).apply {
            setTitle("Cross Device Validate")
            setMessage("Your request has been responded by device $deviceName with \"$action\".")
            setNeutralButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }
}