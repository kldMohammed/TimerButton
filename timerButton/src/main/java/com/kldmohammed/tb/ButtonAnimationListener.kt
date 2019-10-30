package com.kldmohammed.tb

interface ButtonAnimationListener {

    /**
     * Callback received when button animation ends. Note that
     * it is different from [.onAnimationReset] which is
     * invoked when the animation is reset to its start state
     */
    fun onAnimationEnd()

    /**
     * Callback received when button animation is reset.
     */
    fun onAnimationReset()

    /**
     * Callback received when button animation starts
     */
    fun onAnimationStart()

}