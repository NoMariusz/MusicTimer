package com.example.musictimer.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class BlockedToUsersScrollingView : HorizontalScrollView {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int):
            super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context): super(context)

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        performClick()
        return false
    }
}