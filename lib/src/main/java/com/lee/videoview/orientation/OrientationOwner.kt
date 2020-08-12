package com.lee.videoview.orientation

interface OrientationOwner {
    fun enterFullscreen()
    fun exitFullscreen()
    fun setRotateAllowed(isAllowed: Boolean)
}