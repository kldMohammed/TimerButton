package com.kldmohammed.tb

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.CountDownTimer
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.RelativeLayout

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat

import java.util.concurrent.TimeUnit

/**
 * A button that has a countdown timer running over it.
 */
class TimerButton : RelativeLayout, Animation.AnimationListener, View.OnClickListener {

    private var mBaseButton: Button? = null
    private var mOverView: View? = null
    private var mTransparentButton: Button? = null

    private var mScaleAnimation: ScaleAnimation? = null
    private var mTimer: ButtonCountDownTimer? = null
    private var mTextColor: ColorStateList? = null
    private var mAnimationListener: ButtonAnimationListener? = null

    private var mDuration = 10000L
    private var mDurationLeft: Long = 0
    private var mDynamicStringId: Int = 0
    private var mButtonBackgroundId: Int = 0
    private var mAnimationBackgroundId: Int = 0
    private var mTextSize: Int = 0
    private var mIsReset: Boolean = false
    private var mIsAnimating: Boolean = false
    private var mOnAnimationCompleteText: String? = ""
    private var mBeforeAnimationText: String? = ""
    private var mTextFont: Typeface? = null

    /**
     * Called to set the typeface of the button during the constructor call. Override this method
     * to set your own font
     *
     * @return Typeface
     */
    val typeface: Typeface?
        get() = if (mBaseButton != null) {
            mBaseButton!!.typeface
        } else null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        parseAttributes(context, attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        parseAttributes(context, attrs)
        init()
    }

    private fun parseAttributes(context: Context, attrs: AttributeSet) {
        if (isInEditMode) {
            return
        }

        val a = context.obtainStyledAttributes(attrs, R.styleable.TimerButton)
        mOnAnimationCompleteText = a.getString(R.styleable.TimerButton_animationCompleteText)
        mBeforeAnimationText = a.getString(R.styleable.TimerButton_defaultText)
        mDynamicStringId = a.getResourceId(R.styleable.TimerButton_dynamicString, 0)
        mButtonBackgroundId = a.getResourceId(R.styleable.TimerButton_buttonBackground, 0)
        mAnimationBackgroundId = a.getResourceId(R.styleable.TimerButton_animationBackground, 0)
        mTextColor = a.getColorStateList(R.styleable.TimerButton_buttonTextColor)
        mTextSize = a.getDimensionPixelSize(R.styleable.TimerButton_buttonTextSize, 0)
        if (a.hasValue(R.styleable.TimerButton_buttonTextFont)) {
            val fontId = a.getResourceId(R.styleable.TimerButton_buttonTextFont, -1)
            mTextFont = ResourcesCompat.getFont(context, fontId)
        }
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        var height = View.MeasureSpec.getSize(heightMeasureSpec)

        for (i in 0 until childCount) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec)
            if (i == 0) {
                height = getChildAt(i).measuredHeight
            }
        }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val width = r - l
            val height = b - t
            getChildAt(i).layout(0, 0, width, height)
        }
    }

    private fun init() {
        View.inflate(context, R.layout.layout_timer_button, this)

        mBaseButton = findViewById(R.id.timer_base_button)
        mOverView = findViewById(R.id.over_view)
        mTransparentButton = findViewById(R.id.text_button)
        if (mTextFont != null) {
            mBaseButton!!.typeface = mTextFont
        }

        setBeforeAnimationText(mBeforeAnimationText!!)
        setButtonBackground(mButtonBackgroundId)
        setAnimationBackground(mAnimationBackgroundId)
        mBaseButton!!.setTextColor(if (mTextColor != null) mTextColor else ColorStateList.valueOf(-0x1000000))
        mTransparentButton!!.setTextColor(
            if (mTextColor != null) mTextColor else ColorStateList.valueOf(
                -0x1000000
            )
        )
        mBaseButton!!.setOnClickListener(this)

        val typeface = typeface
        if (typeface != null) {
            mBaseButton!!.typeface = typeface
            mTransparentButton!!.typeface = typeface
        }

        if (mTextSize > 0) {
            mBaseButton!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
            mTransparentButton!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
        } else {
            mBaseButton!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE.toFloat())
            mTransparentButton!!.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_TEXT_SIZE.toFloat()
            )
        }
    }

    private fun setupAnimation() {
        val fromX = (mDuration - mDurationLeft).toFloat() / mDuration
        mScaleAnimation = ScaleAnimation(fromX, 1.0f, 1.0f, 1.0f)
        mScaleAnimation!!.interpolator = LinearInterpolator()
        mScaleAnimation!!.duration = if (mDurationLeft == 0L) mDuration else mDurationLeft
        mScaleAnimation!!.setAnimationListener(this)
    }

    /**
     * Set the duration for which the animation will run and the button will be disabled
     *
     * @param duration duration of animation
     */
    fun setDuration(duration: Long) {
        mDuration = duration
        mDurationLeft = mDuration
    }

    /**
     * Set the text to display before the animations will run
     *
     * @param beforeAnimationText text to display before animation
     */
    fun setBeforeAnimationText(beforeAnimationText: String) {
        mBeforeAnimationText = beforeAnimationText
        mBaseButton!!.text = mBeforeAnimationText
        mTransparentButton!!.text = mBeforeAnimationText
    }

    /**
     * Set the text to display after the animation is finished. Set as null if
     * it is supposed to be same as set in [.setBeforeAnimationText]
     *
     * @param onAnimationCompleteText text to display after animation is finished
     */
    fun setOnAnimationCompleteText(onAnimationCompleteText: String?) {
        if (mOnAnimationCompleteText == null || mOnAnimationCompleteText!!.isEmpty()) {
            mOnAnimationCompleteText = mBeforeAnimationText
        }
        mOnAnimationCompleteText = onAnimationCompleteText
    }

    /**
     * Set the string resource id to be displayed during the animation.
     * The string resource should be a formatted string that accepts one integer
     *
     * @param id string resource id
     */
    fun setDynamicText(@IdRes id: Int) {
        mDynamicStringId = id
    }

    /**
     * Set the background of the button
     *
     * @param id background resource id
     */
    fun setButtonBackground(@DrawableRes id: Int) {
        if (id != 0) {
            mButtonBackgroundId = id
            mBaseButton!!.setBackgroundResource(id)
        }
    }

    /**
     * Set the background for the overlaying animation
     *
     * @param id animation resource id
     */
    fun setAnimationBackground(@DrawableRes id: Int) {
        if (id != 0) {
            mAnimationBackgroundId = id
            mOverView!!.setBackgroundResource(id)
        }
    }

    /**
     * Start the button animation
     */
    fun startAnimation() {
        mIsAnimating = true
        setupTimer()
        setupAnimation()
        mOverView!!.startAnimation(mScaleAnimation)
        mTimer!!.start()
    }

    private fun setupTimer() {
        mTimer =
            ButtonCountDownTimer(if (mDurationLeft == 0L) mDuration else mDurationLeft, INTERVAL)
    }

    /**
     * Reset button animation
     */
    fun reset() {
        mIsReset = true
        end()
    }

    /**
     * Forcefully end the animation. Note that this is different from
     * [.reset] which resets the animation to the start state
     */
    fun end() {
        if (!mScaleAnimation!!.hasEnded()) {
            mOverView!!.clearAnimation()
            mTimer!!.onFinish()
            mTimer!!.cancel()
        }
        mDurationLeft = mDuration
    }

    /**
     * Set the [ButtonAnimationListener] object to receive callbacks
     *
     * @param listener [ButtonAnimationListener] object
     */
    fun setButtonAnimationListener(listener: ButtonAnimationListener?) {
        mAnimationListener = listener
    }

    override fun onAnimationStart(animation: Animation) {
        mOverView!!.visibility = View.VISIBLE
        mTransparentButton!!.visibility = View.VISIBLE
        mBaseButton!!.isEnabled = false
        if (mAnimationListener != null) {
            mAnimationListener!!.onAnimationStart()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAnimationListener = null
        if (mTimer != null) {
            mTimer!!.onFinish()
        }
    }

    override fun onAnimationEnd(animation: Animation) {
        mOverView!!.visibility = View.GONE
        mTransparentButton!!.visibility = View.GONE
        mBaseButton!!.isEnabled = true
        if (mIsReset) {
            mBaseButton!!.text = mBeforeAnimationText
        } else {
            mBaseButton!!.text = mOnAnimationCompleteText
        }
        mIsReset = false
        mDurationLeft = mDuration
        mIsAnimating = false
    }

    override fun onAnimationRepeat(animation: Animation) {

    }


    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            return performClick()
        }
        performClick()
        return true
    }

    override fun onClick(v: View) {
        performClick()
        // startAnimation();
    }

    private inner class ButtonCountDownTimer internal constructor(
        millisInFuture: Long,
        countDownInterval: Long
    ) : CountDownTimer(millisInFuture, countDownInterval) {

        override fun onTick(millisUntilFinished: Long) {
            val left = (millisUntilFinished / TimeUnit.SECONDS.toMillis(1)).toInt() + 1
            mDurationLeft = left * 1000L
            var formattedString = ""
            if (mDynamicStringId != 0) {
                formattedString = String.format(
                    context.getString(mDynamicStringId), left
                )
            } else {
                formattedString += left
            }
            mBaseButton!!.text = formattedString
            mTransparentButton!!.text = formattedString
        }

        override fun onFinish() {
            if (mAnimationListener != null) {
                if (mIsReset) {
                    mAnimationListener!!.onAnimationReset()
                } else {
                    mAnimationListener!!.onAnimationEnd()
                }
            }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {

        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        mDurationLeft = state.timeInFuture
        mIsAnimating = state.isAnimating

        if (mIsAnimating) {
            startAnimation()
        } else {
            mBaseButton!!.text = state.buttonText
        }

    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        val ss = SavedState(superState!!)
        ss.timeInFuture = mDurationLeft
        ss.width = mOverView!!.width
        ss.isAnimating = mIsAnimating
        ss.buttonText = mBaseButton!!.text.toString()

        return ss
    }

    internal inner class SavedState(superState: Parcelable) : View.BaseSavedState(superState) {

        var timeInFuture: Long = 0
        var width: Int = 0
        var maxWidth: Int = 0
        var isAnimating: Boolean = false
        var buttonText: String? = null

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeLong(timeInFuture)
            out.writeInt(width)
            out.writeInt(maxWidth)
            out.writeByte((if (isAnimating) 1 else 0).toByte())
            out.writeString(buttonText)
        }
    }

    companion object {

        private val INTERVAL = 500L
        private val DEFAULT_TEXT_SIZE = 14
    }
}