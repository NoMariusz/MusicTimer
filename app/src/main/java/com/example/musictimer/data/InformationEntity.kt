package com.example.musictimer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "informations")
data class InformationEntity(@PrimaryKey(autoGenerate = true) var infoId: Long, var selectedThemeId: Long)