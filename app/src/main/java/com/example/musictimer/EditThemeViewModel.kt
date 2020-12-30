package com.example.musictimer

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musictimer.data.MusicTheme
import com.example.musictimer.data.Track
import com.example.musictimer.services.UpdateThemeService

class EditThemeViewModel: ViewModel() {
    val mytag = "EditThemeViewModel"

    var tracksToSave: ArrayList<Track>? = null
    var tempThemeToSave: MusicTheme? = null

    val updateThemeServiceConnection: ServiceConnection = UpdateThemeServiceConnection()
    val updateThemeBinder = MutableLiveData<UpdateThemeService.UpdateThemeBinder>()

    inner class UpdateThemeServiceConnection: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            Log.d(mytag, "UpdateThemeServiceConnection onServiceConnected()")
            val binder: UpdateThemeService.UpdateThemeBinder? = service
                    as UpdateThemeService.UpdateThemeBinder?
            updateThemeBinder.postValue(binder)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
//            Log.d(mytag, "UpdateThemeServiceConnection onServiceDisconnected()")
            updateThemeBinder.postValue(null)
        }
    }
}