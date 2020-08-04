package com.example.musictimer.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.musictimer.*
import com.example.musictimer.TIMER_RUNNING
import com.example.musictimer.TIMER_STOPPED
import com.example.musictimer.mechanisms.MainTimer
import com.example.musictimer.mechanisms.MusicPlayer

class HomeFragment : Fragment() {

    private val mytag = "HomeFragment"

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

        val leftBtn: Button? = getView()?.findViewById(R.id.leftBtn)
        val rightBtn: Button? = getView()?.findViewById(R.id.rightBtn)
        leftBtn?.setOnClickListener{_ -> startTimer()}
        rightBtn?.setOnClickListener{_ -> resetTimer()}
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(mytag, "onActivityCreated - start")

        MainTimer.parrent = this
        MainTimer.loadTimeToUi()
        loadTimerStatus()
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

    // btn operations

    private fun startTimer() {
        Log.d(mytag, "startTimer - start")
        MusicPlayer.loadMusic()
        MainTimer.startMainTimer()
        modifyUiAtStart()
        MusicPlayer.playMusic()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    private fun stopTimer() {
        MainTimer.pauseTimer()
        modifyUiAtStop()
        MusicPlayer.pauseMusic()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun resetTimer() {
        MainTimer.pauseTimer()

        MainTimer.resetTime()
        MainTimer.loadTimeToUi()

        modifyUiAtReset()
        MusicPlayer.disableMusic()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun resumeTimer() {
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

        leftBtn?.setOnClickListener {
            stopTimer()
        }
    }

    private fun modifyUiAtStop(){
        val leftBtn: Button? = view?.findViewById(R.id.leftBtn)
        val rightBtn: Button? = view?.findViewById(R.id.rightBtn)

        rightBtn?.visibility = Button.VISIBLE
        leftBtn?.setText(R.string.timerResume)
        leftBtn?.setOnClickListener {
            resumeTimer()
        }
    }

    private fun modifyUiAtResume() {
        val leftBtn: Button? = view?.findViewById(R.id.leftBtn)

        leftBtn?.setText(R.string.timerStop)
        leftBtn?.setOnClickListener {
            stopTimer()
        }
    }

    private fun modifyUiAtReset() {
        val leftBtn: Button? = view?.findViewById(R.id.leftBtn)
        val rightBtn: Button? = view?.findViewById(R.id.rightBtn)

        leftBtn?.setText(R.string.timerStart)
        leftBtn?.setOnClickListener {
            startTimer()
        }
        rightBtn?.visibility = Button.GONE
    }

}
