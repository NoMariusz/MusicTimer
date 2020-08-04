package com.example.musictimer.ui

import android.content.Context
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.musictimer.R
import com.example.musictimer.data.Track

class TracksRecycleAdapter(context: Context, var trackList: ArrayList<Track>)
    : RecyclerView.Adapter<TracksRecycleAdapter.TracksViewHolder>(),
    TracksOnEditThemeTouchHelperAdapter {

    private val layoutInflater = LayoutInflater.from(context)

    private lateinit var tracksTouchHelper: ItemTouchHelper

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : TracksViewHolder {
        val itemView = layoutInflater.inflate(R.layout.track_card, parent, false)
        return TracksViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return trackList.size
    }

    override fun onBindViewHolder(holder: TracksViewHolder, position: Int) {
        holder.trackNameTV.text = trackList[position].name
        holder.trackPosition = position
    }

    override fun onItemMove(startPosition: Int, endPosition: Int) {
        val fromTrack = trackList[startPosition]
        trackList.removeAt(startPosition)
        trackList.add(endPosition, fromTrack)
        notifyItemMoved(startPosition, endPosition) // may to change with notifyDataSetChanged
    }

    override fun onItemSwiped(position: Int) {
        trackList.removeAt(position)
        notifyItemRemoved(position) // may to change with notifyDataSetChanged
    }

    fun setTouchHelper(touchHelper: ItemTouchHelper){
        tracksTouchHelper = touchHelper
    }

    fun setDataSet(newTrackList: ArrayList<Track>){
        trackList = newTrackList
    }


    inner class TracksViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
        , View.OnTouchListener, GestureDetector.OnGestureListener{

        var trackPosition = 0
        val trackNameTV: TextView = itemView.findViewById(R.id.trackNameTV)
        private val gestureDetector = GestureDetector(itemView.context, this)

        init {
            itemView.setOnTouchListener(this)
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            gestureDetector.onTouchEvent(event)
            return true
        }

        override fun onShowPress(e: MotionEvent?) {
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return false
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return false
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return false
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent?) {
            tracksTouchHelper.startDrag(this)
        }
    }

}