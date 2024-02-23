package com.bll.lnkcommon.ui.activity.drawing

import PopupClick
import PopupFreeNoteList
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.StrictMode
import android.provider.MediaStore
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.dialog.ItemSelectorDialog
import com.bll.lnkcommon.dialog.NoteModuleAddDialog
import com.bll.lnkcommon.dialog.PopupShareNoteList
import com.bll.lnkcommon.greendao.StringConverter
import com.bll.lnkcommon.manager.FreeNoteDaoManager
import com.bll.lnkcommon.mvp.model.FreeNoteBean
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.ShareNoteList
import com.bll.lnkcommon.mvp.presenter.ShareNotePresenter
import com.bll.lnkcommon.mvp.view.IContractView.IShareNoteView
import com.bll.lnkcommon.utils.*
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.ac_diary.*
import kotlinx.android.synthetic.main.ac_free_note.*
import kotlinx.android.synthetic.main.ac_free_note.v_content
import kotlinx.android.synthetic.main.common_drawing_bottom.*
import kotlinx.android.synthetic.main.common_title.*
import java.io.File

class FreeNoteActivity:BaseDrawingActivity(),IShareNoteView {

    private val presenter=ShareNotePresenter(this)
    private var bgRes=""
    private var freeNoteBean:FreeNoteBean?=null
    private var posImage=0
    private var images= mutableListOf<String>()//手写地址
    private var bgResList= mutableListOf<String>()//背景地址
    private var freeNotePopWindow:PopupFreeNoteList?=null
    private var sharePopWindow:PopupShareNoteList?=null
    private var popsShare= mutableListOf<PopupBean>()
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
        bgRes= ToolUtils.getImageResStr(this,R.mipmap.icon_freenote_bg_1)
        freeNoteBean= FreeNoteBean()
        freeNoteBean?.date=System.currentTimeMillis()
        freeNoteBean?.title=DateUtils.longToStringNoYear(freeNoteBean?.date!!)
        freeNoteBean?.userId=if (isLoginState()) getUser()?.accountId else 0

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

        disMissView(tv_page_title,iv_catalog,iv_btn)
        tv_title.text=freeNoteBean?.title
        elik=v_content.pwInterFace

        tv_title.setOnClickListener {
            InputContentDialog(this,tv_title.text.toString()).builder().setOnDialogClickListener{
                tv_title.text=it
                freeNoteBean?.title=it
            }
        }

        tv_theme.setOnClickListener {
            NoteModuleAddDialog(this,1).builder()
                ?.setOnDialogClickListener { moduleBean ->
                    bgRes=ToolUtils.getImageResStr(this, moduleBean.resFreenoteBg)
                    v_content.setImageResource(ToolUtils.getImageResId(this,bgRes))
                    bgResList[posImage]=bgRes
                }
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
                    tv_title.text=freeNoteBean?.title
                    setContentImage()
                }
            }
            else{
                freeNotePopWindow?.show()
            }
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
                    val items=DataBeanManager.friends
                    val lists= mutableListOf<ItemList>()
                    for (item in items){
                        lists.add(ItemList(item.id,item.nickname))
                    }
                    ItemSelectorDialog(this,"分享好友",lists).builder().setOnDialogClickListener{
                        friendId=items[it].friendId
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
            bgRes= ToolUtils.getImageResStr(this,R.mipmap.icon_freenote_bg_1)
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
        v_content.setImageResource(ToolUtils.getImageResId(this,bgResList[posImage]))
        val path=FileAddress().getPathFreeNote(DateUtils.longToString(freeNoteBean?.date!!))+"/${posImage+1}.tch"
        //判断路径是否已经创建
        if (!images.contains(path)){
            images.add(path)
        }
        tv_page.text="${posImage+1}/${images.size}"

        elik?.setLoadFilePath(path, true)
    }

    override fun onElikSave() {
        elik?.saveBitmap(true) {}
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
        saveFreeNote()
        FileDownloader.getImpl().pauseAll()
    }

}
