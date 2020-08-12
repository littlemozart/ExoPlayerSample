package com.lee.videoview.widget.toast

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.lee.videoview.R

class VolumeToastViewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    ToastView {

    private var progressBar: ProgressBar? = null
    private var stateIcon: ImageView? = null
    private var titleText: TextView? = null

    init {
        View.inflate(context, R.layout.view_volume_toast, this)
        progressBar = findViewById(R.id.progress_bar)
        stateIcon = findViewById(R.id.state_icon)
        titleText = findViewById(R.id.title_text)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isVisible = false
    }

    override fun cancelAnimate() {
        animate().cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAnimate()
    }

    fun setProgress(progress: Int) {
        progressBar?.progress = progress
    }

    fun setTitle(charSequence: CharSequence?) {
        titleText?.text = charSequence
    }

    fun setStateIcon(@DrawableRes resId: Int) {
        stateIcon?.setImageResource(resId)
    }

    fun show() {
        cancelAnimate()
        alpha = 1f
        isVisible = true
    }

    fun dismiss() {
        animate().alpha(0f).setDuration(ToastView.DURATION_ANIMATOR)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    isVisible = false
                }
            })
    }
}