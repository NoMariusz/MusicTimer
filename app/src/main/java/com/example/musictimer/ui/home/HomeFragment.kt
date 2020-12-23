package com.example.musictimer.ui.home

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.musictimer.*
import com.example.musictimer.mechanisms.MainTimer
import com.example.musictimer.mechanisms.MusicPlayer


class HomeFragment : Fragment() {
    private val mytag = "HomeFragment"
    private var musicManageVisible = false
    private var slidingTrackNameAnimator: ObjectAnimator? = null

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
        showMusicManagerIV?.setOnClickListener { changeVisibilityMusicManageBlock() }

        // music manage operations
        val startStopTrackIV: ImageView? = view.findViewById(R.id.startStopTrackIV)
        startStopTrackIV?.setOnClickListener { startMusicClick() }
        val nextTrackIV: ImageView? = view.findViewById(R.id.nextTrackIV)
        nextTrackIV?.setOnClickListener { nextTrack() }
        val previousTrackIV: ImageView? = view.findViewById(R.id.previousTrackIV)
        previousTrackIV?.setOnClickListener { previousTrack() }
        // adding blank listener in lay to prevent view click events on this block
        val musicManageLayout: ConstraintLayout? = view.findViewById(R.id.MusicManageLayout)
        musicManageLayout?.setOnClickListener { }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(mytag, "onActivityCreated - start")

        MainTimer.parrent = this
        MainTimer.loadTimeToUi()
        loadTimerStatus()

        val actualTrackTV: TextView? = view?.findViewById(R.id.actualTrackNameText)
        // listener to start/cancel sliding animation when actualTrackTV change his layout
        actualTrackTV?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (actualTrackTV.text.toString() == resources.getString(R.string.noneTrackNameLoaded)){
                slidingTrackNameAnimator?.cancel()
            } else if (musicManageVisible) {
                startSlidingNameAnimation()
            }
        }
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
            }
        })
    }

    private fun loadTimerStatus() {
        Log.d(mytag, "loadTimerStatus - status ${MainTimer.timerStatus}")
        if (MainTimer.timerStatus == TIMER_RUNNING) {
            modifyUiAtStart()
            modifyUiAtStartMusic()
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (MainTimer.timerStatus == TIMER_STOPPED) {
            modifyUiAtStopMusic()
            modifyUiAtStop()
        }
    }

    private fun changeVisibilityMusicManageBlock(){
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

    // btn and iv events

    // music and timer
    private fun startTimer() {
        Log.d(mytag, "startTimer()")
        MusicPlayer.loadMusic()
        MainTimer.startMainTimer()
        modifyUiAtStart()
        startMusicClick()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    private fun stopTimer() {
        Log.d(mytag, "stopTimer()")
        MainTimer.pauseTimer()
        modifyUiAtStop()
        stopMusicClick()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun resetTimer() {
        Log.d(mytag, "resetTimer()")
        MainTimer.pauseTimer()

        MainTimer.resetTime()
        MainTimer.loadTimeToUi()

        modifyUiAtReset()
        stopMusic()
        MusicPlayer.disableMusic()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun resumeTimer() {
        Log.d(mytag, "resumeTimer()")
        MainTimer.startMainTimer(resetTime = false)
        modifyUiAtResume()
        startMusicClick()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // only music, modify music manage ui
    private inline fun onlyAtTimerRunningDec(strict: Boolean = false, func: () -> Unit){
        if (MainTimer.timerStatus != TIMER_NOT_STARTED && !(strict && !MusicPlayer.isPlaying())) {
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
