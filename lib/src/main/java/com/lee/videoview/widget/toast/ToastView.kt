package com.lee.videoview.widget.toast

interface ToastView {
    companion object {
        const val DURATION_ANIMATOR = 500L
    }

    fun cancelAnimate()
}