package com.lee.videoview.manager

import android.view.Window

class BrightnessManager(private val window: Window?) {

    fun getCurrentBrightness(): Float {
        if (window != null) {
            val brightness = window.attributes.screenBrightness
            return if (brightness != -1f) brightness else 0.5f
        }
        return 0.5f
    }

    fun setBrightness(brightness: Float) {
        if (window != null) {
            window.attributes = window.attributes.apply { screenBrightness = brightness }
        }
    }
}