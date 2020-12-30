package com.example.musictimer.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.musictimer.SELECTED_THEME_INFORMATION_ENTITY_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [MusicTheme::class, Track::class, TracksThemesCrossReference::class, InformationEntity::class],
    version = 1)
abstract class MusicDatabase: RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MusicDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this ){
                val instance = Room.databaseBuilder(context.applicationContext,
                    MusicDatabase::class.java, "music_database")
                    .addCallback(MusicDatabaseCallback(scope)).build()
                INSTANCE = instance
                return instance
            }
        }
    }
    
    private class MusicDatabaseCallback(private val scope: CoroutineScope) :
        RoomDatabase.Callback() {
        val mytag = "MusicDatabaseCallback"

        override fun onCreate(db: SupportSQLiteDatabase) {
            Log.d(mytag, "onCreate - start")
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.musicDao())
                }
            }
            Log.d(mytag, "onCreate - stop")
        }

        suspend fun populateDatabase(musicDao: MusicDao) {
            initThemes(musicDao)
            initSelectedTheme(musicDao)
            Log.d(mytag, "populateDatabase - end")
        }

        suspend fun initThemes(musicDao: MusicDao) {
            musicDao.addTheme(MusicTheme(
                0, "Quiet", loop = false, random = false, isUpdating = false,
                isSelfDeleting = false
            ))
        }

        suspend fun initSelectedTheme(musicDao: MusicDao) {
            musicDao.addBaseInfo(InformationEntity(SELECTED_THEME_INFORMATION_ENTITY_ID, 1))
        }
    }
}