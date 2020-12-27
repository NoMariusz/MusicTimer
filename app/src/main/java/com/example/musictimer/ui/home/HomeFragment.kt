package com.example.musictimer.ui.home

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.musictimer.*
import com.example.musictimer.mechanisms.MainTimer
import com.example.musictimer.mechanisms.MusicPlayer
import com.example.musictimer.services.TimerAndPlayerService


class HomeFragment : Fragment() {
    private val mytag = "HomeFragment"
    private var slidingTrackNameAnimator: ObjectAnimator? = null
    private var mainTimer: MainTimer? = null
    
    private lateinit var mainActivityViewModel: MainActivityViewModel

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

        view.setOnClickListener{startTimer()}
        val rightBtn: Button? = view.findViewById(R.id.rightBtn)
        rightBtn?.setOnClickListener{ resetTimer()}

        // music manage operations
        val startStopTrackIV: ImageView? = view.findViewById(R.id.startStopTrackIV)
        startStopTrackIV?.setOnClickListener { startMusicClick() }
        val nextTrackIV: ImageView? = view.findViewById(R.id.nextTrackIV)
        nextTrackIV?.setOnClickListener { nextTrack() }
        val previousTrackIV: ImageView? = view.findViewById(R.id.previousTrackIV)
        previousTrackIV?.setOnClickListener { previousTrack() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(mytag, "onActivityCreated - start")

        val actualTrackTV: TextView? = view?.findViewById(R.id.actualTrackNameText)
        // observer to update actualTackTV
        MusicPlayer.actualTrackName.observe(viewLifecycleOwner, Observer { data ->
            data?.let {
                Log.d(mytag, "onActivityCreated: actualTrackName changed $data")
                var newTrackName: String = it
                if (it == ACTUAL_PLAYING_TRACK_NAME_BLANK) {
                    newTrackName = resources.getString(R.string.noneTrackNameLoaded)
                    stopMusic()
                }
                actualTrackTV?.text = newTrackName
                makeAfterDelay({ refreshSlidingAnimation() }, 50)
            }
        })
    
        // start time and player service
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        mainActivityViewModel.timerAndPlayerBinder.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d(mytag, "onActivityCreated: mainActivityViewModel.timerAndPlayerBinder get service $it")
                val timerAndPlayerService = it.getService()
                mainTimer = timerAndPlayerService.mainTimer
//                mainTimer?.parrent = this
//                mainTimer?.loadTimeToUi()
                loadTimerStatus()
//                context?.unbindService(mainActivityViewModel.timerAndPlayerServiceConnection)
                mainTimer?.time?.observe(viewLifecycleOwner, Observer { time ->
                    loadTimeToUi(time[0], time[1])
                })
            }
        })

        val intent = Intent(activity, TimerAndPlayerService::class.java)
        activity?.startService(intent)
        activity?.bindService(intent, mainActivityViewModel.timerAndPlayerServiceConnection,
            Context.BIND_AUTO_CREATE)
//            0)
    }

    override fun onDestroyView() {
        activity?.unbindService(mainActivityViewModel.timerAndPlayerServiceConnection)
        super.onDestroyView()
    }

    private fun loadTimerStatus() {
        Log.d(mytag, "loadTimerStatus - status ${mainTimer?.timerStatus}")
        if (mainTimer?.timerStatus == TIMER_RUNNING) {
            modifyUiAtStart()
            modifyUiAtStartMusic()
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (mainTimer?.timerStatus == TIMER_STOPPED) {
            modifyUiAtStopMusic()
            modifyUiAtStop()
        }
    }

    fun loadTimeToUi(minutes: Long, seconds: Long) {
        val timerText: TextView? = activity?.findViewById(R.id.timerText)
        timerText?.text = "%02d:%02d".format(
            minutes,
            seconds
        )
    }

    private fun startSlidingNameAnimation() {
        val trackNameScrollView: HorizontalScrollView? = view?.findViewById(R.id.trackNameScrollView)
        val actualTrackTV: TextView? = view?.findViewById(R.id.actualTrackNameText)
        val animTime: Long = (actualTrackTV?.text?.length ?: 35) * 130L
        val scrollWith: Int = (actualTrackTV?.width ?: 0) - (trackNameScrollView?.width ?: 0)

        slidingTrackNameAnimator?.cancel()
        slidingTrackNameAnimator = ObjectAnimator.ofInt(
            trackNameScrollView, "scrollX", 0, scrollWith
        ).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            duration = animTime
            start()
        }
    }

    private fun refreshSlidingAnimation(){
        val actualTrackTV: TextView? = view?.findViewById(R.id.actualTrackNameText)
        if (actualTrackTV?.text.toString() != resources.getString(R.string.noneTrackNameLoaded)){
            startSlidingNameAnimation()
        } else{
            slidingTrackNameAnimator?.cancel()
            slidingTrackNameAnimator = null
        }
    }

    private fun makeAfterDelay(func: () -> Unit, delay: Long){
        Handler().postDelayed(func, delay)
    }

    // btn and iv events

    // music and timer
    private fun startTimer() {
        Log.d(mytag, "startTimer()")
        MusicPlayer.loadMusic()
        mainTimer?.startMainTimer()
        modifyUiAtStart()
        startMusicClick()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    private fun stopTimer() {
        Log.d(mytag, "stopTimer()")
        mainTimer?.pauseTimer()
        modifyUiAtStop()
        stopMusicClick()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun resetTimer() {
        Log.d(mytag, "resetTimer()")
        mainTimer?.pauseTimer()

        mainTimer?.resetTime()
//        mainTimer?.loadTimeToUi()

        modifyUiAtReset()
        stopMusic()
        MusicPlayer.disableMusic()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun resumeTimer() {
        Log.d(mytag, "resumeTimer()")
        mainTimer?.startMainTimer(resetTime = false)
        modifyUiAtResume()
        startMusicClick()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // only music, modify music manage ui
    private inline fun onlyAtTimerRunningDec(strict: Boolean = false, func: () -> Unit){
        if (mainTimer?.timerStatus != TIMER_NOT_STARTED && !(strict && !MusicPlayer.isPlaying())) {
            func()
        } else {
            playMusicManageLayLockedAnimation()
        }
    }

    private fun playMusicManageLayLockedAnimation(){
        val actualTrackName: TextView? = view?.findViewById(R.id.actualTrackNameText)
        val animator = ObjectAnimator.ofFloat(actualTrackName, View.ROTATION, 0f, 5f)
        animator.duration = 150
        animator.repeatCount = 1
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.interpolator = AnticipateOvershootInterpolator()
        animator.start()
    }

    private fun stopMusicClick() = onlyAtTimerRunningDec(true) {
        stopMusic()
    }

    private fun stopMusic(){
        modifyUiAtStopMusic()
        MusicPlayer.pauseMusic()
    }

    private fun modifyUiAtStopMusic(){
        val startStopTrackIV: ImageView? = view?.findViewById(R.id.startStopTrackIV)
        startStopTrackIV?.setImageResource(R.drawable.ic_baseline_play_arrow_48)
        startStopTrackIV?.setOnClickListener { startMusicClick() }
    }

    private  fun startMusicClick() = onlyAtTimerRunningDec(true) {
        modifyUiAtStartMusic()
        MusicPlayer.playMusic()
    }

    private fun modifyUiAtStartMusic(){
        val startStopTrackIV: ImageView? = view?.findViewById(R.id.startStopTrackIV)
        startStopTrackIV?.setImageResource(R.drawable.ic_baseline_pause_48)
        startStopTrackIV?.setOnClickListener { stopMusicClick() }
    }

    private fun nextTrack() = onlyAtTimerRunningDec {
        MusicPlayer.nextSong()
    }

    private fun previousTrack() = onlyAtTimerRunningDec {
        MusicPlayer.previousSong()
    }

    // main ui modify

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
