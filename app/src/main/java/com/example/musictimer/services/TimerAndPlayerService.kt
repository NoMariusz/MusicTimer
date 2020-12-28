package com.example.musictimer.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.musictimer.*
import com.example.musictimer.mechanisms.MainTimer
import com.example.musictimer.mechanisms.MusicPlayer


class TimerAndPlayerService: LifecycleService() {
    private val timerAndPlayerBinder = TimerAndPlayerBinder()
    private val TAG = this::class.simpleName
    val mainTimer = ConnectedMusicTimer()
    val musicPlayer = MusicPlayer()

    var isForegroundServiceRunning = false

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

    private fun startForeground(){
        Log.d(TAG, "startForeground: $this")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val builder  =
            NotificationCompat.Builder(this, MAIN_FOREGROUND_SERVICE_CHANNEL_ID)
                .setContentTitle("MusicTimer")
                .setContentText("Working")
                .setSmallIcon(R.drawable.ic_mtlogo)
                .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            builder.priority = NotificationCompat.PRIORITY_LOW
        }

        val notification: Notification = builder.build()
        startForeground(1, notification)
        isForegroundServiceRunning = true
    }

    private fun stopForeground(){
        Log.d(TAG, "stopForeground: $this")
        stopForeground(true)
        isForegroundServiceRunning = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                MAIN_FOREGROUND_SERVICE_CHANNEL_ID,
                "Playing music",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    inner class TimerAndPlayerBinder: Binder(){
        fun getService(): TimerAndPlayerService =  this@TimerAndPlayerService
    }

    inner class ConnectedMusicTimer: MainTimer(){
        override var timerStatus = TIMER_NOT_STARTED
        set(value){
            if (value == TIMER_NOT_STARTED && isForegroundServiceRunning){
                stopForeground()
            }
            if (value == TIMER_RUNNING && !isForegroundServiceRunning){
                startForeground()
            }
            field = value
        }
    }
}