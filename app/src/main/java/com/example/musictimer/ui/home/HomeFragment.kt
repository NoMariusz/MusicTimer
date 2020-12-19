package com.example.musictimer.ui.home

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.musictimer.*
import com.example.musictimer.mechanisms.MainTimer
import com.example.musictimer.mechanisms.MusicPlayer
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule


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

        // text translation
        val trackNameScrollView: HorizontalScrollView? = view?.findViewById(R.id.trackNameScrollView)
        actualTrackTV?.movementMethod = ScrollingMovementMethod()   // to scroll
        actualTrackTV?.setHorizontallyScrolling(true)
        val animTime = 5000L
        GlobalScope.launch(Dispatchers.Main){
            while (true) {
                val scrollWith = actualTrackTV?.width?.minus((actualTrackTV.parent?.parent as View).width) ?: 0
                Log.d(mytag, "onActivityCreated - text transition animation start " +
                        "scrollWith $scrollWith")
                try {
                    activity?.runOnUiThread {
                        ObjectAnimator.ofInt(trackNameScrollView, "scrollX", scrollWith).apply {
                            duration = animTime
                            start()
                        }
                    }
                    delay(animTime + 100L)
                    activity?.runOnUiThread {
                        ObjectAnimator.ofInt(trackNameScrollView, "scrollX", 0).apply {
                            duration = animTime
                            start()
                        }
                    }
                    delay(animTime + 100L)
                } catch (e: Exception){
                    Log.e(mytag, "onActivityCreated: slidingTitleJob: ", e)
                }
            }
        }

        MusicPlayer.actualTrackName.observe(viewLifecycleOwner, Observer { data ->
            data?.let {
                Log.d(mytag, "onActivityCreated: actualTrackName changed $data")
                if (it == ACTUAL_PLAYING_TRACK_NAME_BLANK) {
                    actualTrackTV?.text = resources.getString(R.string.noneTrackNameLoaded)
                    stopMusic()
                } else {
                    actualTrackTV?.text = it
                }
                // TODO: find way to restart slidingTitleJob at change track name
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
        val animationDuration: Long = 150
        val actualTrackTV: TextView? = view?.findViewById(R.id.actualTrackNameText)

        fun animateShake(valStart: Float, valEnd: Float){
            val valueAnimator = ValueAnimator.ofFloat(valStart, valEnd)

            valueAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                actualTrackTV?.rotation = value
            }

            valueAnimator.interpolator = AnticipateOvershootInterpolator()
            valueAnimator.duration = animationDuration
            valueAnimator.start()
        }

        animateShake(0f, 5f)

        val secondAnimDelayTimer = Timer()
        secondAnimDelayTimer.schedule(0, animationDuration) {
            this@HomeFragment.activity?.runOnUiThread {
                animateShake(5f, 0f)
                secondAnimDelayTimer.cancel()
            }
        }
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
