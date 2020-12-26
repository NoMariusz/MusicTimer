package com.example.musictimer.ui.tracks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.musictimer.MainActivityViewModel
import com.example.musictimer.R
import com.example.musictimer.data.MusicViewModel
import com.example.musictimer.services.LoadTracksService

class TracksFragment : Fragment() {
    private val mytag = "TracksFragment"

    private lateinit var musicViewModel: MusicViewModel
    private lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.d(mytag, "onCreateView")
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        Log.d(mytag, "onActivityCreated - start")

        val updateBtn = view?.findViewById<Button>(R.id.updateButton)
        updateBtn?.setOnClickListener {
            updateBtnClicked()
        }

        musicViewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
        musicViewModel.allTracks.observe(viewLifecycleOwner, Observer { tracks ->
            loadTracksAmountToUi(tracks.size)
        })

        // to loadTracksService
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        mainActivityViewModel.loadTracksBinder.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val loadTracksService = it.getService()
//                Log.d(mytag, "onActivityCreated: mainActivityViewModel.loadTracksBinder, get service $loadTracksService")
                loadTracksService.startWork(this, true)
                context?.unbindService(mainActivityViewModel.loadTracksServiceConnection)
            }
        })

    }

    fun modifyUiAtEndUpdate(){
        val updateBtn = activity?.findViewById<Button>(R.id.updateButton)
        val progressLay = activity?.findViewById<ConstraintLayout>(R.id.loadingLayout)
        val progressBar = activity?.findViewById<ProgressBar>(R.id.updateProgressBar)
        activity?.runOnUiThread {
            updateBtn?.visibility = View.VISIBLE
            progressLay?.visibility = View.GONE
            progressBar?.progress = 0
        }
    }

    fun prepareProgressBar(max: Int){
        val progressBar = activity?.findViewById<ProgressBar>(R.id.updateProgressBar)
        progressBar?.max = max + (musicViewModel.allTracks.value?.size ?: 0) + 1
    }

    fun appendToProgressBar(){
        val progressBar = activity?.findViewById<ProgressBar>(R.id.updateProgressBar)
        if (progressBar?.progress != null){
            progressBar.progress += 1
        }
    }

    private fun updateBtnClicked() {
        Log.d(mytag, "updateBtnClicked() - start")
        modifyUiAtStartUpdate()
        // start loadTracksService
        val intent = Intent(activity, LoadTracksService::class.java)
        activity?.startService(intent)
        activity?.bindService(intent, mainActivityViewModel.loadTracksServiceConnection,
            Context.BIND_AUTO_CREATE)
    }

    private fun loadTracksAmountToUi(amount: Int){
        val trackNumberTV = activity?.findViewById<TextView>(R.id.tracksCountTV)
        trackNumberTV?.text = amount.toString()
    }

    private fun modifyUiAtStartUpdate(){
        val updateBtn = activity?.findViewById<Button>(R.id.updateButton)
        updateBtn?.visibility = View.GONE

        val progressLay = activity?.findViewById<ConstraintLayout>(R.id.loadingLayout)
        progressLay?.visibility = View.VISIBLE
    }

}
