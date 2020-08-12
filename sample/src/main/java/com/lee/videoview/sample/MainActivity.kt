package com.lee.videoview.sample

import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.google.android.exoplayer2.SimpleExoPlayer
import com.lee.videoview.media.MediaSourceCreator
import com.lee.videoview.orientation.OrientationManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var orientationManager: OrientationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        orientationManager =
            OrientationManager(this)

        val uri =
            Uri.parse("https://13.cdn-vod.huaweicloud.com/asset/6cc185e4bfb0eee39d4b1f53560d9d4e/play_video/615302baf99559b3649e0c79f3ee6b18.m3u8")
        val dataSource = MediaSourceCreator(this).buildMediaSource(uri)
        val player = SimpleExoPlayer.Builder(baseContext).build()
        video_view.player = player
        video_view.orientationOwner = orientationManager
        player.prepare(dataSource)
        player.playWhenReady = true
    }

    override fun onResume() {
        super.onResume()
        orientationManager.enable()
    }

    override fun onPause() {
        super.onPause()
        orientationManager.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        video_view.player?.release()
    }

    override fun onBackPressed() {
        if (video_view.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            video_view.updateLayoutParams {
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            video_view.onFullscreenEntered()
        } else {
            video_view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = 0
                dimensionRatio = "16:9"
            }
            video_view.onFullscreenExited()
        }
    }
}
