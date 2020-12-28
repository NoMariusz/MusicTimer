package com.example.musictimer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.example.musictimer.TIMER_NOT_STARTED
import com.example.musictimer.mechanisms.MainTimer
import com.example.musictimer.mechanisms.MusicPlayer
import kotlin.math.log

class TimerAndPlayerService: LifecycleService() {
    private val timerAndPlayerBinder = TimerAndPlayerBinder()
    private val TAG = this::class.simpleName
    val mainTimer = MainTimer()
    val musicPlayer = MusicPlayer()

    init {
        Log.d(TAG, "init: $this")
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.d(TAG, "onBind: $this")
        return timerAndPlayerBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind: ")
        if (mainTimer.timerStatus == TIMER_NOT_STARTED){
            Log.d(TAG, "onUnbind: stopping self")
            stopSelf()
        }
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: $this $mainTimer")
        musicPlayer.preparePlayer(applicationContext, application, this)
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: $this")
        super.onDestroy()
    }

    inner class TimerAndPlayerBinder: Binder(){
        fun getService(): TimerAndPlayerService =  this@TimerAndPlayerService
    }
}