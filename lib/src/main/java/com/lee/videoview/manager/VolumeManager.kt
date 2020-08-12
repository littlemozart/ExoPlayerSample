package com.lee.videoview.manager

import android.content.Context
import android.media.AudioManager

class VolumeManager(context: Context) {

    private val appContext = context.applicationContext

    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val audioMax = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    fun getCurrentVolume(): Int {
        return audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
    }

    fun getMaxVolume(): Int = audioMax ?: 0

    fun setVolume(volume: Int) {
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }
}