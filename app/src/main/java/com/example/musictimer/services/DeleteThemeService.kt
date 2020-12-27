package com.example.musictimer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.musictimer.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class DeleteThemeService : Service() {
    val mytag = "DeleteThemeService"

    private val deleteThemeBinder : IBinder = DeleteThemeBinder()
    private lateinit var musicViewModel: MusicViewModel

    private var allThemesLoaded = false
    private var allThemeTracksRefLoaded = false
    private var selectedThemeInfoLoaded = false
    private var deletingStarted = false

    override fun onCreate() {
        super.onCreate()
        musicViewModel = MusicViewModel(application)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return deleteThemeBinder
    }

    fun startDeletingTheme(theme: MusicTheme, lifecycleOwner: LifecycleOwner){
        // making observers to needed musicViewModel liveData, and start deleting when all data are
        // loaded
        musicViewModel.allThemes.observe(lifecycleOwner, Observer {
            allThemesLoaded = true
            if (checkAllNecessaryLiveDataLoaded()) deleteTheme(theme)
        })
        musicViewModel.allThemesTracksReferences.observe(lifecycleOwner, Observer {
            allThemeTracksRefLoaded = true
            if (checkAllNecessaryLiveDataLoaded()) deleteTheme(theme)
        })
        musicViewModel.selectedThemeInformationEntities.observe(lifecycleOwner, Observer {
            selectedThemeInfoLoaded = true
            if (checkAllNecessaryLiveDataLoaded()) deleteTheme(theme)
        })
    }

    private fun checkAllNecessaryLiveDataLoaded(): Boolean {
        return (allThemesLoaded && allThemeTracksRefLoaded && selectedThemeInfoLoaded) && !deletingStarted
    }

    private fun deleteTheme(theme: MusicTheme) = GlobalScope.launch(Dispatchers.IO) {
        // delete theme by musicViewModel and after that stop self
        deletingStarted = true
        Log.d(mytag, "startDeletingTheme(), theme: $theme")
        musicViewModel.suspendDeleteTheme(theme)
        Log.d(mytag, "startDeletingTheme - theme deleted, stopping self")
        stopSelf()
    }

    inner class DeleteThemeBinder : Binder(){
        fun getService() : DeleteThemeService = this@DeleteThemeService
    }
}