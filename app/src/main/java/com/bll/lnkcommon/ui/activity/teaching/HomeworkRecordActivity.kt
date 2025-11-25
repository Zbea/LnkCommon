package com.bll.lnkcommon.ui.activity.teaching

import android.widget.TextView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.ResultStandardDetailsDialog
import com.bll.lnkcommon.mvp.model.teaching.TeacherHomeworkList
import com.bll.lnkcommon.utils.DateUtils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.ac_drawing.iv_score
import kotlinx.android.synthetic.main.ac_homework_record.iv_play
import kotlinx.android.synthetic.main.ac_homework_record.progressBar
import kotlinx.android.synthetic.main.ac_homework_record.tv_content
import kotlinx.android.synthetic.main.ac_homework_record.tv_end_time
import kotlinx.android.synthetic.main.ac_homework_record.tv_play
import kotlinx.android.synthetic.main.ac_homework_record.tv_speed_0_5
import kotlinx.android.synthetic.main.ac_homework_record.tv_speed_1
import kotlinx.android.synthetic.main.ac_homework_record.tv_speed_1_5
import kotlinx.android.synthetic.main.ac_homework_record.tv_speed_2
import kotlinx.android.synthetic.main.ac_homework_record.tv_speed_2_5
import kotlinx.android.synthetic.main.ac_homework_record.tv_start_time
import java.util.Timer
import java.util.TimerTask
import java.util.stream.Collectors

class HomeworkRecordActivity:BaseActivity() {
    private var homeworkBean: TeacherHomeworkList.TeacherHomeworkBean?=null
    private var exoPlayer: ExoPlayer? = null
    private var timer: Timer? = null
    private var speed=1f
    private var isReadyRecorder=false

    override fun layoutId(): Int {
        return R.layout.ac_homework_record
    }

    override fun initData() {
        homeworkBean=intent.getBundleExtra("bundle")?.getSerializable("homeworkBean") as TeacherHomeworkList.TeacherHomeworkBean
    }

    override fun initView() {
        setPageTitle("朗读作业")

        tv_content.text=homeworkBean?.title

        speed=1f
        isReadyRecorder=false
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.setMediaItem(MediaItem.fromUri(homeworkBean?.submitContent!!))
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        isReadyRecorder=true
                        val totalTime = exoPlayer?.duration!!.toInt()
                        tv_end_time.text = DateUtils.secondToString(totalTime)
                        progressBar.max = totalTime/1000
                    }
                    Player.STATE_ENDED -> {
                        tv_start_time.text = "00:00"
                        exoPlayer?.pause()
                        exoPlayer?.seekTo(0)
                        progressBar.progress = 0
                        changeMediaView(false)
                        timer?.cancel()
                    }
                }
            }
        })
        exoPlayer?.prepare()
        tv_start_time.text = "00:00"
        changeMediaView(false)

        iv_play.setOnClickListener {
            if (exoPlayer != null) {
                if (!isReadyRecorder){
                    showToast("录音未加载完成")
                    return@setOnClickListener
                }
                if (exoPlayer?.isPlaying == true) {
                    exoPlayer?.pause()
                    timer?.cancel()
                    changeMediaView(false)
                } else {
                    exoPlayer?.play()
                    startTimer()
                    changeMediaView(true)
                }
            }
        }

        tv_speed_0_5.setOnClickListener {
            if (exoPlayer != null) {
                setSpeed(0.5f)
                setSpeedView(tv_speed_0_5)
            }
        }
        tv_speed_1.setOnClickListener {
            if (exoPlayer != null) {
                setSpeed(1f)
                setSpeedView(tv_speed_1)
            }
        }
        tv_speed_1_5.setOnClickListener {
            if (exoPlayer != null) {
                setSpeed(1.5f)
                setSpeedView(tv_speed_1_5)
            }
        }
        tv_speed_2.setOnClickListener {
            if (exoPlayer != null) {
                setSpeed(2f)
                setSpeedView(tv_speed_2)
            }
        }
        tv_speed_2_5.setOnClickListener {
            if (exoPlayer != null) {
                setSpeed(2.5f)
                setSpeedView(tv_speed_2_5)
            }
        }

        iv_score.setOnClickListener {
            if (homeworkBean?.recordType==1){
                ResultStandardDetailsDialog(this, homeworkBean?.title!!, homeworkBean?.score!!.toDouble(),homeworkBean?.question!!).builder()
            }
            else{
                val items= DataBeanManager.getResultStandardItems(homeworkBean!!.subType,homeworkBean!!.typeName,homeworkBean!!.questionType).stream().collect(Collectors.toList())
                ResultStandardDetailsDialog(this,homeworkBean?.title!!,homeworkBean?.score!!.toDouble(),homeworkBean?.questionType!! ,homeworkBean?.question!!,items).builder()
            }
        }
    }

    private fun changeMediaView(boolean: Boolean) {
        if (boolean) {
            iv_play.setImageResource(R.mipmap.icon_record_pause)
            tv_play.text = "暂停"
        } else {
            iv_play.setImageResource(R.mipmap.icon_record_play)
            tv_play.text = "播放"
        }
    }

    private fun setSpeedView(tvSpeed: TextView) {
        tv_speed_0_5.background = getDrawable(R.drawable.bg_black_stroke_5dp_corner)
        tv_speed_0_5.setTextColor(getColor(R.color.black))
        tv_speed_1.background = getDrawable(R.drawable.bg_black_stroke_5dp_corner)
        tv_speed_1.setTextColor(getColor(R.color.black))
        tv_speed_1_5.background = getDrawable(R.drawable.bg_black_stroke_5dp_corner)
        tv_speed_1_5.setTextColor(getColor(R.color.black))
        tv_speed_2.background = getDrawable(R.drawable.bg_black_stroke_5dp_corner)
        tv_speed_2.setTextColor(getColor(R.color.black))
        tv_speed_2_5.background = getDrawable(R.drawable.bg_black_stroke_5dp_corner)
        tv_speed_2_5.setTextColor(getColor(R.color.black))
        tvSpeed.background = getDrawable(R.drawable.bg_black_solid_5dp_corner)
        tvSpeed.setTextColor(getColor(R.color.white))
    }

    private fun setSpeed(speed: Float) {
        this.speed=speed
        exoPlayer?.setPlaybackSpeed(speed)
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.pause()
            timer?.cancel()
            exoPlayer?.play()
            startTimer()
        }
    }

    private fun startTimer() {
        val periodTime = (1000/speed).toLong()
        Thread {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        val currentTime=exoPlayer?.currentPosition!!.toInt()
                        progressBar.progress = currentTime/1000
                        tv_start_time.text = DateUtils.secondToString(currentTime)
                    }
                }
            }, 0, periodTime)
        }.start()
    }


    private fun release() {
        timer?.cancel()
        if (exoPlayer != null) {
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }
}