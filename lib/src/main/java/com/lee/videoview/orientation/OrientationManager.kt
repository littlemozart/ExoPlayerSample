package com.lee.videoview.orientation

import android.app.Activity
import android.content.pm.ActivityInfo
import android.provider.Settings
import android.util.Log
import android.view.OrientationEventListener

private const val TAG = "OrientationManager"

class OrientationManager(private val activity: Activity) : OrientationEventListener(activity),
    OrientationOwner {

    private var oldOrientation = 0

    override fun onOrientationChanged(orietation: Int) {
        if (!isAutoRotate) {
            return
        }
        if (orietation == ORIENTATION_UNKNOWN || activity.requestedOrientation == ORIENTATION_UNKNOWN) {
            return
        }
        val temp = when {
            orietation > 350 || orietation < 10 -> 0
            orietation in 81..99 -> 90
            orietation in 171..189 -> 180
            orietation in 261..279 -> 270
            else -> return
        }
        if (oldOrientation != temp) {
            oldOrientation = temp
            Log.d(TAG, "onOrientationChanged: $oldOrientation")
            activity.requestedOrientation = if (temp == 0 || temp == 180) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else if (temp == 90) {
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    private inline val isAutoRotate: Boolean
        get() {
            try {
                val orientation = Settings.System.getInt(
                    activity.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION
                )
                return orientation == 1
            } catch (e: Settings.SettingNotFoundException) {
                Log.e(TAG, "isAutoRotate: fail to get settings", e)
            }
            return false
        }

    override fun enterFullscreen() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun exitFullscreen() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun setRotateAllowed(isAllowed: Boolean) {
        if (isAllowed) enable() else disable()
    }
}