package com.fazpass.android_trusted_device_v2

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidTrustedDeviceTest {

    @Test
    fun createMetaTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Fazpass.instance.init(appContext, "com.fazpass.android_trusted_device_v2.test")
        Fazpass.instance.check(appContext)
    }
}