package com.bll.lnkcommon.ui.activity

import PopupClick
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.manager.RecordDaoManager
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.RecordBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.ui.adapter.RecordAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.SPUtil
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ac_list.*
import kotlinx.android.synthetic.main.common_page_number.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class RecordListActivity : BaseActivity(){

    private var mAdapter: RecordAdapter? = null
    private var recordBeans = mutableListOf<RecordBean>()
    private var currentPos = -1//当前点击位置
    private var position = 0//当前点击位置
    private var mediaPlayer: MediaPlayer? = null
    private var pops= mutableListOf<PopupBean>()


    override fun layoutId(): Int {
        return R.layout.ac_list
    }

    override fun initData() {
        pageSize=12
        pops.add(PopupBean(0,"修改",R.mipmap.icon_notebook_edit))
        pops.add(PopupBean(1,"删除",R.mipmap.icon_delete))
    }

    override fun initView() {
        setPageTitle("录音")

        showView(iv_manager)
        iv_manager.setImageResource(R.mipmap.icon_add)

        iv_manager.setOnClickListener {
            customStartActivity(Intent(this,RecordActivity::class.java))
        }

        rv_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mAdapter = RecordAdapter(R.layout.item_record, recordBeans)
        rv_list.adapter = mAdapter
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(DP2PX.dip2px(this,50f), DP2PX.dip2px(this,30f),DP2PX.dip2px(this,50f),20)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            this.position=position
            if (view.id == R.id.iv_record) {
                setPlay()
            }
            if (view.id == R.id.iv_setting){
                setSetting(view)
            }
        }

        fetchData()
    }

    //点击播放
    private fun setPlay(){
        val path=recordBeans[position].path
        if (!File(path).exists())return
        if (currentPos == position) {
            if (mediaPlayer?.isPlaying == true) {
                pause(position)
            } else {
                mediaPlayer?.start()
                recordBeans[position].state=1
                mAdapter?.notifyItemChanged(position)//刷新为播放状态
            }
        } else {
            if (mediaPlayer?.isPlaying == true) {
                pause(currentPos)
            }
            release()
            play(path)
        }
        currentPos = position
    }

    private fun release(){
        if (mediaPlayer!=null){
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private fun play(path:String){
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(path)
        mediaPlayer?.setOnCompletionListener {
            recordBeans[position].state=0
            mAdapter?.notifyItemChanged(position)//刷新为结束状态
        }
        mediaPlayer?.prepare()
        mediaPlayer?.start()
        recordBeans[position].state=1
        mAdapter?.notifyItemChanged(position)//刷新为播放状态
    }

    private fun pause(pos:Int){
        mediaPlayer?.pause()
        recordBeans[pos].state=0
        mAdapter?.notifyItemChanged(pos)//刷新为结束状态
    }


    private fun setSetting(view : View){
        PopupClick(this,pops,view,0).builder().setOnSelectListener{
            if (it.id == 0) {
                edit()
            }
            if (it.id == 1) {
                delete()
            }
        }

    }

    //修改笔记
    private fun edit(){
        val recordBean=recordBeans[position]
        InputContentDialog(this,recordBean.title).builder().setOnDialogClickListener { string ->
            recordBean.title=string
            mAdapter?.notifyItemChanged(position)
            RecordDaoManager.getInstance().insertOrReplace(recordBean)
        }
    }

    //删除
    private fun delete(){
        CommonDialog(this).setContent("确定删除？").builder()
            .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    val recordBean=recordBeans[position]
                    mAdapter?.remove(position)
                    RecordDaoManager.getInstance().deleteBean(recordBean)
                    FileUtils.deleteFile(File(recordBean.path))
                }
            })
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag==Constants.RECORD_EVENT){
            pageIndex=1
            fetchData()
        }
    }

    override fun fetchData() {
        recordBeans = RecordDaoManager.getInstance().queryAllList(pageIndex, pageSize)
        val total = RecordDaoManager.getInstance().queryAllList()
        setPageNumber(total.size)
        mAdapter?.setNewData(recordBeans)
    }


    override fun onDestroy() {
        super.onDestroy()
        release()
    }

}