package com.lee.videoview.widget.toast

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible

class TimeToastViewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr),
    ToastView {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isVisible = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAnimate()
    }

    override fun cancelAnimate() {
        animate().cancel()
    }

    fun show(@StringRes resId: Int) {
        show(context.getText(resId))
    }

    fun show(charSequence: CharSequence?) {
        cancelAnimate()
        text = charSequence
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