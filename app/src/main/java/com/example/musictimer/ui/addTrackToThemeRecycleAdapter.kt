package com.example.musictimer.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musictimer.R
import com.example.musictimer.data.Track

class AddToTrackRecycleAdapter(context: Context, var allTracks: List<Track>)
    : RecyclerView.Adapter<AddToTrackRecycleAdapter.AddToTrackViewHolder>(){

    val selectedTracks = mutableListOf<Track>() // list to store selected themes

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddToTrackViewHolder {
        val itemView = layoutInflater.inflate(R.layout.add_to_track_card, parent, false)
        return AddToTrackViewHolder(itemView, this)
    }

    override fun getItemCount(): Int {
        return allTracks.size
    }

    override fun onBindViewHolder(holder: AddToTrackViewHolder, position: Int) {
        val track = allTracks[position]
        holder.trackName.text = track.name
        holder.track = track
        holder.selectedCB.isChecked = track in selectedTracks
    }

    fun setNewAllTracks(newAllTracks: List<Track>){
        allTracks = newAllTracks
    }

    fun setSelectedTracks(newSelectedTracks: List<Track>){
        selectedTracks.addAll(newSelectedTracks)
    }

    inner class AddToTrackViewHolder(itemView: View, recycleAdapter: AddToTrackRecycleAdapter): RecyclerView.ViewHolder(itemView){

        val selectedCB: CheckBox = itemView.findViewById(R.id.selectTrackCB)
        var trackName: TextView = itemView.findViewById(R.id.trackNameTV)
        lateinit var track: Track

        init {
            selectedCB.setOnClickListener {
                if (selectedCB.isChecked){
                    recycleAdapter.selectedTracks.add(track)
                } else {
                    recycleAdapter.selectedTracks.remove(track)
                }
            }
        }
    }

}