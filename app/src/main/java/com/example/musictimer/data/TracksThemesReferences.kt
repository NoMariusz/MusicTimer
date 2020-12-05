package com.example.musictimer.data

import androidx.room.*

@Entity(primaryKeys = ["themeId", "trackId"], tableName = "tracksThemesCrossReference")
data class TracksThemesCrossReference(val themeId: Long, val trackId: Long)

//data class ThemeWithTracks(
//    @Embedded val theme: MusicTheme,
//    @Relation(
//        parentColumn = "themeId",
//        entityColumn = "trackId",
//        associateBy = Junction(TracksThemesCrossReference::class)
//    ) var tracks: List<Track>
//)