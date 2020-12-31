package com.example.musictimer.data

import androidx.room.*

@Entity(primaryKeys = ["themeId", "trackId"], tableName = "tracksThemesCrossReference")
data class TracksThemesCrossReference(val themeId: Long, val trackId: Long)
