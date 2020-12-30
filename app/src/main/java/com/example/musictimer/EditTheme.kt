package com.example.musictimer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musictimer.data.MusicTheme
import com.example.musictimer.data.MusicViewModel
import com.example.musictimer.services.DeleteThemeService
import com.example.musictimer.services.UpdateThemeService
import com.example.musictimer.ui.TracksOnEditThemeTouchHelper
import com.example.musictimer.ui.TracksRecycleAdapter


class EditTheme : AppCompatActivity() {

    private val mytag = this::class.simpleName
    private val tracksViewManager by lazy {
        LinearLayoutManager(this)
    }
    private var themePosition = SELECTED_THEME_ID_NOT_SET
    private lateinit var tracksAdapter: TracksRecycleAdapter

    private var tracksBaseLoaded = false
    private var themeTracksReferenceBaseLoaded = false
    private var themesBaseLoaded = false

    private lateinit var musicViewModel: MusicViewModel

    private lateinit var editThemeViewModel: EditThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(mytag, "onCreate - this $this")
        setContentView(R.layout.activity_edit_theme)

        actionBar?.title = "Edit theme"
        supportActionBar?.title = "Edit theme"
        makeClickListenerToAddTracks()

        themePosition = intent.getLongExtra(SELECTED_THEME_ID, SELECTED_THEME_ID_NOT_SET)
        Log.d(mytag, "onCreate - get theme pos $themePosition")

        tracksAdapter = TracksRecycleAdapter(this, arrayListOf())

        musicViewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
        musicViewModel.setNeededBlankDataObservers(this,
            listOf(musicViewModel.selectedThemeInformationEntities))
        // observers are set to update ui when livedata from viewmodel are loaded
        musicViewModel.allThemesTracksReferences.observe(this, Observer { themes ->
            themes?.let {
                themeTracksReferenceBaseLoaded = true
                updateThemeTracksOnAdapter()
            }
        })
        musicViewModel.allTracks.observe(this, Observer { themes ->
            themes?.let {
                tracksBaseLoaded = true
                updateThemeTracksOnAdapter()
            }
        })
        musicViewModel.allThemes.observe(this, Observer { themes ->
            themes?.let {
                themesBaseLoaded = true
                loadThemeToUI()
                updateThemeTracksOnAdapter()
            }
        })

        //to update theme with service
        editThemeViewModel = ViewModelProvider(this).get(EditThemeViewModel::class.java)

        editThemeViewModel.updateThemeBinder.observe(this, Observer{
            if (it != null){
                val updateThemeService = it.getService()
                Log.d(mytag, "editThemeViewModel.UpdateThemeBinder, get service $updateThemeService")
                if (editThemeViewModel.tempThemeToSave != null && editThemeViewModel.tracksToSave != null){
                    Log.d(mytag, "editThemeViewModel.UpdateThemeBinder, updatingTheme")
                    updateThemeService.updateThemeTracksAndTheme(this, themePosition,
                        editThemeViewModel.tempThemeToSave!!, editThemeViewModel.tracksToSave!!)
                    editThemeViewModel.tempThemeToSave = null
                    editThemeViewModel.tracksToSave = null
                }
                unbindService(editThemeViewModel.updateThemeServiceConnection)
            }
        })

        initRecyclerView()
    }

    override fun onPause() {
        super.onPause()
        Log.d(mytag, "onPause() - this $this")
        if (themePosition != SELECTED_THEME_ID_NOT_SET){
            startUpdateThemeAndReferences()
        }
    }

    override fun onResume() {
        super.onResume()
        tracksAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        Log.d(mytag, "OnCreateOptionsMenu - start")
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.edit_theme_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Log.d(mytag, "OnOptionsItemSelected - start")
        if (item.itemId == R.id.deleteTheme){
            deleteThemeBtnClick()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(mytag, "onActivityResult() - request: $requestCode, result: $resultCode, actual theme ID: $themePosition")
        if ((requestCode == ADD_TRACK_TO_THEME_REQUEST_CODE) && (resultCode == Activity.RESULT_OK)){
            val newTracksIds = data?.getLongArrayExtra(ADD_TRACK_TO_THEME_NEW_TRACKS_ID_ARRAY)
            if (newTracksIds != null) {
                addSelectedTracksToTheme(newTracksIds.toTypedArray())
            }
        }
    }

    private fun loadThemeToUI(){
        val theme = musicViewModel.getThemeById(themePosition)

        findViewById<TextView>(R.id.themeNameMT).text = theme.name
        findViewById<Switch>(R.id.loopSwitch).isChecked = theme.loop
        findViewById<Switch>(R.id.randomSwitch).isChecked = theme.random
        findViewById<TextView>(R.id.isUpdatingTextView).visibility =
            if (theme.isUpdating) TextView.VISIBLE else TextView.GONE

        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun updateThemeTracksOnAdapter(){
        // Update theme tracks adapter only when all needed livedatas are  loaded
        if (themeTracksReferenceBaseLoaded and tracksBaseLoaded and themesBaseLoaded){
            tracksAdapter.setDataSet(ArrayList(musicViewModel.getThemeTracksById(themePosition)))
            Log.d(mytag, "updateThemeTracksOnAdapter() - adapter tracks size ${tracksAdapter.trackList.size}")
            tracksAdapter.notifyDataSetChanged()
        }
    }

    private fun initRecyclerView(){
        val tracksRecyclerView = findViewById<RecyclerView>(R.id.tracksRecyclerView)

        val callback: ItemTouchHelper.Callback =
            TracksOnEditThemeTouchHelper(tracksAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        tracksAdapter.setTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(tracksRecyclerView)

        tracksRecyclerView.layoutManager = tracksViewManager
        tracksRecyclerView.adapter = tracksAdapter
    }

    private fun startUpdateThemeAndReferences(){
        Log.d(mytag, "prepareToUpdateThemeAndReferences(), this $this")
        editThemeViewModel.tempThemeToSave = MusicTheme(SELECTED_THEME_ID_NOT_SET,
            findViewById<TextView>(R.id.themeNameMT).text.toString(),
            findViewById<Switch>(R.id.loopSwitch).isChecked,
            findViewById<Switch>(R.id.randomSwitch).isChecked,
            isUpdating = false
        )
        editThemeViewModel.tracksToSave = tracksAdapter.trackList

        val intent = Intent(this, UpdateThemeService::class.java)
        startService(intent)
        bindService(intent, editThemeViewModel.updateThemeServiceConnection, 0)

        Log.d(mytag, "saveTracksFromRecyclerView() - end, service start binding")
    }

    private fun makeClickListenerToAddTracks(){
        val addBtn = findViewById<ImageView>(R.id.addTrackIV)
        addBtn.setOnClickListener {
            startAddingTracksToTheme()
        }
    }

    private fun startAddingTracksToTheme(){
        val addTrackToThemeActivityIntent = Intent(
            this@EditTheme, AddTrackToThemeActivity::class.java)
        addTrackToThemeActivityIntent.putExtra(SELECTED_THEME_ID, themePosition)
        startActivityForResult(addTrackToThemeActivityIntent, ADD_TRACK_TO_THEME_REQUEST_CODE)
    }

    private fun addSelectedTracksToTheme(newTracksIds: Array<Long>){
        Log.d(mytag, "addSelectedTracksToTheme - adding new tracks")
        musicViewModel.addThemeWithTracksByIds(newTracksIds, themePosition)
    }

    private fun deleteThemeBtnClick(){
        AlertDialog.Builder(this).setTitle("Delete theme")
            .setMessage("Are you sure to delete this theme?")
            .setPositiveButton(R.string.editThemeDeleteThemeDialogYes) { _, _ ->
                deleteActualTheme()
            }
            .setNegativeButton(R.string.editThemeDeleteThemeDialogNo, null).show()
    }

    private fun deleteActualTheme() {
        Log.d(mytag, "deleteActualTheme - start, theme id = $themePosition")
        startDeletingThemeService()
        themePosition = SELECTED_THEME_ID_NOT_SET
        finish()
    }

    private fun startDeletingThemeService(){
        val intent = Intent(this, DeleteThemeService::class.java)
        intent.putExtra(THEME_TO_DELETE_ID, themePosition.toInt())
        startService(intent)
        Log.d(mytag, "startDeletingThemeService() - end")
    }
}
