package com.lee.videoview.widget.video

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lee.videoview.R
import com.lee.videoview.gesture.VideoGestureListener
import com.lee.videoview.gesture.VideoGestureView
import com.lee.videoview.manager.BrightnessManager
import com.lee.videoview.manager.VolumeManager
import com.lee.videoview.orientation.OrientationOwner
import com.lee.videoview.utils.toMediaTime
import com.lee.videoview.widget.toast.SpeedToastView
import com.lee.videoview.widget.toast.TimeToastViewView
import com.lee.videoview.widget.toast.VolumeToastViewView
import kotlin.math.max
import kotlin.math.min

private const val TAG = "CustomVideoView"

class CustomVideoView : FrameLayout, VideoGestureView,
    FullscreenView, SpeedToastView.OnSpeedChangedListener {

    var isOrientationLocked = false
    var isGestureEnabled = true

    var player: ExoPlayer? = null
        set(value) {
            field = value
            playerView?.player = player
        }
    var orientationOwner: OrientationOwner? = null

    private var playerView: PlayerView? = null
    private var timeToastView: TimeToastViewView? = null
    private var volumeToastView: VolumeToastViewView? = null
    private var brightnessToastView: VolumeToastViewView? = null
    private var speedToastView: SpeedToastView? = null

    private var screenBtn: ImageButton? = null
    private var screenLockBtn: FloatingActionButton? = null
    private var controllerBar: View? = null
    private var speedBtn: TextView? = null

    private var isFullscreen = false
    private var isLockRotate = false
        set(value) {
            field = value
            controllerBar?.isVisible = !field
            isGestureEnabled = !field
            if (field) screenLockBtn?.setImageResource(R.drawable.ic_lock)
            else screenLockBtn?.setImageResource(R.drawable.ic_unlock)
        }
    private var positionOffset: Long = 0L

    private val gestureListener =
        VideoGestureListener(context, this)
    private val gesture = GestureDetector(context, gestureListener)

    private val brightnessManager: BrightnessManager = if (context is Activity) {
        BrightnessManager((context as Activity).window)
    } else {
        BrightnessManager(null)
    }
    private val volumeManager: VolumeManager =
        VolumeManager(context)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @SuppressLint("ClickableViewAccessibility")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomVideoView)
        val playerViewLayoutId =
            a.getResourceId(R.styleable.CustomVideoView_player_view_layout_id, R.layout.view_player)
        val timeToastLayoutId = a.getResourceId(
            R.styleable.CustomVideoView_time_toast_layout_id,
            R.layout.view_time_toast
        )
        a.recycle()

        View.inflate(context, playerViewLayoutId, this)

        val operationLayout = findViewById<RelativeLayout>(R.id.operation_layout)
        if (operationLayout != null) {
            val view = View.inflate(context, timeToastLayoutId, null)
            operationLayout.addView(view)
            timeToastView = view.findViewById(R.id.exo_time_toast)
        }
        if (operationLayout != null) {
            volumeToastView = VolumeToastViewView(
                context
            ).apply {
                setTitle(context.getText(R.string.title_volume))
            }.also {
                operationLayout.addView(it)
            }

            brightnessToastView = VolumeToastViewView(
                context
            ).apply {
                setTitle(context.getText(R.string.title_brightness))
            }.also {
                operationLayout.addView(it)
            }

            speedToastView = SpeedToastView(context).apply {
                onSpeedChangedListener = this@CustomVideoView
            }.also {
                operationLayout.addView(
                    it,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }

        playerView = findViewById(R.id.player_view)
        playerView?.setOnTouchListener { view, event ->
            if (isOrientationLocked) {
                if (event?.action == MotionEvent.ACTION_UP) {
                    view.performClick()
                }
                return@setOnTouchListener true
            }

            if (isGestureEnabled && gesture.onTouchEvent(event)) {
                gestureListener.endTouch(event)
                return@setOnTouchListener true
            }
            gestureListener.endTouch(event)

            super.onTouchEvent(event)
        }
        playerView?.setControllerVisibilityListener {
            Log.d(TAG, "controller visibility: $it")
        }

        screenBtn = findViewById(R.id.exo_fullscreen)
        screenBtn?.setOnClickListener {
            if (isFullscreen) {
                orientationOwner?.exitFullscreen()
            } else {
                orientationOwner?.enterFullscreen()
            }
        }

        screenLockBtn = findViewById(R.id.exo_lock)
        screenLockBtn?.setOnClickListener {
            isLockRotate = if (isLockRotate) {
                orientationOwner?.setRotateAllowed(true)
                false
            } else {
                orientationOwner?.setRotateAllowed(false)
                true
            }
        }

        controllerBar = findViewById(R.id.control_bar)

        speedBtn = findViewById(R.id.exo_speed)
        speedBtn?.setOnClickListener {
            playerView?.hideController()
            speedToastView?.show(player?.playbackParameters?.speed)
        }
    }

    override fun getViewWidth(): Int = width

    override fun getViewHeight(): Int = height

    override fun onVolumeSliding(percent: Float): Boolean {
        val current = volumeManager.getCurrentVolume()
        val max = volumeManager.getMaxVolume()
        val temp = current + (max.toFloat() * percent).toInt()
        val volume = min(max(0, temp), max)
        volumeManager.setVolume(volume)

        val progress = if (max == 0) 0 else (volume * 100 / max)
        val resId = if (progress > 0) R.drawable.ic_volume_up else R.drawable.ic_volume_off
        volumeToastView?.setProgress(progress)
        volumeToastView?.setStateIcon(resId)
        volumeToastView?.show()
        return true
    }

    override fun onBrightnessSliding(percent: Float): Boolean {
        Log.d(TAG, "onBrightnessSliding: $percent")
        val current = brightnessManager.getCurrentBrightness()
        val temp = current + percent * 0.1f
        val brightness = min(max(temp, 0.01f), 1f)
        brightnessManager.setBrightness(brightness)

        val progress = (brightness * 100).toInt()
        val resId =
            if (progress > 50) R.drawable.ic_brightness_high else R.drawable.ic_brightness_low
        brightnessToastView?.setProgress(progress)
        brightnessToastView?.setStateIcon(resId)
        brightnessToastView?.show()
        return true
    }

    override fun onTimelineSeeking(percent: Float): Boolean {
        val duration = player?.duration ?: 0
        val current = player?.currentPosition ?: 0
        val offset = duration * percent
        val position = current + offset.toLong()
        positionOffset = when {
            position < 0 -> 0
            position > duration -> duration
            else -> position
        }
        showSeekToast(positionOffset, duration)
        return true
    }

    override fun onTimelineSeekEnded() {
        player?.seekTo(positionOffset)
        timeToastView?.dismiss()
    }

    override fun onVolumeOrBrightnessSlideEnded() {
        volumeToastView?.dismiss()
        brightnessToastView?.dismiss()
        playerView?.hideController()
    }

    override fun onSingleTap() {
        playerView?.performClick()
    }

    override fun onDoubleTap() {
        player?.playWhenReady = player?.isPlaying != true
    }

    override fun onSpeedChanged(speed: Float) {
        player?.setPlaybackParameters(PlaybackParameters(speed))
        speedToastView?.dismiss()
        when {
            speed < 1f -> speedBtn?.text = SpeedToastView.SpeedText.Speed0.speed
            speed < 1.25f -> speedBtn?.text = SpeedToastView.SpeedText.Speed1.speed
            speed < 1.5f -> speedBtn?.text = SpeedToastView.SpeedText.Speed2.speed
            speed < 2.0f -> speedBtn?.text = SpeedToastView.SpeedText.Speed3.speed
            else -> speedBtn?.text = SpeedToastView.SpeedText.Speed4.speed
        }
    }

    override fun onFullscreenEntered() {
        isFullscreen = true
        screenBtn?.setImageResource(R.drawable.ic_fullscreen_exit)
        screenLockBtn?.isVisible = true
        speedBtn?.isVisible = true
    }

    override fun onFullscreenExited() {
        isFullscreen = false
        isLockRotate = false
        screenBtn?.setImageResource(R.drawable.ic_fullscreen_enter)
        screenLockBtn?.isVisible = false
        speedBtn?.isVisible = false
    }

    override fun onBackPressed(): Boolean {
        if (isFullscreen && orientationOwner != null) {
            orientationOwner?.exitFullscreen()
            return true
        }
        return false
    }

    @Suppress("DEPRECATION")
    private fun showSeekToast(position: Long, duration: Long) {
        val left = (position / 1000).toMediaTime()
        val right = (duration / 1000).toMediaTime()
        val toast = Html.fromHtml("<b><font color='#019EFF'>$left</font> / $right</b>")
        timeToastView?.show(toast)
        playerView?.hideController()
    }
}