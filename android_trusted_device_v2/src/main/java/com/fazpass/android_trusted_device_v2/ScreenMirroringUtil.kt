package com.fazpass.android_trusted_device_v2

import android.content.Context
import android.hardware.display.DisplayManager
import android.media.MediaRouter

internal class ScreenMirroringUtil(private val context: Context) {

    val isScreenMirroring : Boolean
        get() = checkMethod1() || checkMethod2()

    private fun checkMethod1() : Boolean {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        return displayManager
            .getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            .isNotEmpty()
    }

    private fun checkMethod2() : Boolean {
        val mediaRouter = context.getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter
        val route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO) ?: return false
        return route.presentationDisplay != null
    }
}