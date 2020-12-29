package com.example.musictimer.mechanisms

import android.content.*
import android.util.Log
import com.example.musictimer.*
import com.example.musictimer.services.TimerAndPlayerService

class MediaButtonsBroadcastReceiver: BroadcastReceiver() {
    val TAG = this::class.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        val action: Int? = intent?.getIntExtra(MEDIA_BUTTON_ACTION , -1)
//        Log.d(TAG, "onReceive: action $action")
        val serviceIntent = Intent(context, TimerAndPlayerService::class.java)
        val binder = peekService(context, serviceIntent) as TimerAndPlayerService.TimerAndPlayerBinder
        val musicPlayer = binder.getService().musicPlayer
        when (action) {
            MUSIC_PLAYER_PREVIOUS_TRACK -> {
                musicPlayer.previousSong()
            }
            MUSIC_PLAYER_NEXT_TRACK -> {
                musicPlayer.nextSong()
            }
            MUSIC_PLAYER_STOP_TRACK -> {
                if (musicPlayer.isPlaying()){
                    musicPlayer.pauseMusic()
                }
            }
            MUSIC_PLAYER_START_TRACK -> {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.playMusic()
                }
            }
        }

    }
}