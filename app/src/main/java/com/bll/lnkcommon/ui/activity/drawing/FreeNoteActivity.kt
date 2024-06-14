package com.bll.lnkcommon.ui.activity.drawing

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.*
import com.bll.lnkcommon.greendao.StringConverter
import com.bll.lnkcommon.manager.FreeNoteDaoManager
import com.bll.lnkcommon.mvp.model.FreeNoteBean
import com.bll.lnkcommon.mvp.model.ShareNoteList
import com.bll.lnkcommon.mvp.presenter.ShareNotePresenter
import com.bll.lnkcommon.mvp.view.IContractView.IShareNoteView
import com.bll.lnkcommon.utils.*
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.ac_free_note.*
import kotlinx.android.synthetic.main.ac_free_note.v_content
import kotlinx.android.synthetic.main.common_drawing_tool.*
import java.io.File

class FreeNoteActivity:BaseDrawingActivity(),IShareNoteView {

    private val presenter=ShareNotePresenter(this)
    private var bgRes=""
    private var freeNoteBean:FreeNoteBean?=null
    private var posImage=0
    private var images= mutableListOf<String>()//手写地址
    private var bgResList= mutableListOf<String>()//背景地址
    private var sharePopWindow:PopupFreeNoteShareList?=null
    private var shareTotal=0//分享总数
    private var shareNotes= mutableListOf<ShareNoteList.ShareNoteBean>()
    private var sharePosition=0//分享列表position
    private var friendIds= mutableListOf<Int>()

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
                    map["userIds"]=friendIds
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
        freeNoteBean=FreeNoteDaoManager.getInstance().queryBean()
        if (freeNoteBean==null){
            createFreeNote()
        }
        posImage=freeNoteBean?.page!!
        if (isLoginState())
            fetchShareNotes(1,false)
    }
    override fun initView() {
        disMissView(iv_catalog,iv_btn)

        iv_save.setOnClickListener {
            freeNoteBean?.isSave=true
            saveFreeNote()
            createFreeNote()
            posImage=0
            initFreeNote()
            setContentImage()
        }

        tv_name.setOnClickListener {
            InputContentDialog(this,tv_name.text.toString()).builder().setOnDialogClickListener{
                tv_name.text=it
                freeNoteBean?.title=it
            }
        }

        tv_theme.setOnClickListener {
            ModuleSelectDialog(this,0,DataBeanManager.freenoteModules).builder()
                ?.setOnDialogClickListener { moduleBean ->
                    bgRes=ToolUtils.getImageResStr(this, moduleBean.resContentId)
                    v_content.setImageResource(ToolUtils.getImageResId(this,bgRes))
                    bgResList[posImage]=bgRes
                }
        }

        tv_delete.setOnClickListener {
            CommonDialog(this).setContent("确定删除当前随笔？").builder().setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    FreeNoteDaoManager.getInstance().deleteBean(freeNoteBean)
                    FileUtils.deleteFile(File(FileAddress().getPathFreeNote(DateUtils.longToString(freeNoteBean?.date!!))))
                    if (freeNoteBean?.isSave==true){
                        freeNoteBean=FreeNoteDaoManager.getInstance().queryBean()
                        posImage=freeNoteBean?.page!!
                    }
                    else{
                        createFreeNote()
                        posImage=0
                    }
                    showView(iv_save)
                    initFreeNote()
                    setContentImage()
                }
            })
        }

        tv_free_list.setOnClickListener {
            PopupFreeNoteList(this,tv_free_list,freeNoteBean!!.date).builder().setOnSelectListener{
                saveFreeNote()
                freeNoteBean=it
                posImage=freeNoteBean?.page!!
                initFreeNote()
                if (freeNoteBean?.isSave==true){
                    disMissView(iv_save)
                }
                else{
                    showView(iv_save)
                }
                setContentImage()
            }
        }

        tv_share_list.setOnClickListener {
            if (!isLoginState()){
                showToast("未登录")
                return@setOnClickListener
            }
            if (sharePopWindow==null){
                sharePopWindow=PopupFreeNoteShareList(this,tv_share_list,shareTotal).builder()
                sharePopWindow?.setData(shareNotes)
                sharePopWindow?.setOnClickListener(object : PopupFreeNoteShareList.OnClickListener {
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
            if (!isLoginState()){
                showToast("未登录")
                return@setOnClickListener
            }
            if (DataBeanManager.friends.size==0){
                showToast("未加好友")
                return@setOnClickListener
            }
            FriendSelectorDialog(this).builder().setOnDialogClickListener{
                friendIds= it as MutableList<Int>
                presenter.getToken()
            }
        }

        initFreeNote()
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

    private fun initFreeNote(){
        bgResList= freeNoteBean?.bgRes as MutableList<String>
        images= freeNoteBean?.paths as MutableList<String>
        tv_name.text=freeNoteBean?.title
    }

    /**
     * 创建新随笔
     */
    private fun createFreeNote(){
        bgRes= ToolUtils.getImageResStr(this,R.mipmap.icon_freenote_bg_1)
        freeNoteBean= FreeNoteBean()
        freeNoteBean?.date=System.currentTimeMillis()
        freeNoteBean?.title=DateUtils.longToStringNoYear(freeNoteBean?.date!!)
        freeNoteBean?.userId=if (isLoginState()) getUser()?.accountId else 0
        freeNoteBean?.bgRes= arrayListOf(bgRes)
        freeNoteBean?.paths= arrayListOf()
        FreeNoteDaoManager.getInstance().insertOrReplace(freeNoteBean)
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
        tv_page.text="${posImage+1}"
        tv_page_total.text="${images.size}"
        elik?.setLoadFilePath(path, true)
    }

    private fun saveFreeNote(){
        val path=FileAddress().getPathFreeNote(DateUtils.longToString(freeNoteBean?.date!!))
        if (!File(path).list().isNullOrEmpty()){
            freeNoteBean?.paths = images
            freeNoteBean?.bgRes = bgResList
            freeNoteBean?.page=posImage
            FreeNoteDaoManager.getInstance().insertOrReplace(freeNoteBean)
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
                    freeNoteBean.isSave=true
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
