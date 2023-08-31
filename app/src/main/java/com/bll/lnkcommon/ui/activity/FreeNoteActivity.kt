package com.bll.lnkcommon.ui.activity

import PopupClick
import PopupFreeNoteList
import PopupRecordList
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.media.MediaRecorder
import android.net.Uri
import android.os.StrictMode
import android.provider.MediaStore
import android.view.EinkPWInterface
import android.view.View
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.FriendSelectorDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.dialog.NoteModuleAddDialog
import com.bll.lnkcommon.dialog.PopupShareNoteList
import com.bll.lnkcommon.greendao.StringConverter
import com.bll.lnkcommon.manager.*
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.presenter.ShareNotePresenter
import com.bll.lnkcommon.mvp.view.IContractView.IShareNoteView
import com.bll.lnkcommon.utils.*
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.ac_free_note.*
import kotlinx.android.synthetic.main.common_drawing_bottom.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException

class FreeNoteActivity:BaseDrawingActivity(),IShareNoteView {

    private val presenter=ShareNotePresenter(this)
    private var isRecord=false
    private var recordBean: RecordBean? = null
    private var mRecorder: MediaRecorder? = null
    private var recordPath: String? = null
    private var bgRes=""
    private var freeNoteBean:FreeNoteBean?=null
    private var posImage=0
    private var images= mutableListOf<String>()//手写地址
    private var bgResList= mutableListOf<String>()//背景地址
    private var freeNotePopWindow:PopupFreeNoteList?=null
    private var sharePopWindow:PopupShareNoteList?=null
    private var popsNote= mutableListOf<PopupBean>()
    private var popsShare= mutableListOf<PopupBean>()
    private var notebooks= mutableListOf<Notebook>()
    private var shareTotal=0//分享总数
    private var shareNotes= mutableListOf<ShareNoteList.ShareNoteBean>()
    private var sharePosition=0//分享列表position
    private var friendId=0//选中好友id

    override fun onList(list: ShareNoteList) {
        shareNotes=list.list
        shareTotal=list.total
    }
    override fun onToken(token: String) {
        showLoading()
        //分享只能是有手写页面
        val sImages= mutableListOf<String>()
        val sBgRes= mutableListOf<String>()
        for (i in images.indices){
            if (File(images[i]).exists()){
                sImages.add(images[i])
                sBgRes.add(bgResList[i])
            }
        }
        if (sImages.size==0){
            hideLoading()
            showToast("暂无分享内容")
            return
        }
        val imagePaths= mutableListOf<String>()
        for (path in sImages){
            imagePaths.add(path.replace("tch","png"))
        }
        FileImageUploadManager(token, imagePaths).apply {
            startUpload()
            setCallBack(object : FileImageUploadManager.UploadCallBack {
                override fun onUploadSuccess(urls: List<String>) {
                    val urls=ToolUtils.getImagesStr(urls)
                    val bgs=ToolUtils.getImagesStr(sBgRes)
                    val map=HashMap<String,Any>()
                    map["userId"]=friendId
                    map["title"]=freeNoteBean?.title!!
                    map["bgRes"]=bgs
                    map["paths"]=urls
                    map["date"]=freeNoteBean?.date!!
                    presenter.commitShare(map)
                }
                override fun onUploadFail() {
                    hideLoading()
                    showToast("分享失败")
                }
            })
        }
    }
    override fun onDeleteSuccess() {
        sharePopWindow?.deleteData(sharePosition)
    }
    override fun onShare() {
        showToast("分享成功")
    }


    override fun layoutId(): Int {
        return R.layout.ac_free_note
    }
    override fun initData() {
        bgRes= ToolUtils.getImageResStr(this,R.mipmap.icon_note_details_bg_1)
        freeNoteBean= FreeNoteBean()
        freeNoteBean?.date=System.currentTimeMillis()
        freeNoteBean?.title=DateUtils.longToStringNoYear(freeNoteBean?.date!!)
        freeNoteBean?.userId=if (isLoginState()) getUser()?.accountId else 0

        if (isLoginState()){
            notebooks.add(Notebook().apply {
                title = getString(R.string.note_tab_diary)
            })
            notebooks.addAll(NotebookDaoManager.getInstance().queryAll())

            for (i in notebooks.indices){
                popsNote.add(PopupBean(i,notebooks[i].title))
            }
        }

        popsShare.add(PopupBean(0,"微信",R.mipmap.ic_wx))
        popsShare.add(PopupBean(1,"墨本",R.mipmap.ic_launcher))

        if (isLoginState())
            fetchShareNotes(1,false)
    }
    override fun initView() {
        //用于分享本地应用
        val builder=  StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()

        tv_name.text=freeNoteBean?.title
        tv_insert.visibility=if (isLoginState()) View.VISIBLE else View.GONE
//        elik=iv_image.pwInterFace

        tv_name.setOnClickListener {
            InputContentDialog(this,tv_name.text.toString()).builder().setOnDialogClickListener{
                tv_name.text=it
                freeNoteBean?.title=it
            }
        }

        iv_record.setOnClickListener {
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
                    iv_image.setImageResource(ToolUtils.getImageResId(this,bgRes))
                    bgResList[posImage]=bgRes
                }
        }

        tv_insert.setOnClickListener {
            insertNote()
        }

        tv_free_list.setOnClickListener {
            if (freeNotePopWindow==null){
                freeNotePopWindow=PopupFreeNoteList(this,tv_free_list).builder()
                freeNotePopWindow?.setOnSelectListener{
                    saveFreeNote()
                    posImage=0
                    freeNoteBean=it
                    bgResList= freeNoteBean?.bgRes as MutableList<String>
                    images= freeNoteBean?.paths as MutableList<String>
                    tv_name.text=freeNoteBean?.title
                    setContentImage()
                }
            }
            else{
                freeNotePopWindow?.show()
            }
        }

        tv_record_list.setOnClickListener {
            PopupRecordList(this,tv_record_list).builder()
        }

        tv_share_list.setOnClickListener {
            if (!isLoginState()){
                showToast("未登录")
                return@setOnClickListener
            }
            if (sharePopWindow==null){
                sharePopWindow=PopupShareNoteList(this,tv_share_list,shareTotal).builder()
                sharePopWindow?.setData(shareNotes)
                sharePopWindow?.setOnClickListener(object : PopupShareNoteList.OnClickListener {
                    override fun onPage(pageIndex: Int) {
                        fetchShareNotes(pageIndex,true)
                    }
                    override fun onDelete(position: Int) {
                        sharePosition=position
                        val map=HashMap<String,Any>()
                        map["ids"]= arrayOf(shareNotes[position].id)
                        presenter.deleteShareNote(map)
                    }
                    override fun onDownload(position: Int) {
                        downloadShareNote(shareNotes[position])
                    }
                })
            }
            else{
                sharePopWindow?.show()
            }
        }

        tv_share.setOnClickListener {
            PopupClick(this,popsShare,tv_share,5).builder().setOnSelectListener{
                if (it.id==0){
                    if (AppUtils.isAvailable(this,Constants.PACKAGE_WX)){
                        shareWx()
                    }
                    else{
                        showToast("未安装微信")
                    }
                }
                else{
                    if (!isLoginState()){
                        showToast("未登录")
                        return@setOnSelectListener
                    }
                    FriendSelectorDialog(this, DataBeanManager.friends).builder().setOnDialogClickListener{ id->
                        friendId=id
                        presenter.getToken()
                    }
                }
            }
        }

        if (posImage>=bgResList.size){
            bgResList.add(bgRes)
        }
        setContentImage()
    }

    override fun onPageDown() {
        posImage+=1
        if (posImage>=bgResList.size){
            bgRes= ToolUtils.getImageResStr(this,R.mipmap.icon_note_details_bg_1)
            bgResList.add(bgRes)
        }
        setContentImage()
    }

    override fun onPageUp() {
        if (posImage>0){
            posImage-=1
            setContentImage()
        }
    }

    /**
     * 下载分享随笔
     */
    private fun downloadShareNote(item:ShareNoteList.ShareNoteBean){
        val date=System.currentTimeMillis()
        val path=FileAddress().getPathFreeNote(DateUtils.longToString(date))
        val savePaths= mutableListOf<String>()
        val tchPaths= mutableListOf<String>()
        val urls=item.paths.split(",")
        for (i in urls.indices)
        {
            savePaths.add(path+"/${i+1}.png")
            tchPaths.add(path+"/${i+1}.tch")
        }
        FileMultitaskDownManager.with(this).create(urls).setPath(savePaths).startMultiTaskDownLoad(
            object : FileMultitaskDownManager.MultiTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int, ) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    val freeNoteBean= FreeNoteBean()
                    freeNoteBean.userId=getUser()?.accountId!!
                    freeNoteBean.title=item.title
                    freeNoteBean.date=date
                    freeNoteBean.bgRes=StringConverter().convertToEntityProperty(item.bgRes)
                    freeNoteBean.paths=tchPaths
                    FreeNoteDaoManager.getInstance().insertOrReplace(freeNoteBean)
                    showToast("下载成功")
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    showToast("下载失败")
                }
            })
    }

    /**
     * 分享微信
     */
    private fun shareWx(){
        showLoading()
        Thread{
            val urls= ArrayList<Uri>()
            val paths=saveImage()
            for (path in paths){
                if (File(path).exists()){
                    val uri= Uri.parse(MediaStore.Images.Media.insertImage(contentResolver,
                            File(path).path,  DateUtils.longToString(System.currentTimeMillis())+".png",""))
                    urls.add(uri)
                }
            }
            //分享到微信好友
            if (urls.isNotEmpty()){
                val  intent = Intent()
                val componentName = ComponentName(Constants.PACKAGE_WX, "com.tencent.mm.ui.tools.ShareImgUI");
                intent.component = componentName
                intent.action = Intent.ACTION_SEND_MULTIPLE
                intent.type="image/*"
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, urls)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            runOnUiThread{
                hideLoading()
            }
        }.start()
    }

    /**
     * 合图
     */
    private fun saveImage():MutableList<String>{
        val list= mutableListOf<String>()
        for (i in images.indices){
            if (File(images[i]).exists()){
                val bgRes=bgResList[i]
                val drawPath=images[i].replace("tch","png")
                val mergePath=FileAddress().getPathFreeNote(DateUtils.longToString(freeNoteBean?.date!!))+"/merge/${i+1}.jpg"

                val oldBitmap=BitmapFactory.decodeResource(resources, ToolUtils.getImageResId(this,bgRes))
                val drawBitmap = BitmapFactory.decodeFile(drawPath)
                if (drawBitmap!=null){
                    val mergeBitmap = BitmapUtils.mergeBitmap(oldBitmap, drawBitmap)
                    BitmapUtils.saveBmpGallery(this, mergeBitmap, mergePath)
                }
                else{
                    BitmapUtils.saveBmpGallery(this, oldBitmap, mergePath)
                }
                list.add(mergePath)
            }
        }
        return list
    }

    /**
     * 更换内容
     */
    private fun setContentImage(){
        iv_image.setImageResource(ToolUtils.getImageResId(this,bgResList[posImage]))
        val path=FileAddress().getPathFreeNote(DateUtils.longToString(freeNoteBean?.date!!))+"/${posImage+1}.tch"
        //判断路径是否已经创建
        if (!images.contains(path)){
            images.add(path)
        }
        tv_page.text="${posImage+1}/${images.size}"

//        elik?.setLoadFilePath(path, true)
//        elik?.setDrawEventListener(object : EinkPWInterface.PWDrawEvent {
//            override fun onTouchDrawStart(p0: Bitmap?, p1: Boolean) {
//            }
//            override fun onTouchDrawEnd(p0: Bitmap?, p1: Rect?, p2: ArrayList<Point>?) {
//            }
//            override fun onOneWordDone(p0: Bitmap?, p1: Rect?) {
//                elik?.saveBitmap(true) {}
//            }
//        })
    }

    /**
     * 插入笔记
     */
    private fun insertNote(){
        PopupClick(this,popsNote,tv_insert,10).builder().setOnSelectListener{
//            if (NoteDaoManager.getInstance().isExist(it.name,freeNoteBean?.title)){
//                showToast("已存在，插入失败")
//                return@setOnSelectListener
//            }
            val note=Note()
            note.title = freeNoteBean?.title
            note.date = System.currentTimeMillis()
            note.typeStr = it.name
            note.contentResId = ToolUtils.getImageResStr(this,0)
            NoteDaoManager.getInstance().insertOrReplace(note)
            for (i in images.indices){
                val oldPath=images[i]
                if(File(oldPath).exists()){
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
            }
            showToast("插入笔记成功")
            EventBus.getDefault().post(Constants.NOTE_EVENT)
        }
    }

    /**
     * 开始录音
     */
    private fun startRecord(){
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
                e.printStackTrace()
            }
        }
    }

    /**
     * 结束录音
     */
    private fun stopRecord(){
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
    }

    private fun saveFreeNote(){
        //清空没有手写页面
        val sImages= mutableListOf<String>()
        for (i in images.indices){
            if (File(images[i]).exists()){
                sImages.add(images[i])
            }
        }
        freeNoteBean?.paths=images
        freeNoteBean?.bgRes=bgResList
        if (sImages.size>0)
            FreeNoteDaoManager.getInstance().insertOrReplace(freeNoteBean)
    }

    private fun fetchShareNotes(page:Int,isShow: Boolean){
        val map=HashMap<String,Any>()
        map["size"]=6
        map["page"]=page
        presenter.getShareNotes(map,isShow)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (recordBean!=null){
            stopRecord()
        }
        saveFreeNote()
        FileDownloader.getImpl().pauseAll()
    }

}
