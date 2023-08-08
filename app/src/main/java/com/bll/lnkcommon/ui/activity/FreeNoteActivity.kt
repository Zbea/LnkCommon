package com.bll.lnkcommon.ui.activity

import PopupClick
import PopupFreeNoteList
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.media.MediaRecorder
import android.view.EinkPWInterface
import android.view.PWDrawObjectHandler
import android.view.View
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.dialog.NoteModuleAddDialog
import com.bll.lnkcommon.manager.*
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.ToolUtils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ac_free_note.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.util.Date

class FreeNoteActivity:BaseActivity() {

    private var elik:EinkPWInterface?=null
    private var isErasure=false
    private var isRecord=false
    private var recordBean: RecordBean? = null
    private var mRecorder: MediaRecorder? = null
    private var recordPath: String? = null
    private var bgRes=""
    private var freeNoteBean:FreeNoteBean?=null
    private var posImage=0
    private var images= mutableListOf<String>()
    private var freeNotePopWindow:PopupFreeNoteList?=null
    private var pops= mutableListOf<PopupBean>()
    private var notebooks= mutableListOf<Notebook>()

    override fun layoutId(): Int {
        return R.layout.ac_free_note
    }
    override fun initData() {
        bgRes= ToolUtils.getImageResStr(this,R.mipmap.icon_note_details_bg_1)
        freeNoteBean= FreeNoteBean()
        freeNoteBean?.bgRes=bgRes
        freeNoteBean?.date=System.currentTimeMillis()
        freeNoteBean?.title=DateUtils.longToStringNoYear(freeNoteBean?.date!!)
        freeNoteBean?.userId=if (isLoginState()) getUser()?.accountId else 0

        if (isLoginState()){
            notebooks.add(Notebook().apply {
                title = getString(R.string.note_tab_diary)
            })
            notebooks.addAll(NotebookDaoManager.getInstance().queryAll())

            for (i in notebooks.indices){
                pops.add(PopupBean(i,notebooks[i].title))
            }
        }
    }
    override fun initView() {
        setPageTitle("随笔")
        tv_name.text=freeNoteBean?.title
        tv_insert.visibility=if (isLoginState()) View.VISIBLE else View.GONE
//        elik=iv_image.pwInterFace

        tv_name.setOnClickListener {
            InputContentDialog(this,tv_name.text.toString()).builder().setOnDialogClickListener{
                tv_name.text=it
                freeNoteBean?.title=it
            }
        }

        tv_record.setOnClickListener {
            isRecord=!isRecord
            if (isRecord){
                startRecord()
            }
            else{
                stopRecord()
            }
        }

        tv_theme.setOnClickListener {
            NoteModuleAddDialog(this,1).builder()
                ?.setOnDialogClickListener { moduleBean ->
                    bgRes=ToolUtils.getImageResStr(this, moduleBean.resContentId)
                    freeNoteBean?.bgRes=bgRes
                    iv_image.setImageResource(ToolUtils.getImageResId(this,bgRes))
                }
        }

        tv_insert.setOnClickListener {
            insertNote()
        }

        tv_list.setOnClickListener {
            if (freeNotePopWindow==null){
                freeNotePopWindow=PopupFreeNoteList(this,tv_list).builder()
                freeNotePopWindow?.setOnSelectListener{
                    freeNoteBean?.paths=images
                    if (images.isNotEmpty())
                        FreeNoteDaoManager.getInstance().insertOrReplace(freeNoteBean)
                    posImage=0
                    freeNoteBean=it
                    images= freeNoteBean?.paths as MutableList<String>
                    tv_name.text=freeNoteBean?.title
                    setContentImage()
                }
            }
            else{
                freeNotePopWindow?.show()
            }

        }

        iv_page_up.setOnClickListener {
            if (posImage>0){
                posImage-=1
                setContentImage()
            }
        }

        iv_page_down.setOnClickListener {
            posImage+=1
            setContentImage()
        }

        iv_erasure.setOnClickListener {
            isErasure=!isErasure
            if (isErasure){
                iv_erasure?.setImageResource(R.mipmap.icon_draw_erasure_big)
                elik?.drawObjectType = PWDrawObjectHandler.DRAW_OBJ_CHOICERASE
            }
            else{
                iv_erasure?.setImageResource(R.mipmap.icon_draw_erasure)
                elik?.drawObjectType = PWDrawObjectHandler.DRAW_OBJ_RANDOM_PEN
            }
        }

        setContentImage()
    }

    /**
     * 更换内容
     */
    private fun setContentImage(){
        val path=FileAddress().getPathFreeNote(DateUtils.longToString(freeNoteBean?.date!!))+"/${posImage+1}.tch"
        //判断路径是否已经创建
        if (!images.contains(path)){
            images.add(path)
            freeNoteBean?.paths=images
        }
        tv_page.text="${posImage+1}/${images.size}"

//        elik?.setLoadFilePath(path, true)
//        elik?.setDrawEventListener(object : EinkPWInterface.PWDrawEvent {
//            override fun onTouchDrawStart(p0: Bitmap?, p1: Boolean) {
//            }
//
//            override fun onTouchDrawEnd(p0: Bitmap?, p1: Rect?, p2: ArrayList<Point>?) {
//            }
//
//            override fun onOneWordDone(p0: Bitmap?, p1: Rect?) {
//                elik?.saveBitmap(true) {}
//            }
//
//        })
    }

    /**
     * 插入笔记
     */
    private fun insertNote(){
        PopupClick(this,pops,tv_insert,10).builder().setOnSelectListener{
            if (NoteDaoManager.getInstance().isExist(it.name,freeNoteBean?.title)){
                showToast("已存在，插入失败")
                return@setOnSelectListener
            }
            val note=Note()
            note.title = freeNoteBean?.title
            note.date = System.currentTimeMillis()
            note.typeStr = it.name
            note.contentResId = freeNoteBean?.bgRes
            NoteDaoManager.getInstance().insertOrReplace(note)
            for (i in freeNoteBean?.paths!!.indices){
                val oldPath=freeNoteBean?.paths!![i]
                val date=System.currentTimeMillis()
                val pathName = DateUtils.longToString(date)
                val path=FileAddress().getPathNote(it.name,note.title,date)+"/${pathName}.tch"
                FileUtils.copyFile(oldPath,path)
//                FileUtils.copyFile(oldPath.replace("tch","png"),path.replace("tch","png"))
                val noteContent = NoteContent()
                noteContent.date = date
                noteContent.typeStr=note.typeStr
                noteContent.notebookTitle = note.title
                noteContent.resId = note.contentResId
                noteContent.title="未命名${i+1}"
                noteContent.filePath = path
                noteContent.pathName=pathName
                noteContent.page = i
                NoteContentDaoManager.getInstance().insertOrReplaceNote(noteContent)
            }
            showToast("插入笔记成功")
            EventBus.getDefault().post(Constants.NOTE_EVENT)
        }
    }

    /**
     * 开始录音
     */
    private fun startRecord(){
        tv_record.text="结束"
        recordBean = RecordBean()
        recordBean?.userId=if (isLoginState()) getUser()?.accountId else 0
        recordBean?.date=System.currentTimeMillis()
        recordBean?.title=tv_name.text.toString()

        val path= FileAddress().getPathRecord()
        if (!File(path).exists())
            File(path).mkdir()
        recordPath = File(path, "${DateUtils.longToString(recordBean?.date!!)}.mp3").path

        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setOutputFile(recordPath)
            setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            try {
                prepare()//准备
                start()//开始录音
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 结束录音
     */
    private fun stopRecord(){
        tv_record.text="录音"
        mRecorder?.apply {
            setOnErrorListener(null)
            setOnInfoListener(null)
            setPreviewDisplay(null)
            stop()
            release()
            mRecorder=null
        }
        recordBean?.path=recordPath
        RecordDaoManager.getInstance().insertOrReplace(recordBean)
        recordBean=null
        recordPath=null
        release()
    }

    private fun release(){
        mRecorder?.run {
            stop()
            release()
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
        freeNoteBean?.paths=images
        if (images.isNotEmpty())
            FreeNoteDaoManager.getInstance().insertOrReplace(freeNoteBean)
    }

}