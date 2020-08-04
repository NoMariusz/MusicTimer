package com.example.musictimer.mechanisms

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.musictimer.data.MusicViewModel
import kotlin.random.Random

object MusicPlayer {
    private var parrentContext: Context? = null
    private const val mytag = "MusicPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var playList: Array<String>? = null

    private lateinit var musicViewModel: MusicViewModel

    init {
        Log.d(mytag, " - init")
    }

    fun preparePlayer(context: Context, viewModelStoreOwner: ViewModelStoreOwner, lifecycleOwner: LifecycleOwner){
        parrentContext = context
        musicViewModel = ViewModelProvider(viewModelStoreOwner).get(MusicViewModel::class.java)
        musicViewModel.setNeededBlankDataObservers(lifecycleOwner, listOf(
            musicViewModel.selectedThemeInformationEntities, musicViewModel.allThemes,
            musicViewModel.allThemesTracksReferences, musicViewModel.allTracks))
    }

    fun loadMusic() {
        Log.d(mytag, "loadMusic - start")
        makeActualPlaylist()
        loadNewTrack()
    }

    private fun loadNewTrack(){
        Log.d(mytag, "loadNewTrack - start")
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()

        if (playList?.size != 0){
            val track = playList?.get(0)
            if (track != null) {
                mediaPlayer = MediaPlayer.create(
                    parrentContext, Uri.parse(track))
            }
        } else {
            mediaPlayer = null
            Log.d(mytag, "loadNewTrack - blank playlist")
        }

        mediaPlayer?.setOnCompletionListener {
            trackEnd()
        }
    }

    fun playMusic() {
        Log.d(mytag, "playMusic - start")
        mediaPlayer?.start()
    }

    fun pauseMusic() {
        Log.d(mytag, "pauseMusic - start")
        mediaPlayer?.pause()
    }

    fun disableMusic() {
        Log.d(mytag, "disableMusic - start")
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun trackEnd(){
        Log.d(mytag, "trackEnd - start")
        Log.d(mytag, "before drop ${playList?.size}")
        playList = playList?.drop(1)?.toTypedArray()
        Log.d(mytag, "after drop ${playList?.size}")

        checkPlaylistLoop()

        loadNewTrack()

        if (playList?.size != 0){
            playMusic()
        }
    }

    private fun checkPlaylistLoop() {
        Log.d(mytag, "checkPlaylistLoop")
        val selectedTheme =
            musicViewModel.getSelectedTheme()
        if (selectedTheme.loop){
            if (playList?.size == 0){
                makeActualPlaylist()
            }
        }
    }

    private fun makeActualPlaylist(){
        Log.d(mytag, "makeActualPlaylist")
        val selectedTheme = musicViewModel.getSelectedTheme()
        playList = musicViewModel.getSelectedThemeTracksValues().toTypedArray()
        if (selectedTheme.random) {
            myShuffle(playList)
        }
    }

    private fun myShuffle(workArray: Array<String>?){
        if (workArray != null){
            // Fisher-Yates shuffle algorithm
            for (i in workArray.size - 1 downTo 1) {
                val j = Random.nextInt(i + 1)
                val temp = workArray[i]
                workArray[i] = workArray[j]
                workArray[j] = temp
            }
        }
    }

}
