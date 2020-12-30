package com.example.musictimer.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musictimer.EditTheme
import com.example.musictimer.R
import com.example.musictimer.SELECTED_THEME_ID
import com.example.musictimer.SELECTED_THEME_ID_NOT_SET
import com.example.musictimer.data.MusicTheme
import com.example.musictimer.data.MusicViewModel

class ThemesRecyclerAdapter(private val context: Context, private var themesList: Array<MusicTheme>,
                            private var selectedTheme: MusicTheme,
                            private val musicViewModel: MusicViewModel)
    : RecyclerView.Adapter<ThemesRecyclerAdapter.MyViewHolder>() {

    private val mytag = "ThemesRecyclerAdapter"
    private val layoutInflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = layoutInflater.inflate(R.layout.theme_card_list, parent, false)
        return MyViewHolder(itemView, this)
    }

    override fun getItemCount() = themesList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val theme = themesList[position]
        holder.themeId = theme.themeId
        holder.cardTitleTextView?.text = theme.name
        holder.isUpdatingTextView?.visibility = if(theme.isUpdating) View.VISIBLE else View.GONE
        if (!theme.isUpdating){
            holder.itemView.setOnLongClickListener {
                val editThemeActivityIntent = Intent(context, EditTheme::class.java)
                editThemeActivityIntent.putExtra(SELECTED_THEME_ID, theme.themeId)
                Log.d(mytag, "LongClick - starting edit Theme position ${theme.themeId}")
                context.startActivity(editThemeActivityIntent)
                true
            }
        } else {
            holder.itemView.setOnLongClickListener(null)
        }

        if (theme.random){
            colorByFilter(holder.randomImageView,
                R.color.colorPrimary
            )
        }
        else {
            colorByFilter(holder.randomImageView,
                R.color.disabledGrey
            )
        }

        if (theme.loop){
            colorByFilter(holder.loopImageView,
                R.color.colorPrimary
            )
        }
        else {
            colorByFilter(holder.loopImageView,
                R.color.disabledGrey
            )
        }

        holder.isSelectedCheckButton?.isChecked = theme == selectedTheme
    }

    private fun colorByFilter(item: ImageView?, color: Int){
        item?.setColorFilter(
            ContextCompat.getColor(context, color), android.graphics.PorterDuff.Mode.SRC_IN)
    }

    fun setThemes(gThemesList: Array<MusicTheme>){
        themesList = gThemesList
    }

    fun setSelectedTheme(theme: MusicTheme){
        selectedTheme = theme
    }


    inner class MyViewHolder(itemView : View, adapter: ThemesRecyclerAdapter)
        : RecyclerView.ViewHolder(itemView){
        val cardTitleTextView = itemView.findViewById<TextView?>(R.id.cardTitleTextView)
        val loopImageView = itemView.findViewById<ImageView?>(R.id.loopImageView)
        val randomImageView = itemView.findViewById<ImageView?>(R.id.randomImageView)
        val isSelectedCheckButton = itemView.findViewById<CheckBox?>(R.id.checkBox)
        val isUpdatingTextView = itemView.findViewById<TextView?>(R.id.isUpdatingTextView)
        var themeId: Long = SELECTED_THEME_ID_NOT_SET

        init {
            isSelectedCheckButton?.setOnClickListener {
                musicViewModel.setSelectedThemeById(themeId)
                adapter.notifyDataSetChanged()
            }
        }
    }
}