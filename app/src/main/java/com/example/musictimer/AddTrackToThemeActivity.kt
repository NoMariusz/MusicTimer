package com.example.musictimer

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musictimer.data.MusicViewModel
import com.example.musictimer.ui.AddToTrackRecycleAdapter

class AddTrackToThemeActivity : AppCompatActivity() {

    private val mytag = this::class.simpleName

    private var themePosition = SELECTED_THEME_ID_NOT_SET
    private lateinit var recycleAdapter: AddToTrackRecycleAdapter
    private lateinit var musicViewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(mytag, "onCreate")
        setContentView(R.layout.activity_add_track_to_theme)

        actionBar?.title = "Add track"
        supportActionBar?.title = "Add track"

        themePosition = intent.getLongExtra(SELECTED_THEME_ID, SELECTED_THEME_ID_NOT_SET)
        Log.d(mytag, "onCreate - get theme pos $themePosition")

        initRecyclerView()
        initSearching()

        musicViewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
        musicViewModel.setNeededBlankDataObservers(this,
            listOf(musicViewModel.allThemes, musicViewModel.allThemesTracksReferences))
        // update all tracks adapter when viewmodel tracks live data are loaded
        musicViewModel.allTracks.observe(this, { themes ->
            themes?.let {
                val tracks = musicViewModel.allTracks.value
                if (tracks != null){
                    recycleAdapter.setNewAllTracks(tracks)
                    recycleAdapter.notifyDataSetChanged()
                }
            }
        })

        // to not focus at text View at starting Activity
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onPause() {
        super.onPause()
        Log.d(mytag, "onPause - selectedThemeId: $themePosition")
        Log.d(mytag, "onPause - selected tracks size ${recycleAdapter.selectedTracks.size}")
//        saveTracksToBase()
    }

    override fun onBackPressed() {
        prepareResult()
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(mytag, "OnCreateOptionsMenu - start")
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.add_tracks_to_theme_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(mytag, "OnOptionsItemSelected - start")
        when (item.itemId){
            R.id.selectAllBtn -> selectAllVisibleTracks()
            R.id.unselectAllBtn -> unselectAllVisibleTracks()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(mytag, "onSaveInstanceState()")
        // saving state to remember selected tracks
        val newTracksIds = recycleAdapter.selectedTracks.map {
            it.trackId
        }.toLongArray()
        outState.putLongArray(ADD_TRACK_TO_THEME_SAVING_INTENT_TRACKS_IDS, newTracksIds)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(mytag, "onRestoreInstanceState()")
        // load selected tracks from intent
        val selectedTracksIds = savedInstanceState.getLongArray(
            ADD_TRACK_TO_THEME_SAVING_INTENT_TRACKS_IDS)
        Log.d(mytag, "onCreate list from savedInstanceState $selectedTracksIds")
        if (selectedTracksIds != null) updateSelectedTracks(selectedTracksIds)
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.allTracksRV)
        recycleAdapter = AddToTrackRecycleAdapter(this, listOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recycleAdapter
    }

    private fun prepareResult(){
        // preparing result to save selected tracks in editTheme
        Log.d(mytag, "prepareResult() - start")
        val newTracksIds = recycleAdapter.selectedTracks.map {
            it.trackId
        }
        val replyIntent = Intent()
        replyIntent.putExtra(ADD_TRACK_TO_THEME_NEW_TRACKS_ID_ARRAY, newTracksIds.toLongArray())
        Log.d(mytag, "prepareResult() - end, result: ${Activity.RESULT_OK}")
        setResult(Activity.RESULT_OK, replyIntent)
    }

    private fun initSearching(){
        val searchingMT = findViewById<TextView>(R.id.searchMT)
        searchingMT.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    filterElementsInRV(s)
                }
            }

        })
    }

    private fun filterElementsInRV(s: CharSequence){
        val filterAdapterTracks = musicViewModel.allTracks.value?.filter {
            it.name.contains(s, ignoreCase = true)
        } ?: listOf()
        recycleAdapter.setNewAllTracks(filterAdapterTracks.toMutableList())
        recycleAdapter.notifyDataSetChanged()
    }

    private fun selectAllVisibleTracks(){
        val visibleTracks = recycleAdapter.allTracks
        recycleAdapter.selectedTracks.addAll(visibleTracks)
        recycleAdapter.notifyDataSetChanged()
    }

    private fun unselectAllVisibleTracks(){
        val visibleTracks = recycleAdapter.allTracks
        recycleAdapter.selectedTracks.removeAll(visibleTracks)
        recycleAdapter.notifyDataSetChanged()
    }

    private fun updateSelectedTracks(tracksIds: LongArray){
        Log.d(mytag, "updateSelectedTracks(), got array $tracksIds")
        val tracks = musicViewModel.getTracksByIds(tracksIds)
        recycleAdapter.setSelectedTracks(tracks)
        recycleAdapter.notifyDataSetChanged()
    }
}
