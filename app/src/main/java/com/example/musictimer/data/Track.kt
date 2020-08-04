package com.example.musictimer.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tracks", indices = [Index(value = ["name", "value"], unique = true)])
data class Track(@PrimaryKey(autoGenerate = true) var trackId: Long,
                 val name: String,
                 val value: String)     // value is uri string