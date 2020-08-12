package com.lee.videoview.widget.toast

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.lee.videoview.R
import java.lang.ref.WeakReference

class SpeedToastView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ToastView {

    private val speeds = arrayOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    private val handler = DismissHandler(WeakReference(this))
    private val runnable = Runnable {
        handler.sendEmptyMessage(DismissHandler.MSG_DISMISS)
    }

    private var root: View? = null
    private var speedText0: TextView? = null
    private var speedText1: TextView? = null
    private var speedText2: TextView? = null
    private var speedText3: TextView? = null
    private var speedText4: TextView? = null

    var onSpeedChangedListener: OnSpeedChangedListener? = null

    interface OnSpeedChangedListener {
        fun onSpeedChanged(speed: Float)
    }

    enum class SpeedText(val speed: String) {
        Speed0("0.75X"),
        Speed1("1.0X"),
        Speed2("1.25X"),
        Speed3("1.5X"),
        Speed4("2.0X")
    }

    init {
        View.inflate(context, R.layout.view_speed_toast, this)
        speedText0 = findViewById(R.id.speed_0)
        speedText1 = findViewById(R.id.speed_1)
        speedText2 = findViewById(R.id.speed_2)
        speedText3 = findViewById(R.id.speed_3)
        speedText4 = findViewById(R.id.speed_4)
        root = findViewById(R.id.speed_layout)
        speedText0?.setOnClickListener {
            updateSelectedSpeed(speeds[0])
            onSpeedChangedListener?.onSpeedChanged(speeds[0])
        }
        speedText1?.setOnClickListener {
            updateSelectedSpeed(speeds[1])
            onSpeedChangedListener?.onSpeedChanged(speeds[1])
        }
        speedText2?.setOnClickListener {
            updateSelectedSpeed(speeds[2])
            onSpeedChangedListener?.onSpeedChanged(speeds[2])
        }
        speedText3?.setOnClickListener {
            updateSelectedSpeed(speeds[3])
            onSpeedChangedListener?.onSpeedChanged(speeds[3])
        }
        speedText4?.setOnClickListener {
            updateSelectedSpeed(speeds[4])
            onSpeedChangedListener?.onSpeedChanged(speeds[4])
        }
        root?.setOnClickListener {
            dismiss()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isVisible = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(runnable)
        cancelAnimate()
    }

    private fun updateSelectedSpeed(speed: Float) {
        when {
            speed < 1f -> {
                speedText0?.setBackgroundResource(R.drawable.bg_speed_selected)
                speedText1?.background = null
                speedText2?.background = null
                speedText3?.background = null
                speedText4?.background = null
            }
            speed < 1.25f -> {
                speedText0?.background = null
                speedText1?.setBackgroundResource(R.drawable.bg_speed_selected)
                speedText2?.background = null
                speedText3?.background = null
                speedText4?.background = null
            }
            speed < 1.5f -> {
                speedText0?.background = null
                speedText1?.background = null
                speedText2?.setBackgroundResource(R.drawable.bg_speed_selected)
                speedText3?.background = null
                speedText4?.background = null
            }
            speed < 2.0f -> {
                speedText0?.background = null
                speedText1?.background = null
                speedText2?.background = null
                speedText3?.setBackgroundResource(R.drawable.bg_speed_selected)
                speedText4?.background = null
            }
            else -> {
                speedText0?.background = null
                speedText1?.background = null
                speedText2?.background = null
                speedText3?.background = null
                speedText4?.setBackgroundResource(R.drawable.bg_speed_selected)
            }
        }
    }

    fun show(speed: Float?) {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 2000)
        speed?.let {
            updateSelectedSpeed(it)
        }
        cancelAnimate()
        alpha = 1f
        isVisible = true
    }

    fun dismiss() {
        handler.removeCallbacks(runnable)
        animate().alpha(0f).setDuration(ToastView.DURATION_ANIMATOR)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    isVisible = false
                }
            })
    }

    override fun cancelAnimate() {
        animate().cancel()
    }

    class DismissHandler(private val weakReference: WeakReference<SpeedToastView>) :
        Handler(Looper.getMainLooper()) {

        companion object {
            const val MSG_DISMISS = 0x01
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MSG_DISMISS) {
                weakReference.get()?.dismiss()
            }
        }
    }
}