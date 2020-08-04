package com.example.musictimer

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musictimer.data.MusicTheme
import com.example.musictimer.data.Track
import com.example.musictimer.services.DeleteThemeService
import com.example.musictimer.services.UpdateThemeService

class EditThemeViewModel: ViewModel() {
    val mytag = "EditThemeViewModel"

    val updateThemeBinder = MutableLiveData<UpdateThemeService.UpdateThemeBinder>()
    var tracksToSave: ArrayList<Track>? = null
    var tempThemeToSave: MusicTheme? = null

    val deleteThemeServiceConnection: ServiceConnection = DeleteThemeServiceConnection()
    val deleteThemeBinder = MutableLiveData<DeleteThemeService.DeleteThemeBinder>()

    inner class DeleteThemeServiceConnection : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(mytag, "DeleteThemeServiceConnection onServiceConnected()")
            val binder: DeleteThemeService.DeleteThemeBinder? = service as DeleteThemeService.DeleteThemeBinder?
            deleteThemeBinder.postValue(binder)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(mytag, "DeleteThemeServiceConnection onServiceDisconnected()")
            deleteThemeBinder.postValue(null)
        }
    }
}