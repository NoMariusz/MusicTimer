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
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.example.musictimer.*
import com.example.musictimer.mechanisms.MainTimer
import com.example.musictimer.mechanisms.MediaButtonsBroadcastReceiver
import com.example.musictimer.mechanisms.MusicPlayer


class TimerAndPlayerService: LifecycleService() {
    private val timerAndPlayerBinder = TimerAndPlayerBinder()
    private val TAG = this::class.simpleName
    val mainTimer = ConnectedMusicTimer()
    val musicPlayer = MusicPlayer()

    var isForegroundServiceRunning = false
    private var trackName: String = "Not playing music"

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.d(TAG, "onBind: $this")
        return timerAndPlayerBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind: ")
        if (mainTimer.timerStatus == TIMER_NOT_STARTED){    // stop when not timing
            Log.d(TAG, "onUnbind: stopping self")
            stopSelf()
        }
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: $this")
        musicPlayer.preparePlayer(applicationContext, application, this)
        super.onCreate()
        // to update track name in foreground service notification
        musicPlayer.actualTrackName.observe(this, Observer {
            trackName = if (it == ACTUAL_PLAYING_TRACK_NAME_BLANK || it == null){
                resources.getString(R.string.noneTrackNameLoaded)
            } else {
                it
            }

            if (isForegroundServiceRunning) {
                startForeground()
            }
        })
        // to update start stop button
        musicPlayer.playerStatus.observe(this, Observer {
            if (isForegroundServiceRunning){
                startForeground()
            }
        })
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: $this")
        super.onDestroy()
    }

    private fun startForeground(){
        Log.d(TAG, "startForeground: $this")

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 0
        )
        val builder  =
            NotificationCompat.Builder(this, MAIN_FOREGROUND_SERVICE_CHANNEL_ID)
                .setContentTitle(trackName)
                .setContentText("Theme: ${musicPlayer.selectedTheme?.name}")
                .setSmallIcon(R.drawable.ic_mtlogo)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setColor(ContextCompat.getColor(baseContext, R.color.colorAccent))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setStyle(MediaStyle()
                    .setShowActionsInCompactView(1))

                // Add media control buttons that invoke intents
                .addAction(R.drawable.ic_baseline_skip_previous_48, "Previous",
                    buildMediaIntent(MUSIC_PLAYER_PREVIOUS_TRACK)) // #0
        if (musicPlayer.playerStatus.value == MusicPlayer.PlayerStatuses.PLAYING){
            builder.addAction(R.drawable.ic_baseline_pause_48, "Pause",
                buildMediaIntent(MUSIC_PLAYER_STOP_TRACK)) // #1
        } else {
            builder.addAction(R.drawable.ic_baseline_play_arrow_48, "Play",
                buildMediaIntent(MUSIC_PLAYER_START_TRACK)) // #1
        }
        builder.addAction(R.drawable.ic_baseline_skip_next_48, "Next",
            buildMediaIntent(MUSIC_PLAYER_NEXT_TRACK)) // #2


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
            serviceChannel.setShowBadge(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildMediaIntent(mediaAction: Int): PendingIntent{
        // build intent to broadcast receiver who handle buttons events
        val mediaIntent = Intent(this, MediaButtonsBroadcastReceiver::class.java)
        mediaIntent.putExtra(MEDIA_BUTTON_ACTION, mediaAction)
        return PendingIntent.getBroadcast(this, mediaAction, mediaIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    inner class TimerAndPlayerBinder: Binder(){
        fun getService(): TimerAndPlayerService =  this@TimerAndPlayerService
    }

    // special changed MainTimer to start and stop foreground
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