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
import java.util.concurrent.TimeUnit
import java.util.Calendar

object MainTimer {
    var parrent: Fragment? = null
    private const val mytag = "MainTimer"

    private var seconds: Long = 0
    private var minutes: Long = 0
    private var startTime: Long = 0
    private const val millisecondsPrecision = 100  // how precise is timer tick
    private var mainTimer = Timer()
    var timerStatus = TIMER_NOT_STARTED

    init {
        Log.d(mytag, "init")
    }


    fun startMainTimer(resetTime: Boolean = true) {
        Log.d(mytag, "startMainTimer - start")
        startTime = Calendar.getInstance().time.time
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
        val workingTime: Long = Calendar.getInstance().time.time - startTime

        minutes = TimeUnit.MILLISECONDS.toMinutes(workingTime)
        seconds = TimeUnit.MILLISECONDS.toSeconds(workingTime) - TimeUnit.MINUTES.toSeconds(minutes)

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
        seconds = 0
        minutes = 0
        timerStatus =
            TIMER_NOT_STARTED
    }
}