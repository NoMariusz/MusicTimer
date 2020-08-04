package com.example.musictimer.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MusicDao {
    //notes: Query functions should not be suspend when be used to get LiveData, they shouldn't
    //get info at runtime on ui thread, they may be suspend if they not got LiveData, but objects

    // themes and tracks

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTheme(theme: MusicTheme): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrack(track: Track): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTracks(tracksList: List<Track>): List<Long>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addThemeWithTracks(themesCrossReference: TracksThemesCrossReference): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addThemeTracksCrossReferences(themeTracksReferences :List<TracksThemesCrossReference>)

    @Query("SELECT * FROM themes WHERE themeId = :themeID")
    suspend fun getThemeById(themeID: Long): MusicTheme

    @Query("SELECT * FROM themes")
    fun getAllThemes(): LiveData<List<MusicTheme>>

    @Query("SELECT * FROM tracks")
    fun getAllTracks(): LiveData<List<Track>>

    @Query("SELECT * FROM tracksThemesCrossReference")
    fun getAllThemeTrackReferences(): LiveData<List<TracksThemesCrossReference>>

    @Transaction
    @Delete
    suspend fun deleteTheme(theme: MusicTheme)

    @Transaction
    @Delete
    suspend fun deleteTrack(track: Track)

    @Transaction
    @Delete
    suspend fun deleteThemeWithTracks(themesCrossReference: TracksThemesCrossReference)

    @Query("DELETE FROM tracksThemesCrossReference WHERE themeId = :themeId")
    suspend fun deleteThemeTracksReferences(themeId: Long)

    @Transaction
    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()

    @Transaction
    @Update
    suspend fun updateTheme(theme: MusicTheme)

    @Transaction
    @Update
    suspend fun updateTrack(track: Track)

    @Transaction
    @Update
    suspend fun updateTracks(tracksList: List<Track>)

    @Transaction
    suspend fun updateThemeTracksReferences(themeId: Long, themeTracksReferences: List<TracksThemesCrossReference>){
        deleteThemeTracksReferences(themeId)
        addThemeTracksCrossReferences(themeTracksReferences)
    }

    // selected theme

    @Query(
        "SELECT * FROM informations")
    fun getInformationEntitiesLiveData()
            : LiveData<List<InformationEntity>>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBaseInfo(informationEntity: InformationEntity): Long

    @Transaction
    @Update
    suspend fun setSelectedTheme(informationEntity: InformationEntity)

    @Query("SELECT * FROM informations where infoId = :mainInfoEntityId")
    suspend fun getMainInfoEntity(mainInfoEntityId: Long): InformationEntity
}