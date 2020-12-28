package com.example.musictimer

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musictimer.services.DeleteThemeService
import com.example.musictimer.services.LoadTracksService
import com.example.musictimer.services.TimerAndPlayerService

class MainActivityViewModel: ViewModel() {
    val mytag = "MainActivityViewModel"
    var isCleaningBaseMade = false

    val deleteThemeServiceConnection: ServiceConnection = DeleteThemeServiceConnection()
    val deleteThemeBinder = MutableLiveData<DeleteThemeService.DeleteThemeBinder>()

    val loadTracksServiceConnection: ServiceConnection = LoadTracksServiceConnection()
    val loadTracksBinder = MutableLiveData<LoadTracksService.LoadTracksBinder>()

    val timerAndPlayerServiceConnection: ServiceConnection = TimerAndPlayerServiceConnection()
    val timerAndPlayerBinder = MutableLiveData<TimerAndPlayerService.TimerAndPlayerBinder>()

    inner class DeleteThemeServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
//            Log.d(mytag, "DeleteThemeServiceConnection onServiceDisconnected()")
            deleteThemeBinder.postValue(null)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            Log.d(mytag, "DeleteThemeServiceConnection onServiceConnected()")
            val binder: DeleteThemeService.DeleteThemeBinder? = service as DeleteThemeService.DeleteThemeBinder?
            deleteThemeBinder.postValue(binder)
        }
    }

    inner class LoadTracksServiceConnection: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            Log.d(mytag, "LoadTracksServiceConnection onServiceConnected()")
            val binder: LoadTracksService.LoadTracksBinder? = service as LoadTracksService.LoadTracksBinder?
            loadTracksBinder.postValue(binder)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
//            Log.d(mytag, "LoadTracksServiceConnection onServiceDisconnected()")
            loadTracksBinder.postValue(null)
        }
    }

    inner class TimerAndPlayerServiceConnection: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: TimerAndPlayerService.TimerAndPlayerBinder? = service as TimerAndPlayerService.TimerAndPlayerBinder?
            Log.d(mytag, "TimerAndPlayerServiceConnection onServiceConnected() ${binder?.getService()}")
            timerAndPlayerBinder.postValue(binder)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(mytag, "TimerAndPlayerServiceConnection onServiceDisconnected()")
            timerAndPlayerBinder.postValue(null)
        }
    }

    override fun onCleared() {
//        Log.d(mytag, "onCleared, this - $this")
        super.onCleared()
    }
}