package com.lee.videoview.media

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MediaSourceCreator(context: Context) {

    private val appContext = context.applicationContext
    private val bandwidthMeter = DefaultBandwidthMeter.getSingletonInstance(appContext)

    fun buildMediaSource(uri: Uri, overrideExtension: String? = null): MediaSource {
        val type = if (overrideExtension.isNullOrEmpty()) {
            Util.inferContentType(uri)
        } else {
            Util.inferContentType(".$overrideExtension")
        }
        val factory = buildDataSourceFactory()
        return when (type) {
            C.TYPE_SS -> SsMediaSource.Factory(factory).createMediaSource(uri)
            C.TYPE_DASH -> DashMediaSource.Factory(factory).createMediaSource(uri)
            C.TYPE_HLS -> HlsMediaSource.Factory(factory).createMediaSource(uri)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(factory).createMediaSource(uri)
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }

    private fun buildDataSourceFactory(): DataSource.Factory {
        return DefaultDataSourceFactory(
            appContext,
            bandwidthMeter,
            DefaultHttpDataSourceFactory("exo_player_sampler", bandwidthMeter)
        )
    }
}