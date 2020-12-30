package com.example.musictimer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "themes")
data class MusicTheme(@PrimaryKey(autoGenerate = true) var themeId: Long,
                      var name: String,
                      var loop: Boolean,
                      var random: Boolean,
                      var isUpdating: Boolean
                      )
