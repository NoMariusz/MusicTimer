package com.example.musictimer.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.musictimer.SELECTED_THEME_INFORMATION_ENTITY_ID
import com.example.musictimer.ui.tracks.TracksFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicViewModel(application: Application): AndroidViewModel(application) {

    private val TAG = "MusicViewModel"
    private val repository: MusicRepository
    val allTracks: LiveData<List<Track>>
    val allThemes: LiveData<List<MusicTheme>>
    val selectedThemeInformationEntities: LiveData<List<InformationEntity>>
    val allThemesTracksReferences: LiveData<List<TracksThemesCrossReference>>

    init {
        val musicDao = MusicDatabase.getDatabase(application, viewModelScope).musicDao()
        repository = MusicRepository(musicDao)

        allThemesTracksReferences = repository.allThemeTrackCrossReferences
        allTracks = repository.allTracks
        allThemes = repository.allThemes
        selectedThemeInformationEntities = repository.selectedThemeInformation
    }

    fun setNeededBlankDataObservers(lifecycleOwner: LifecycleOwner,observeDataList: List<LiveData<out List<Any>>>){
        for (observeData in observeDataList){
            observeData.observe(lifecycleOwner, Observer { data -> data?.let {
//                    Log.d("MusicViewModel", "observeData.observe - data changed it $it")
                }
            })
        }
    }

    fun getSelectedTheme(): MusicTheme{
        val selectedThemeId: Long = getMainInfoEntity().selectedThemeId
        return getThemeById(selectedThemeId)
    }

    private fun getTrackById(trackId: Long): Track {
        // do not invoke when allTracks are not updated form base
        for (track in allTracks.value!!){
            if (trackId == track.trackId){
                return track
            }
        }
        throw Exception("MusicVieModel - getTrackById() - can't find trackId $trackId in database")
    }

    private fun getThemeTracks(theme: MusicTheme): List<Track>{
        return getThemeTracksById(theme.themeId)
    }

    fun getThemeTracksById(themeId: Long): List<Track> {
        // do not invoke when allThemesTracksReferences are not updated form base
        val selectedThemeTracks = mutableListOf<Track>()
        for (twt in allThemesTracksReferences.value!!){
            if (twt.themeId == themeId){
                selectedThemeTracks.add(getTrackById(twt.trackId))
            }
        }

        return selectedThemeTracks
    }

    fun getSelectedThemeTracks(): List<Track> {
        val selectedTheme = getSelectedTheme()
        return getThemeTracks(selectedTheme)
    }

    private fun getMainInfoEntity(): InformationEntity {
        // do not invoke when selectedThemeInformationEntities are not updated form base
        for (infoe in selectedThemeInformationEntities.value!!){
            if (infoe.infoId == SELECTED_THEME_INFORMATION_ENTITY_ID){
                return infoe
            }
        }
        throw Exception("MusicVieModel - getMainInfoEntity() - can't find main entity")
    }

    fun getThemeById(themeId: Long): MusicTheme {
        // do not invoke when allThemes are not updated form base
        for (theme in allThemes.value!!){
            if (theme.themeId == themeId){
                return theme
            }
        }
        return MusicTheme(-1, "Non-exist theme", loop = false, random = false, isUpdating = false, isSelfDeleting =  false)
    }

    private fun getFirstTheme(): MusicTheme {
        // do not invoke when allThemes are not updated form base
        for (theme in allThemes.value!!){
            return theme
        }
        throw Exception("MusicVieModel - getFirstTheme() - can't find any theme, theme list in view model is empty")
    }

    private fun getTrackByName(name: String): Track{
        for (track in allTracks.value!!){
            if (track.name == name){
                return track
            }
        }
        throw Exception("MusicVieModel - getTrackByName() - can't find any track")
    }

    private fun getTrackByValue(value: String): Track{
        for (track in allTracks.value!!){
            if (track.value == value){
                return track
            }
        }
        throw Exception("MusicVieModel - getTrackByValue() - can't find any track")
    }

    fun getTracksByIds(tracksIds: LongArray): MutableList<Track> {
        val tracks = mutableListOf<Track>()
        for (trackId in tracksIds){
            tracks.add(getTrackById(trackId))
        }
        return tracks
    }

    suspend fun suspendUpdateThemeAndHisTracks(themeId: Long, tempTheme: MusicTheme, tracks: List<Track>) {
        val theme = repository.getThemeById(themeId)
        Log.d(TAG, "suspend updateThemeWithTracks() - start - theme: $theme")
        theme.isUpdating = true
        theme.name = tempTheme.name
        theme.random = tempTheme.random
        theme.loop = tempTheme.loop
        repository.updateTheme(theme)

        repository.updateThemeTracksReferences(themeId, tracks)

        theme.isUpdating = false
        repository.updateTheme(theme)
        Log.d(TAG, "suspend updateThemeWithTracks() - themeWithTracks end")
    }

    fun addThemeWithTracksByIds(themeTracksIds: Array<Long>, themeId: Long) = viewModelScope.launch(Dispatchers.IO) {
        val theme = repository.getThemeById(themeId)
        Log.d(TAG, "addThemeWithTracksByIds(), themeId: $themeId, got theme: $theme")
        theme.isUpdating = true
        repository.updateTheme(theme)

        val themeTracksCrossRef = mutableListOf<TracksThemesCrossReference>()
        for (id in themeTracksIds){
            themeTracksCrossRef.add(TracksThemesCrossReference(themeId, id))
        }
        repository.addThemeWithTracksRelations(themeTracksCrossRef)
        theme.isUpdating = false
        repository.updateTheme(theme)
    }

    fun setSelectedThemeById(themeId: Long) = viewModelScope.launch(Dispatchers.IO) {
        val mainInfoEntity = repository.getMainInfoEntity()
        mainInfoEntity.selectedThemeId = themeId
        repository.setSelectedTheme(mainInfoEntity)
    }

    fun updateTheme(theme: MusicTheme) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTheme(theme)
    }

    fun addThemeWithNoneTracks(theme: MusicTheme) = viewModelScope.launch(Dispatchers.IO) {
        repository.addTheme(theme)
    }

    suspend fun suspendDeleteTheme(theme: MusicTheme) {
        Log.d(TAG, "suspendDeleteTheme() - theme: $theme")
        theme.isSelfDeleting = true
        repository.updateTheme(theme)
        if (getMainInfoEntity().selectedThemeId == theme.themeId){
            setSelectedThemeById(getFirstTheme().themeId)
        }
        repository.deleteAllThemeTracks(theme.themeId)
        repository.deleteTheme(theme)
        Log.d(TAG, "suspendDeleteTheme() - end deleting theme")
    }

    // special function for updating tracks in LoadTracksService
    suspend fun updateTracks(tracks: List<Track>, parent: TracksFragment?) {
        Log.d(TAG, "updateTracks() - start")
        parent?.prepareProgressBar(tracks.size)
        // deleting all tracks in base without names or values in actual device music scan list
        val namesList = tracks.map { track -> track.name }
        val valuesList = tracks.map { track -> track.value }
        for (aTrack in allTracks.value!!) {
            if ((aTrack.name !in namesList) and (aTrack.value !in valuesList)){
                repository.deleteTrackWithRefrences(aTrack)
            }
            parent?.appendToProgressBar()
        }
        // adding new tracks or updating that with is moved or renamed
        val actualNamesList = allTracks.value?.map { track -> track.name } ?: listOf()
        val actualValuesList = allTracks.value?.map { track -> track.value } ?: listOf()
        val tracksToUpdate = mutableListOf<Track>()
        val tracksToInsert = mutableListOf<Track>()
        for (track in tracks){
            val nameInBase = track.name in actualNamesList
            val uriValueInBase = track.value in actualValuesList
            if (!(nameInBase and uriValueInBase)) {
                when {
                    nameInBase -> {        // on renaming file
                        val actualTrack = getTrackByName(track.name)
                        track.trackId = actualTrack.trackId
                        tracksToUpdate.add(track)
                    }
                    uriValueInBase -> {     // on removing file
                        val actualTrack = getTrackByValue(track.value)
                        track.trackId = actualTrack.trackId
                        tracksToUpdate.add(track)
                    }
                    else -> {               // on adding new file
                        tracksToInsert.add(track)
                    }
                }
            }
            parent?.appendToProgressBar()
        }
        repository.updateTracks(tracksToUpdate)
        repository.addTracks(tracksToInsert)
        parent?.appendToProgressBar()
        parent?.modifyUiAtEndUpdate()
        Log.d(TAG, "updateTracks() - end")
    }
}