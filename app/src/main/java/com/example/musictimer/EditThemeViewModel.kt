package com.example.musictimer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musictimer.data.MusicTheme
import com.example.musictimer.data.Track
import com.example.musictimer.services.UpdateThemeService

class EditThemeViewModel: ViewModel() {
    val mytag = "EditThemeViewModel"

    val updateThemeBinder = MutableLiveData<UpdateThemeService.UpdateThemeBinder>()
    var tracksToSave: ArrayList<Track>? = null
    var tempThemeToSave: MusicTheme? = null
}