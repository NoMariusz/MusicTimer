package com.example.musictimer.ui.tracks

import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.musictimer.R
import com.example.musictimer.data.MusicViewModel
import com.example.musictimer.data.Track

class TracksFragment : Fragment() {
    private val mytag = "TracksFragment"

    private lateinit var musicViewModel: MusicViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(mytag, "onCreateView")
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(mytag, "onViewCreated - start")

        val updateBtn = getView()?.findViewById<Button>(R.id.updateButton)
        updateBtn?.setOnClickListener {
            updateBtnClicked()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(mytag, "onActivityCreated - start")

        musicViewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
        musicViewModel.allTracks.observe(viewLifecycleOwner, Observer { tracks ->
            loadTracksAmountToUi(tracks.size)
        })
        musicViewModel.setNeededBlankDataObservers(viewLifecycleOwner, listOf(musicViewModel.allThemesTracksReferences))

        // updating tracks observer
        musicViewModel.viewModelOperationEnded.observe(viewLifecycleOwner, Observer { value ->
            if (value == true){
                modifyUiAtEndUpdate()
            }
        })

    }

    private fun updateBtnClicked() {
        Log.d(mytag, "updateBtnClicked() - start")
        checkPerrmisions()
    }

    private fun checkPerrmisions(){
        if (context != null){
            val checkedPerrmisions = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
            if (checkedPerrmisions == PackageManager.PERMISSION_GRANTED){
                Log.d(mytag, "checkPerrmisions() - PERMISSION_GRANTED")
                startUpdatingTracks()
            } else {
                Log.d(mytag, "checkPerrmisions() - PERMISSION_DENIED")
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }
    }

    private fun startUpdatingTracks(){
        modifyUiAtStartUpdate()
        val tracks = getTracks()
        prepareProgressBar(tracks.size)
        updateTracksInBase(tracks)
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

        val contextResolver = context?.contentResolver
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
        Log.d(mytag, "get videos length ${audioList.size}")
        return audioList
    }

    private fun loadTracksAmountToUi(amount: Int){
        val trackNumberTV = activity?.findViewById<TextView>(R.id.tracksCountTV)
        trackNumberTV?.text = amount.toString()
    }

    private fun updateTracksInBase(tracks: List<Track>){
        musicViewModel.updateTracks(tracks, this)
    }

    private fun modifyUiAtStartUpdate(){
        Log.d(mytag, "modifyUiAtStartUpdate()")
        val updateBtn = activity?.findViewById<Button>(R.id.updateButton)
        updateBtn?.visibility = Button.GONE

        val progressLay = activity?.findViewById<ConstraintLayout>(R.id.loadingLayout)
        progressLay?.visibility = ConstraintLayout.VISIBLE
    }

    private fun modifyUiAtEndUpdate(){
        Log.d(mytag, "modifyUiAtEndUpdate()")
        val updateBtn = activity?.findViewById<Button>(R.id.updateButton)
        updateBtn?.visibility = Button.VISIBLE

        val progressLay = activity?.findViewById<ConstraintLayout>(R.id.loadingLayout)
        progressLay?.visibility = ConstraintLayout.GONE
    }

    private fun prepareProgressBar(max: Int){
        val progressBar = activity?.findViewById<ProgressBar>(R.id.updateProgressBar)
        progressBar?.progress = 0
        progressBar?.max = max + (musicViewModel.allTracks.value?.size ?: 0) + 1
    }

    fun appendToProgressBar(){
        val progressBar = activity?.findViewById<ProgressBar>(R.id.updateProgressBar)
        if (progressBar?.progress != null){
            progressBar.progress += 1
        }
    }
}
