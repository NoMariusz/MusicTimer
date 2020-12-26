package com.example.musictimer.services

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.musictimer.data.MusicViewModel
import com.example.musictimer.data.Track
import com.example.musictimer.ui.tracks.TracksFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoadTracksService: Service() {
    private val TAG = "LoadTracksService"
    private val loadTracksBinder: IBinder = LoadTracksBinder()
    private lateinit var parent: Fragment
    private lateinit var musicViewModel: MusicViewModel
    private var neededDataLoadedCount = 0
    private lateinit var tracks: List<Track>
    private var startedFromTracksFragment: Boolean = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: start")
        musicViewModel = MusicViewModel(application)
    }

    override fun onBind(intent: Intent?): IBinder {
//        Log.d(TAG, "onBind: ")
        return loadTracksBinder
    }

    fun startWork(gotParent: Fragment, isStartedFromTracksFragment: Boolean){
        Log.d(TAG, "startWork: isStartedFromTracksFragment $isStartedFromTracksFragment")
        parent = gotParent
        startedFromTracksFragment = isStartedFromTracksFragment
        if (checkPermissions()){    // if has permissions then update tracks
            startUpdatingTracks()
        } else {    // if has not permissions then request it and check again
            parent.requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            if (checkPermissions()) {
                startUpdatingTracks()
            } else {
                stopSelf()
            }
        }
    }

    private fun checkPermissions(): Boolean{
        val context = parent.context
        if (context != null) {
            val checkedPermissions = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            return checkedPermissions == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun startUpdatingTracks(){
        // loading data in musicViewModel
        musicViewModel.allTracks.observe(parent, Observer {
            if (it != null){ dataToUpdateLoaded() }
        })

        musicViewModel.allThemesTracksReferences.observe(parent, Observer {
            if (it != null){ dataToUpdateLoaded() }
        })
        
        // loading tracks data
        tracks = getTracks()
        if (startedFromTracksFragment){
            (parent as TracksFragment).prepareProgressBar(tracks.size)
        }
        dataToUpdateLoaded()
    }
    
    private fun dataToUpdateLoaded(){   // all data must be loaded to make operation
        neededDataLoadedCount ++
        if (neededDataLoadedCount >= 3){
//            Log.d(TAG, "dataToUpdateLoaded: All data loaded")
            updateAndStop()
        }
    }

    private fun updateAndStop() = GlobalScope.launch(Dispatchers.IO) {
        val updateTracksParent =
            if (startedFromTracksFragment) (parent as TracksFragment) else null
        musicViewModel.updateTracks(tracks, updateTracksParent)
        Log.d(TAG, "updateAndStop: updating end - stopping")
        stopSelf()
    }

    private fun getTracks(): MutableList<Track> {
        // Need the READ_EXTERNAL_STORAGE permission if accessing video files that your
        // app didn't create.

        val audioList = mutableListOf<Track>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.IS_MUSIC
        )

        // Display audios in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val contextResolver = parent.context?.contentResolver
        val query = contextResolver?.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} = ?",
            arrayOf("1"),
            sortOrder
        )
        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                // Get values of columns for a given audio.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                audioList.add(Track(0, name, contentUri.toString()))
            }
        }
        Log.d(TAG, "get videos length ${audioList.size}")
        return audioList
    }

    inner class LoadTracksBinder: Binder(){
        fun getService() : LoadTracksService = this@LoadTracksService
    }
}