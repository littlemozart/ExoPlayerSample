package com.lee.videoview.widget.video

interface FullscreenView {
    fun onFullscreenEntered()
    fun onFullscreenExited()
    fun onBackPressed(): Boolean
}