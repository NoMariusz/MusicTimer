package com.example.musictimer.services

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.example.musictimer.THEME_TO_DELETE_ID
import com.example.musictimer.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DeleteThemeService : LifecycleService() {
    val mytag = "DeleteThemeService"

    private lateinit var musicViewModel: MusicViewModel

    private var allThemesLoaded = false
    private var allThemeTracksRefLoaded = false
    private var selectedThemeInfoLoaded = false
    private var deletingStarted = false

    override fun onCreate() {
        super.onCreate()
        musicViewModel = MusicViewModel(application)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val themeId = intent?.getIntExtra(THEME_TO_DELETE_ID, -1)
        Log.d(mytag, "onStartCommand: got id $themeId")
        startDeletingTheme(themeId)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startDeletingTheme(themeId: Int?){
        // making observers to needed musicViewModel liveData, and start deleting when all data are
        // loaded
        musicViewModel.allThemes.observe(this, {
            allThemesLoaded = true
            if (checkAllNecessaryLiveDataLoaded()) deleteTheme(themeId)
        })
        musicViewModel.allThemesTracksReferences.observe(this, {
            allThemeTracksRefLoaded = true
            if (checkAllNecessaryLiveDataLoaded()) deleteTheme(themeId)
        })
        musicViewModel.selectedThemeInformationEntities.observe(this, {
            selectedThemeInfoLoaded = true
            if (checkAllNecessaryLiveDataLoaded()) deleteTheme(themeId)
        })
    }

    private fun checkAllNecessaryLiveDataLoaded(): Boolean {
        return (allThemesLoaded && allThemeTracksRefLoaded && selectedThemeInfoLoaded) && !deletingStarted
    }

    private fun deleteTheme(themeId: Int?) = GlobalScope.launch(Dispatchers.IO) {
        // delete theme by musicViewModel and after that stop self
        deletingStarted = true
        Log.d(mytag, "startDeletingTheme(), themeid: $themeId")
        if (themeId != null && themeId != -1){
            musicViewModel.suspendDeleteTheme(themeId.toLong())
        }
        Log.d(mytag, "startDeletingTheme - theme deleted, stopping self")
        stopSelf()
    }
}