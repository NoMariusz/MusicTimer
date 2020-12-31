package com.example.musictimer.ui.theme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musictimer.*
import com.example.musictimer.SELECTED_THEME_ID
import com.example.musictimer.data.MusicTheme
import com.example.musictimer.data.MusicViewModel
import com.example.musictimer.ui.ThemesRecyclerAdapter

class ThemeFragment : Fragment() {

    private val mytag = "ThemeFragment"

    private lateinit var musicViewModel: MusicViewModel
    private val themesRecycleAdapter by lazy {
        this.context?.let {
            ThemesRecyclerAdapter(it,
                musicViewModel.allThemes.value?.toTypedArray() ?: arrayOf(),
                MusicTheme(SELECTED_THEME_ID_NOT_SET, "Error", loop = false, random = false, isUpdating = false),
                musicViewModel)
        }
    }
    private val themesViewManager by lazy {
        GridLayoutManager(this.context, 1)
    }
    private var addingNewThemeStart = false
    private var themesLoaded = false
    private var informationEntitiesLoaded = false


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d(mytag, " onCreateView")
        return inflater.inflate(R.layout.fragment_themes, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(mytag, " onCreate")
        setHasOptionsMenu(true)

        musicViewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
        musicViewModel.allThemes.observe(this, { themes ->
            themes?.let {
                themesLoaded = true
                Log.d(mytag, " musicViewModel.allThemes.observe - themesRecycleAdapter?.setThemes(it.toTypedArray()) - start themes: $it")
                themesRecycleAdapter?.setThemes(it.toTypedArray())
                themesRecycleAdapter?.notifyDataSetChanged()
                if (addingNewThemeStart){
                    // invoke when new theme is added to base and open edit menu for this theme
                    startNewTheme()
                }
                if (informationEntitiesLoaded and themesLoaded){
                    loadSelectedTheme()
                }
            }
        })
        musicViewModel.selectedThemeInformationEntities.observe(this, { themes ->
            themes?.let {
                informationEntitiesLoaded = true
                if (informationEntitiesLoaded and themesLoaded){
                    loadSelectedTheme()
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(mytag, "onViewCreated")

        val themeRecyclerView = getView()?.findViewById<RecyclerView>(R.id.themesRecyclerView)
        themeRecyclerView?.layoutManager = themesViewManager
        themeRecyclerView?.adapter = themesRecycleAdapter
    }

    override fun onResume() {
        super.onResume()
        Log.d(mytag, " onResume - start")
        addingNewThemeStart = false     //to not looping opening last add theme
        themesRecycleAdapter?.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.themes_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addTheme) addNewTheme()
        return super.onOptionsItemSelected(item)
    }

    private fun loadSelectedTheme(){
        // load selected theme info to recycleadapter, after load needed livedata
        themesRecycleAdapter?.setSelectedTheme(musicViewModel.getSelectedTheme())
        themesRecycleAdapter?.notifyDataSetChanged()
    }

    private fun addNewTheme(){
        // add new theme to base and set addingNewThemeStart to load edit theme after add this theme
        val theme = MusicTheme(
            0, "New theme", loop = false, random = false, isUpdating = false
        )
        musicViewModel.addThemeWithNoneTracks(
            theme)
        addingNewThemeStart = true
    }

    private fun startNewTheme(){
        val themeId = musicViewModel.allThemes.value?.last()?.themeId ?: SELECTED_THEME_ID_NOT_SET
        val editNewThemeIntent = Intent(context, EditTheme::class.java)
        editNewThemeIntent.putExtra(SELECTED_THEME_ID, themeId)
        startActivity(editNewThemeIntent)
    }
}
