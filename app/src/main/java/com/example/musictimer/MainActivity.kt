package com.example.musictimer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.musictimer.data.MusicTheme
import com.example.musictimer.data.MusicViewModel
import com.example.musictimer.mechanisms.MusicPlayer
import com.example.musictimer.services.DeleteThemeService
import com.example.musictimer.services.LoadTracksService
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {
    private val mytag = "MainActivity"

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var musicViewModel: MusicViewModel

    private var deleteThemeService: DeleteThemeService? = null
    private var themeToDelete: MusicTheme? = null
    private lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        musicViewModel = ViewModelProvider(this).get(MusicViewModel::class.java)
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        // observer is necessary to force open database and populate it, but his work is clean base
        // after errors made by killing app when base services work, cleaning only once at open app
        musicViewModel.allThemes.observe(this, Observer { themes ->
            themes?.let {
                Log.d(mytag, "musicViewModel.allThemes.observe - themes: $themes, isCleaningBaseMade: ${mainActivityViewModel.isCleaningBaseMade}")
                if(!mainActivityViewModel.isCleaningBaseMade) {
                    mainActivityViewModel.isCleaningBaseMade = true
                    for (theme in it) {
                        if (theme.isUpdating) {
                            Log.d(mytag, "In base are not updated themes, themes: $it")
                            theme.isUpdating = false
                            musicViewModel.updateTheme(theme)
                        }
                        if (theme.isSelfDeleting) {
                            Log.d(mytag, "In base are not deleted themes, themes: $it")
                            themeToDelete = theme
                            startDeletingThemeService()
                        }
                    }
                }
            }
        })
        //to delete theme as service and finish
        mainActivityViewModel.deleteThemeBinder.observe(this, Observer{
            if (it != null){
                deleteThemeService = it.getService()
                Log.d(mytag, "editThemeViewModel.deleteThemeBinder, get service $deleteThemeService, themeToDelete $themeToDelete")
                themeToDelete?.let { it1 -> deleteThemeService?.startDeletingTheme(it1, this) }
                unbindService(mainActivityViewModel.deleteThemeServiceConnection)
            }
        })

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.tracksFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        startUpdatingTracksService()
    }

    override fun onPause() {
        Log.d(mytag, "onPause")
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        Log.d(mytag, "Destroy")
        super.onDestroy()
    }

    private fun startDeletingThemeService(){
        val intent = Intent(this, DeleteThemeService::class.java)
        startService(intent)
        bindService(intent, mainActivityViewModel.deleteThemeServiceConnection, Context.BIND_AUTO_CREATE)
        Log.d(mytag, "startDeletingThemeService() - end, service start binding")
    }

    private fun startUpdatingTracksService() {  // set alarm that fires service to update tracks
        Log.d(mytag, "startUpdatingTracksService: start")
        val alarmMgr: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent: PendingIntent = Intent(applicationContext, LoadTracksService::class.java).let { intent ->
            intent.putExtra(LOAD_TRACKS_SERVICE_STARTED_FROM_ALARM, true)
            PendingIntent.getService(applicationContext, 0, intent, 0)
        }

//        alarmMgr.cancel(alarmIntent)  // cancel to override existing alarm
        alarmMgr.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            DateUtils.WEEK_IN_MILLIS,
            alarmIntent
        )
    }
}
