package com.example.musictimer.ui.home

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.musictimer.*
import com.example.musictimer.TIMER_RUNNING
import com.example.musictimer.TIMER_STOPPED
import com.example.musictimer.mechanisms.MainTimer
import com.example.musictimer.mechanisms.MusicPlayer

class HomeFragment : Fragment() {

    private val mytag = "HomeFragment"
    private var musicManageVisible = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d(mytag, "onCreateView")
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(mytag, "onViewCreated - start")

        val rightBtn: Button? = view.findViewById(R.id.rightBtn)
        val showMusicManagerIV: ImageView? = view.findViewById(R.id.showMusicManageIV)
        view.setOnClickListener{startTimer()}
        rightBtn?.setOnClickListener{ resetTimer()}
        showMusicManagerIV?.setOnClickListener { displayMusicManageBlock() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(mytag, "onActivityCreated - start")

        MainTimer.parrent = this
        MainTimer.loadTimeToUi()
        loadTimerStatus()

        val actualTrackTV: TextView? = view?.findViewById(R.id.actualTrackNameText)
        MusicPlayer.actualTrackName.observe(viewLifecycleOwner, Observer { data ->
            data?.let {
                if (it == ACTUAL_PLAYING_TRACK_NAME_BLANK){
                    actualTrackTV?.text = resources.getString(R.string.noneTrackNameLoaded)
                } else {
                    actualTrackTV?.text = it
                }
            }
        })
    }

    private fun loadTimerStatus() {
        Log.d(mytag, "loadTimerStatus - status ${MainTimer.timerStatus}")
        if (MainTimer.timerStatus == TIMER_RUNNING) {
            modifyUiAtStart()
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (MainTimer.timerStatus == TIMER_STOPPED) {
            modifyUiAtStop()
        }
    }

    private fun displayMusicManageBlock(){
        val musicManageLay: ConstraintLayout? = view?.findViewById(R.id.MusicManageLayout)
        val showMusicManagerIV: ImageView? = view?.findViewById(R.id.showMusicManageIV)
        if (musicManageVisible){
            musicManageLay?.visibility = View.GONE
        } else {
            musicManageLay?.visibility = View.VISIBLE
        }
        showMusicManagerIV?.rotation = 180F + showMusicManagerIV?.rotation!!
        musicManageVisible = !musicManageVisible
    }

    // btn operations

    private fun startTimer() {
        Log.d(mytag, "startTimer()")
        MusicPlayer.loadMusic()
        MainTimer.startMainTimer()
        modifyUiAtStart()
        MusicPlayer.playMusic()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    private fun stopTimer() {
        Log.d(mytag, "stopTimer()")
        MainTimer.pauseTimer()
        modifyUiAtStop()
        MusicPlayer.pauseMusic()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun resetTimer() {
        Log.d(mytag, "resetTimer()")
        MainTimer.pauseTimer()

        MainTimer.resetTime()
        MainTimer.loadTimeToUi()

        modifyUiAtReset()
        MusicPlayer.disableMusic()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun resumeTimer() {
        Log.d(mytag, "resumeTimer()")
        MainTimer.startMainTimer(resetTime = false)
        modifyUiAtResume()
        MusicPlayer.playMusic()
    }

    // ui modify

    private fun modifyUiAtStart(){
        val leftBtn: Button? = view?.findViewById(R.id.leftBtn)
        val rightBtn: Button? = view?.findViewById(R.id.rightBtn)

        rightBtn?.visibility = Button.VISIBLE
        leftBtn?.setText(R.string.timerStop)

        view?.setOnClickListener {
            stopTimer()
        }
    }

    private fun modifyUiAtStop(){
        val leftBtn: Button? = view?.findViewById(R.id.leftBtn)
        val rightBtn: Button? = view?.findViewById(R.id.rightBtn)

        rightBtn?.visibility = Button.VISIBLE
        leftBtn?.setText(R.string.timerResume)
        view?.setOnClickListener {
            resumeTimer()
        }
    }

    private fun modifyUiAtResume() {
        val leftBtn: Button? = view?.findViewById(R.id.leftBtn)

        leftBtn?.setText(R.string.timerStop)
        view?.setOnClickListener {
            stopTimer()
        }
    }

    private fun modifyUiAtReset() {
        val leftBtn: Button? = view?.findViewById(R.id.leftBtn)
        val rightBtn: Button? = view?.findViewById(R.id.rightBtn)

        leftBtn?.setText(R.string.timerStart)
        view?.setOnClickListener {
            startTimer()
        }
        rightBtn?.visibility = Button.INVISIBLE
    }

}
