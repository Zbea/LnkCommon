package com.bll.lnkcommon.ui.activity

import android.media.MediaPlayer
import android.media.MediaRecorder
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.manager.RecordDaoManager
import com.bll.lnkcommon.mvp.model.RecordBean
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileUtils
import kotlinx.android.synthetic.main.ac_record.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException

class RecordActivity : BaseActivity() {

    //语音文件保存路径
    private var pathFile: String? = null
    //语音操作对象
    private var mPlayer: MediaPlayer? = null
    private var mRecorder: MediaRecorder? = null
    private var recordBean: RecordBean? = null
    private var isSave=false

    override fun layoutId(): Int {
        return R.layout.ac_record
    }

    override fun initData() {
        recordBean = RecordBean()
        recordBean?.userId=if (isLoginState()) getUser()?.accountId else 0
        recordBean?.date=System.currentTimeMillis()

        val path=FileAddress().getPathRecord()
        if (!File(path).exists())
            File(path).mkdir()
        pathFile = File(path, "${DateUtils.longToString(recordBean?.date!!)}.mp3").path
    }

    override fun initView() {
        setPageTitle("录音")
        showView(tv_setting)
        tv_setting.text="保存"

        iv_back?.setOnClickListener {
            finish()
            FileUtils.deleteFile(File(pathFile))
        }

        tv_setting?.setOnClickListener {
            hideKeyboard()
            if (!FileUtils.isExist(pathFile)) {
                showToast("请录音")
                return@setOnClickListener
            }
            val title=et_title.text.toString()
            if (title.isEmpty()){
                showToast("请输入标题")
                return@setOnClickListener
            }

            isSave=true
            recordBean?.title=title
            recordBean?.path = pathFile
            RecordDaoManager.getInstance().insertOrReplace(recordBean)

            EventBus.getDefault().post(Constants.RECORD_EVENT)
            finish()
        }

        ll_record.setOnClickListener {
            hideKeyboard()
            mPlayer?.run {
                release()
                null
            }
            iv_record.setImageResource(R.mipmap.icon_record_show)
            mRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
                setOutputFile(pathFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                try {
                    prepare()//准备
                    start()//开始录音
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }

        ll_record_stop.setOnClickListener {
            iv_record.setImageResource(R.mipmap.icon_record_file)
            mRecorder?.apply {
                setOnErrorListener(null)
                setOnInfoListener(null)
                setPreviewDisplay(null)
                stop()
                release()
                mRecorder=null
                startPrepare()
            }
        }

        ll_record_play.setOnClickListener {
            if(mRecorder!=null){
                showToast("正在录音中")
                return@setOnClickListener
            }
            mPlayer?.apply {
                if (isPlaying){
                    iv_play.setImageResource(R.mipmap.icon_record_play)
                    tv_play.setText("播放")
                    pause()
                }
                else{
                    iv_play.setImageResource(R.mipmap.icon_record_pause)
                    tv_play.setText("暂停")
                    start()
                }

            }
        }

        ll_record_backward.setOnClickListener {
            backWard()
        }

        ll_record_forward.setOnClickListener {
            forWard()
        }

    }

    //播放更新准备
    private fun startPrepare(){
        mPlayer = MediaPlayer().apply {
            setDataSource(pathFile)
            setOnCompletionListener {
                iv_play.setImageResource(R.mipmap.icon_record_play)
                tv_play.text = "播放"
            }
            prepare()
        }
    }

    //快进1秒
    private fun forWard(){
        mPlayer?.apply {
            if (isPlaying) seekTo(currentPosition + 1000)
        }
    }

    //后退一秒
    private fun backWard(){
        mPlayer?.apply {
            if (isPlaying){
                var position = currentPosition
                if(position > 1000){
                    position-=1000
                }else{
                    position = 0
                }
                seekTo(position)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //未保存清理掉录音原件
        if (!isSave){
            FileUtils.deleteFile(File(pathFile))
        }
        mRecorder?.run {
            stop()
            release()
            null
        }

        mPlayer?.run {
            release()
            null
        }
    }

}