package com.example.musictimer.data

import androidx.lifecycle.LiveData
import com.example.musictimer.SELECTED_THEME_INFORMATION_ENTITY_ID

class MusicRepository(private val musicDao: MusicDao) {
    val mytag = "MusicRepository"

    val allTracks: LiveData<List<Track>>
    val allThemes: LiveData<List<MusicTheme>>
    val selectedThemeInformation: LiveData<List<InformationEntity>>
    val allThemeTrackCrossReferences: LiveData<List<TracksThemesCrossReference>>

    init {
        allThemeTrackCrossReferences =  musicDao.getAllThemeTrackReferences()
        selectedThemeInformation = musicDao.getInformationEntitiesLiveData()
        allThemes = musicDao.getAllThemes()
        allTracks = musicDao.getAllTracks()
    }

    suspend fun getThemeById(themeId: Long): MusicTheme {
        return musicDao.getThemeById(themeId)
    }

    suspend fun addTheme(theme: MusicTheme) {
        musicDao.addTheme(theme)
    }

    suspend fun addTrack(track: Track){
        musicDao.addTrack(track)
    }

    suspend fun addTracks(trackList: List<Track>){
        musicDao.addTracks(trackList)
    }

    suspend fun addThemeWithTracksRelations(themeTracksThemesCrossReferences: List<TracksThemesCrossReference>){
        musicDao.addThemeTracksCrossReferences(themeTracksThemesCrossReferences)
    }

    suspend fun deleteTheme(theme: MusicTheme){
        musicDao.deleteTheme(theme)
    }

    suspend fun deleteAllThemeTracks(themeId: Long){
        val themeTrackReference = allThemeTrackCrossReferences.value
        for (ttcr in themeTrackReference!!){
            if (themeId == ttcr.themeId){
                musicDao.deleteThemeWithTracks(ttcr)
            }
        }
    }

    suspend fun deleteTrackWithRefrences(track: Track){
        val themeTrackReference = allThemeTrackCrossReferences.value
        for (ttcr in themeTrackReference!!){
            if (track.trackId == ttcr.trackId){
                musicDao.deleteThemeWithTracks(ttcr)
            }
        }
        musicDao.deleteTrack(track)
    }

    suspend fun updateTheme(theme: MusicTheme){
        musicDao.updateTheme(theme)
    }

    suspend fun updateTrack(track: Track){
        musicDao.updateTrack(track)
    }

    suspend fun updateTracks(tracks: List<Track>){
        musicDao.updateTracks(tracks)
    }

    suspend fun updateThemeTracksReferences(themeId: Long, tracks: List<Track>){
        val themeTracksRef = mutableListOf<TracksThemesCrossReference>()
        for (track in tracks){
            themeTracksRef.add(TracksThemesCrossReference(themeId, track.trackId))
        }
        musicDao.updateThemeTracksReferences(themeId, themeTracksRef)
    }

    suspend fun setSelectedTheme(informationEntity: InformationEntity){
        musicDao.setSelectedTheme(informationEntity)
    }

    suspend fun getMainInfoEntity(): InformationEntity {
        return musicDao.getMainInfoEntity(SELECTED_THEME_INFORMATION_ENTITY_ID)
    }
}
