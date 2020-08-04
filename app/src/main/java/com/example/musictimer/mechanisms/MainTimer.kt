package com.example.musictimer.mechanisms

import android.annotation.SuppressLint
import android.util.Log
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.musictimer.R
import com.example.musictimer.TIMER_NOT_STARTED
import com.example.musictimer.TIMER_RUNNING
import com.example.musictimer.TIMER_STOPPED
import java.util.*
import kotlin.concurrent.schedule

object MainTimer {
    var parrent: Fragment? = null
    private const val mytag = "MainTimer"

    private var seconds = 0
    private var minutes = 0
    private var miliseconds = 0
    private const val millisecondsPrecision = 100  // how precise is timer tick
    private var mainTimer = Timer()
    var timerStatus = TIMER_NOT_STARTED

    init {
        Log.d(mytag, "init")
    }


    fun startMainTimer(resetTime: Boolean = true) {
        Log.d(mytag, "startMainTimer - start")
        if (resetTime) {
            resetTime()
        }
        mainTimer.schedule(0, millisecondsPrecision.toLong()) {
            timerClick()
        }
        timerStatus =
            TIMER_RUNNING
    }

    private fun timerClick() {
        miliseconds += millisecondsPrecision

        if (miliseconds >= 1000) {
            miliseconds = 0
            seconds++
        }
        if (seconds >= 60){
            seconds = 0
            minutes++
        }
        parrent?.activity?.runOnUiThread {
            loadTimeToUi()
        }
    }

    fun pauseTimer() {
        mainTimer.cancel()
        mainTimer = Timer()
        timerStatus =
            TIMER_STOPPED
    }

    fun loadTimeToUi() {
        val timerText: TextView? = parrent?.activity?.findViewById(
            R.id.timerText
        )
        timerText?.text = "%02d:%02d".format(
            minutes,
            seconds
        )
    }

    fun resetTime() {
        miliseconds = 0
        seconds = 0
        minutes = 0
        timerStatus =
            TIMER_NOT_STARTED
    }
}