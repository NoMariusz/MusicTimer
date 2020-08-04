package com.example.musictimer

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musictimer.services.DeleteThemeService

class MainActivityViewModel: ViewModel() {
    val mytag = "MainActivityViewModel"
    var isCleaningBaseMade = false

    init {
        Log.d(mytag, " init, this - $this")
    }
    val deleteThemeServiceConnection: ServiceConnection = DeleteThemeServiceConnection()
    val deleteThemeBinder = MutableLiveData<DeleteThemeService.DeleteThemeBinder>()

    inner class DeleteThemeServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(mytag, "DeleteThemeServiceConnection onServiceDisconnected()")
            deleteThemeBinder.postValue(null)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(mytag, "DeleteThemeServiceConnection onServiceConnected()")
            val binder: DeleteThemeService.DeleteThemeBinder? = service as DeleteThemeService.DeleteThemeBinder?
            deleteThemeBinder.postValue(binder)
        }
    }

    override fun onCleared() {
        Log.d(mytag, "onCleared, this - $this")
        super.onCleared()
    }
}