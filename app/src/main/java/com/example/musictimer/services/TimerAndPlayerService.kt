package com.example.musictimer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.musictimer.TIMER_NOT_STARTED
import com.example.musictimer.mechanisms.MainTimer

class TimerAndPlayerService: Service() {
    private val timerAndPlayerBinder = TimerAndPlayerBinder()
    private val TAG = this::class.simpleName
    val mainTimer = MainTimer()

    init {
        Log.d(TAG, "init: $this")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind: $this")
        return timerAndPlayerBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (mainTimer.timerStatus == TIMER_NOT_STARTED){
            Log.d(TAG, "onUnbind: stopping self")
            stopSelf()
        }
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: $this $mainTimer")
        super.onCreate()
    }

    inner class TimerAndPlayerBinder: Binder(){
        fun getService(): TimerAndPlayerService =  this@TimerAndPlayerService
    }
}