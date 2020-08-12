package com.lee.videoview.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

interface VideoGestureView {
    fun getViewWidth(): Int
    fun getViewHeight(): Int
    fun onVolumeSliding(percent: Float): Boolean
    fun onBrightnessSliding(percent: Float): Boolean
    fun onTimelineSeeking(percent: Float): Boolean
    fun onTimelineSeekEnded()
    fun onVolumeOrBrightnessSlideEnded()
    fun onSingleTap()
    fun onDoubleTap()
}

class VideoGestureListener(private val context: Context, private val view: VideoGestureView) :
    GestureDetector.SimpleOnGestureListener() {

    // 判断是否在垂直方向上滚动
    private var isVerticalScrolling = false

    // 判断是否在水平方向上滚动
    private var isHorizontalScrolling = false

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (e1 == null || e2 == null) {
            return false
        }

        val oldX = e1.x
        val oldY = e1.y
        val newX = e2.x
        val nexY = e2.y

        var layoutWidth = view.getViewWidth()
        var layoutHeight = view.getViewHeight()
        if (layoutWidth <= 0) {
            layoutWidth = context.resources.displayMetrics.widthPixels
        }
        if (layoutHeight <= 0) {
            layoutHeight = context.resources.displayMetrics.heightPixels
        }

        var handled = false

        when {
            isHorizontalScrolling -> {
                val percent = (newX - oldX) / layoutWidth.toFloat()
                handled = view.onTimelineSeeking(percent)
            }
            isVerticalScrolling -> {
                val percent = (oldY - nexY) / layoutHeight.toFloat()
                if (oldX < layoutWidth / 3) {
                    handled = view.onBrightnessSliding(percent)
                } else if (oldX > layoutHeight * 2 / 3) {
                    handled = view.onVolumeSliding(percent)
                }
            }
            else -> {
                isHorizontalScrolling = abs(distanceX) > abs(distanceY)
                isVerticalScrolling = !isHorizontalScrolling
                handled = true
            }
        }

        return handled
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        view.onSingleTap()
        return super.onSingleTapUp(e)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        view.onDoubleTap()
        return super.onDoubleTap(e)
    }

    fun endTouch(event: MotionEvent?) {
        if (event != null) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> {
                    if (isHorizontalScrolling) {
                        view.onTimelineSeekEnded()
                    } else if (isVerticalScrolling) {
                        view.onVolumeOrBrightnessSlideEnded()
                    }
                    resetScrollingFlag()
                }
                MotionEvent.ACTION_CANCEL -> resetScrollingFlag()
            }
        }
    }

    private fun resetScrollingFlag() {
        isVerticalScrolling = false
        isHorizontalScrolling = false
    }
}