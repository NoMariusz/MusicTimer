package com.example.musictimer.ui

import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.musictimer.R

class TracksOnEditThemeTouchHelper(tracksAdapter: TracksOnEditThemeTouchHelperAdapter) : ItemTouchHelper.Callback() {

    private var tracksTouchAdapter: TracksOnEditThemeTouchHelperAdapter = tracksAdapter

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.DOWN or ItemTouchHelper.UP
        val swipeFlags = ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        tracksTouchAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        tracksTouchAdapter.onItemSwiped(viewHolder.adapterPosition)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val backgroundLayout = viewHolder.itemView.findViewById<ConstraintLayout>(R.id.constraintLayout)
        backgroundLayout?.setBackgroundColor(Color.WHITE)
        super.clearView(recyclerView, viewHolder)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
//        here you can color item when is start moving
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG){
            val backgroundLayout = viewHolder?.itemView?.findViewById<ConstraintLayout>(
                R.id.constraintLayout
            )
            backgroundLayout?.setBackgroundColor(Color.parseColor("#eeeeee"))
        }
        super.onSelectedChanged(viewHolder, actionState)
    }
}