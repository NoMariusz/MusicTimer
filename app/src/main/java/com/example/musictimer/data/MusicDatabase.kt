package com.example.musictimer.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.musictimer.SELECTED_THEME_INFORMATION_ENTITY_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [MusicTheme::class, Track::class, TracksThemesCrossReference::class, InformationEntity::class],
    version = 2)
abstract class MusicDatabase: RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Simply DROP column not work so, Create the new table
                database.execSQL(
                    "CREATE TABLE themes_new (themeId INTEGER PRIMARY KEY AUTOINCREMENT " +
                            "NOT NULL, name TEXT NOT NULL, loop INTEGER NOT NULL, random INTEGER " +
                            "NOT NULL, isUpdating INTEGER NOT NULL)")
                // Copy the data
                database.execSQL(
                    "INSERT INTO themes_new (themeId, name, loop, random, isUpdating) " +
                            "SELECT themeId, name, loop, random, isUpdating FROM themes")
                // Remove the old table
                database.execSQL("DROP TABLE themes")
                // Change the table name to the correct one
                database.execSQL("ALTER TABLE themes_new RENAME TO themes")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): MusicDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this ){
                val instance = Room.databaseBuilder(context.applicationContext,
                    MusicDatabase::class.java, "music_database")
                    .addCallback(MusicDatabaseCallback(scope)).addMigrations(MIGRATION_1_2).build()
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

        suspend fun initThemes(musicDao: MusicDao) {    // make base theme
            musicDao.addTheme(MusicTheme(
                0, "Quiet", loop = false, random = false, isUpdating = false
            ))
        }

        suspend fun initSelectedTheme(musicDao: MusicDao) {
            musicDao.addBaseInfo(InformationEntity(SELECTED_THEME_INFORMATION_ENTITY_ID, 1))
        }
    }
}