package com.example.musictimer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.musictimer.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class UpdateThemeService: Service() {
    val mytag = "UpdateThemeService"
    private val updateThemeTracksBinder: IBinder = UpdateThemeBinder()

    private lateinit var musicViewModel: MusicViewModel

    private var updateThemeTracksCalled = false

    override fun onCreate() {
        super.onCreate()
        Log.d(mytag, "onCreate(), this $this")
        musicViewModel = MusicViewModel(application)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return updateThemeTracksBinder
    }

    fun updateThemeTracksAndTheme(lifecycleOwner: LifecycleOwner, themeId: Long, tempTheme: MusicTheme, tracks: List<Track>) {
        // public function to bring service themeWitTracks to save and lifecycleOwner to observe
        // live data. When allThemeTrackCrossReferences are loaded start further operations
        Log.d(mytag, "updateThemeTracks(), start, this $this")
        var allThemesLoaded = false
        var allThemesTracksReferencesLoaded = false
        updateThemeTracksCalled = false     // to re-use service to update theme after call this fun

        musicViewModel.allThemes.observe(lifecycleOwner, Observer {
            allThemesLoaded = true
            Log.d(mytag, "allThemes observer, it ${it.size}, updateThemeTracksCalled $updateThemeTracksCalled, this $this")
            if ((!updateThemeTracksCalled) && allThemesTracksReferencesLoaded){
                updateThemeTracksCalled = true
                updateThemeTracksAndThemeAndStop(themeId, tempTheme, tracks)
            }
        })

        musicViewModel.allThemesTracksReferences.observe(lifecycleOwner, Observer {
            allThemesTracksReferencesLoaded = true
            Log.d(mytag, "allThemeTrackCrossReferences observer, it ${it.size}, updateThemeTracksCalled $updateThemeTracksCalled, this $this")
            if ((!updateThemeTracksCalled) && allThemesLoaded) {
                updateThemeTracksCalled = true
                updateThemeTracksAndThemeAndStop(themeId, tempTheme, tracks)
            }
        })
    }

    private fun updateThemeTracksAndThemeAndStop(themeId: Long, tempTheme: MusicTheme, tracks: List<Track>) =
        GlobalScope.launch(Dispatchers.IO) {
            // update db theme and themeTracks, and stopping self
            Log.d(mytag, "updateThemeTracksAndStop(), theme: $themeId")
            musicViewModel.suspendUpdateThemeAndHisTracks(themeId, tempTheme, tracks)
            Log.d(mytag, "updateThemeTracks() - end updating tracks in viewModel, stopping self")
            killSelf()
        }

    private fun killSelf(){
        Log.d(mytag, "killSelf() - killing self this $this")
        stopSelf()
    }

    inner class UpdateThemeBinder : Binder(){
        fun getService() : UpdateThemeService = this@UpdateThemeService
    }
}