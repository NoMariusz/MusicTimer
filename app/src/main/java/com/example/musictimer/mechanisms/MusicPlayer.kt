package com.example.musictimer.mechanisms

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.musictimer.ACTUAL_PLAYING_TRACK_NAME_BLANK
import com.example.musictimer.data.MusicTheme
import com.example.musictimer.data.MusicViewModel
import com.example.musictimer.data.Track
import kotlin.random.Random

object MusicPlayer {
    private var parentContext: Context? = null
    private const val mytag = "MusicPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var playList: Array<Track>? = null
    private var playlistTrackIndex: Int = 0
    var selectedTheme: MusicTheme? = null
    enum class IndexOperations {
        INCREASE, DECREASE
    }
    var actualTrackName: MutableLiveData<String?> = MutableLiveData(null)

    private lateinit var musicViewModel: MusicViewModel

    init {
        Log.d(mytag, " - init")
    }

    fun preparePlayer(context: Context, viewModelStoreOwner: ViewModelStoreOwner, lifecycleOwner: LifecycleOwner){
        parentContext = context
        musicViewModel = ViewModelProvider(viewModelStoreOwner).get(MusicViewModel::class.java)
        musicViewModel.setNeededBlankDataObservers(lifecycleOwner, listOf(
            musicViewModel.selectedThemeInformationEntities, musicViewModel.allThemes,
            musicViewModel.allThemesTracksReferences, musicViewModel.allTracks))
    }

    fun loadMusic() {
        Log.d(mytag, "loadMusic - start")
        playlistTrackIndex = 0
        selectedTheme = musicViewModel.getSelectedTheme()
        makeActualPlaylist()
        loadNewTrack()
    }

    private fun loadNewTrack(){
        Log.d(mytag, "loadNewTrack - start")
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()

        if (playList?.size != 0){
            val track = playList?.get(playlistTrackIndex)
            if (track != null) {
                mediaPlayer = MediaPlayer.create(
                    parentContext, Uri.parse(track.value))
                Log.d(mytag, "loadNewTrack: set actualTrackName as ${track.name}")
                actualTrackName.postValue(track.name)
            }
        } else {
            mediaPlayer = null
            Log.d(mytag, "loadNewTrack - blank playlist")
        }

        mediaPlayer?.setOnCompletionListener {
            goToNewTrack(IndexOperations.INCREASE, true)
        }
    }

    private fun goToNewTrack(operation: IndexOperations, forcePlay: Boolean = false){
        val canPlay = changeTrackIndex(operation)

        if (canPlay){
            val mediaPlayerWasPlaying: Boolean = mediaPlayer?.isPlaying == true
            loadNewTrack()
            if (mediaPlayerWasPlaying || forcePlay){
                playMusic()
            }
        } else {
            mediaPlayer?.release()
            mediaPlayer = null
            actualTrackName.postValue(ACTUAL_PLAYING_TRACK_NAME_BLANK)
        }
    }

    private fun changeTrackIndex(operation: IndexOperations): Boolean {
        Log.d(mytag, "changeTrackIndex - start, index $playlistTrackIndex, " +
                "operation: $operation")
        if (playList === null){
            return false
        }

        val outOfRangeAtStart: Boolean = playList?.size!! <= playlistTrackIndex  ||
                playlistTrackIndex < 0

        if (operation == IndexOperations.INCREASE){
            playlistTrackIndex ++
        } else {
            playlistTrackIndex --
        }

        if (selectedTheme?.loop == true){
            if (playList?.size == playlistTrackIndex){
                playlistTrackIndex = 0
            } else if (playlistTrackIndex < 0){
                playlistTrackIndex = playList?.size?.minus(1) ?: 0
            }
        } else {
            if (playList?.size!! <= playlistTrackIndex  || playlistTrackIndex < 0){
                // if before and after operation is out of range, then change value to before state
                if (outOfRangeAtStart){
                    if (operation == IndexOperations.INCREASE){
                        playlistTrackIndex --
                    } else {
                        playlistTrackIndex ++
                    }
                }
                return false
            }
        }
        return true
    }

    private fun makeActualPlaylist(){
        Log.d(mytag, "makeActualPlaylist")
        playList = musicViewModel.getSelectedThemeTracks().toTypedArray()
        if (playList?.size == 0){
            playList = null
        }
        if (selectedTheme?.random == true) {
            myShuffle(playList)
        }
    }

    private fun myShuffle(workArray: Array<Track>?){
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

    fun playMusic() {
        Log.d(mytag, "playMusic - start")
        mediaPlayer?.start()
    }

    fun pauseMusic() {
        Log.d(mytag, "pauseMusic - start")
        if (mediaPlayer?.isPlaying == true){  // to prevent from error, when pause not started track
            mediaPlayer?.pause()
        }
    }

    fun disableMusic() {
        Log.d(mytag, "disableMusic - start")
        mediaPlayer?.release()
        mediaPlayer = null
        actualTrackName.postValue(ACTUAL_PLAYING_TRACK_NAME_BLANK)
    }

    fun nextSong() {
        Log.d(mytag, "nextSong - start")
        goToNewTrack(IndexOperations.INCREASE)
    }

    fun previousSong() {
        Log.d(mytag, "previousSong - start")
        goToNewTrack(IndexOperations.DECREASE)
    }

    fun isPlaying(): Boolean{
        return mediaPlayer != null
    }

}
