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
import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.ShareNoteList
import com.bll.lnkcommon.mvp.presenter.FreeNotePresenter
import com.bll.lnkcommon.mvp.view.IContractView.IFreeNoteView
import com.bll.lnkcommon.utils.*
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.ac_free_note.*
import kotlinx.android.synthetic.main.ac_free_note.v_content
import kotlinx.android.synthetic.main.common_drawing_tool.*
import java.io.File

class FreeNoteActivity:BaseDrawingActivity(), IFreeNoteView {

    private val presenter=FreeNotePresenter(this)
    private var bgRes=""
    private var freeNoteBean:FreeNoteBean?=null
    private var posImage=0
    private var images= mutableListOf<String>()//手写地址
    private var bgResList= mutableListOf<String>()//背景地址
    private var receivePopWindow:PopupFreeNoteReceiveList?=null
    private var receiveTotal=0//分享总数
    private var receiveNotes= mutableListOf<ShareNoteList.ShareNoteBean>()
    private var receivePosition=0//分享列表position

    private var sharePopWindow: PopupFreeNoteShareList?=null
    private var shareTotal=0//分享总数
    private var shareNotes= mutableListOf<ShareNoteList.ShareNoteBean>()

    private var friendIds= mutableListOf<Int>()
    private var friends= mutableListOf<FriendList.FriendBean>()

    override fun onReceiveList(list: ShareNoteList) {
        receiveNotes=list.list
        receiveTotal=list.total
        receivePopWindow?.setData(receiveNotes)
    }

    override fun onShareList(list: ShareNoteList) {
        shareNotes=list.list
        shareTotal=list.total
        sharePopWindow?.setData(shareNotes)
    }

    override fun onToken(token: String) {
        showLoading()
        //分享只能是有手写页面
        val sBgRes= mutableListOf<String>()
        val imagePaths= mutableListOf<String>()
        for (i in images.indices){
            val path=images[i]
            if (File(path).exists()){
                imagePaths.add(path)
                sBgRes.add(bgResList[i])
            }
        }
        FileImageUploadManager(token, imagePaths).apply {
            startUpload()
            setCallBack(object : FileImageUploadManager.UploadCallBack {
                override fun onUploadSuccess(urls: List<String>) {
                    val map=HashMap<String,Any>()
                    map["userIds"]=friendIds
                    map["title"]=freeNoteBean?.title!!
                    map["bgRes"]=ToolUtils.getImagesStr(sBgRes)
                    map["paths"]=ToolUtils.getImagesStr(urls)
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
        receivePopWindow?.deleteData(receivePosition)
    }
    override fun onShare() {
        showToast("分享成功")
        fetchShareNotes(1,false)
    }

    override fun onBind() {
        presenter.getFriends()
        showToast("添加好友成功")
    }

    override fun onUnbind() {
        val iterator = friends.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (friendIds.contains(item.friendId)) {
                iterator.remove()
            }
        }
        showToast("解绑好友成功")
    }

    override fun onListFriend(list: FriendList) {
        friends=list.list
    }


    override fun layoutId(): Int {
        return R.layout.ac_free_note
    }
    override fun initData() {
        bgRes= ToolUtils.getImageResStr(this,R.mipmap.icon_freenote_bg_1)
        freeNoteBean=FreeNoteDaoManager.getInstance().queryBean()
        freeNoteBean?.title=DateUtils.longToStringNoYear(System.currentTimeMillis())
        if (freeNoteBean==null){
            createFreeNote()
        }
        posImage=freeNoteBean?.page!!
        if (MethodManager.isLogin()&&NetworkUtil.isNetworkConnected()){
            presenter.getFriends()
            fetchReceiveNotes(1,false)
            fetchShareNotes(1,false)
        }

    }
    override fun initView() {
        tv_save.setOnClickListener {
            freeNoteBean?.isSave=true
            saveFreeNote()
            createFreeNote()
            posImage=0
            changeContent()
        }

        tv_name.setOnClickListener {
            InputContentDialog(this,tv_name.text.toString()).builder().setOnDialogClickListener{
                tv_name.text=it
                freeNoteBean?.title=it
            }
        }

        iv_btn.setOnClickListener {
            ModuleItemDialog(this,"随笔模板",DataBeanManager.freenoteModules).builder()
                .setOnDialogClickListener { moduleBean ->
                    MethodManager.setImageResource(this,moduleBean.resContentId,v_content)
                    bgResList[posImage]=ToolUtils.getImageResStr(this, moduleBean.resContentId)
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
                    showView(tv_save)
                    changeContent()
                }
            })
        }


        tv_receive_list.setOnClickListener {
            if (!MethodManager.isLogin()){
                showToast("未登录")
                return@setOnClickListener
            }
            if (receivePopWindow==null){
                receivePopWindow=PopupFreeNoteReceiveList(this,tv_receive_list,receiveTotal).builder()
                receivePopWindow?.setData(receiveNotes)
                receivePopWindow?.setOnClickListener(object : PopupFreeNoteReceiveList.OnClickListener {
                    override fun onClick(position: Int) {
                        val item=receiveNotes[position]
                        val freeNoteBean=FreeNoteDaoManager.getInstance().queryByDate(item.date)
                        setChangeFreeNote(freeNoteBean)
                    }
                    override fun onPage(pageIndex: Int) {
                        fetchReceiveNotes(pageIndex,true)
                    }
                    override fun onDelete(position: Int) {
                        receivePosition=position
                        val map=HashMap<String,Any>()
                        map["ids"]= arrayOf(receiveNotes[position].id)
                        presenter.deleteShareNote(map)
                    }
                    override fun onDownload(position: Int) {
                        downloadShareNote(receiveNotes[position])
                    }
                })
            }
            else{
                receivePopWindow?.show()
            }
        }

        tv_share_list.setOnClickListener {
            if (!MethodManager.isLogin()){
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
                })
            }
            else{
                sharePopWindow?.show()
            }
        }

        tv_share.setOnClickListener {
            if (!MethodManager.isLogin()){
                showToast("未登录")
                return@setOnClickListener
            }
            if (friends.size==0){
                showToast("未加好友")
                return@setOnClickListener
            }
            FreeNoteFriendManageDialog(this,friends).builder().setOnDialogClickListener{ type, ids->
                if (type==0){
                    val path=FileAddress().getPathFreeNote(DateUtils.longToString(freeNoteBean?.date!!))
                    if (FileUtils.isExistContent(path)){
                        friendIds= ids as MutableList<Int>
                        presenter.getToken()
                    }
                    else{
                        showToast("暂无分享内容")
                    }
                }
                else{
                    presenter.unbindFriend(ids)
                }
            }
        }

        tv_add.setOnClickListener {
            if (!MethodManager.isLogin()){
                showToast("未登录")
                return@setOnClickListener
            }
            InputContentDialog(this,"输入好友账号").builder()
                .setOnDialogClickListener { string ->
                    presenter.onBindFriend(string)
                }
        }

        changeContent()
    }

    /**
     * 切换随笔
     */
    private fun setChangeFreeNote(item:FreeNoteBean){
        saveFreeNote()
        freeNoteBean=item
        posImage=freeNoteBean?.page!!
        if (freeNoteBean?.isSave==true){
            disMissView(tv_save)
        }
        else{
            showView(tv_save)
        }
        changeContent()
    }

    private fun changeContent(){
        bgResList= freeNoteBean?.bgRes as MutableList<String>
        //兼容以前版本
        images = if (freeNoteBean?.paths.isNullOrEmpty()){
            mutableListOf(getPath(0))
        } else{
            freeNoteBean?.paths as MutableList<String>
        }
        tv_name.text=freeNoteBean?.title
        setContentImage()
    }

    /**
     * 创建新随笔
     */
    private fun createFreeNote(){
        freeNoteBean= FreeNoteBean()
        freeNoteBean?.date=System.currentTimeMillis()
        freeNoteBean?.title=DateUtils.longToStringNoYear(freeNoteBean?.date!!)
        freeNoteBean?.bgRes= arrayListOf(bgRes)
        freeNoteBean?.type=0
        freeNoteBean?.paths= mutableListOf(getPath(posImage))
        FreeNoteDaoManager.getInstance().insertOrReplace(freeNoteBean)
    }

    override fun onCatalog() {
        CatalogFreeNoteDialog(this,freeNoteBean!!.date).builder().setOnItemClickListener{
            setChangeFreeNote(it)
        }
    }

    override fun onPageDown() {
        if (posImage<images.size-1){
            posImage+=1
            setContentImage()
        }
        else{
            if (isDrawLastContent()){
                images.add(getPath(images.size))
                bgResList.add(bgRes)
                posImage=images.size-1
                setContentImage()
            }
        }
    }

    override fun onPageUp() {
        if (posImage>0){
            posImage-=1
            setContentImage()
        }
    }

    /**
     * 更换内容
     */
    private fun setContentImage(){
        MethodManager.setImageResource(this,ToolUtils.getImageResId(this,bgResList[posImage]),v_content)
        val path=getPath(posImage)
        tv_page.text="${posImage+1}"
        tv_page_total.text="${images.size}"
        elik?.setLoadFilePath(path, true)
    }

    /**
     * 最后一个是否已写
     */
    private fun isDrawLastContent():Boolean{
        val path = images.last()
        return File(path).exists()
    }

    private fun getPath(index:Int):String{
        return FileAddress().getPathFreeNote(DateUtils.longToString(freeNoteBean?.date!!)) + "/${index + 1}.png"
    }

    private fun saveFreeNote(){
        freeNoteBean?.paths = images
        freeNoteBean?.bgRes = bgResList
        freeNoteBean?.page=posImage
        FreeNoteDaoManager.getInstance().insertOrReplace(freeNoteBean)
    }

    /**
     * 下载分享随笔
     */
    private fun downloadShareNote(item:ShareNoteList.ShareNoteBean){
        val path=FileAddress().getPathFreeNote(DateUtils.longToString(item.date))
        val savePaths= mutableListOf<String>()
        val urls=item.paths.split(",")
        for (i in urls.indices)
        {
            savePaths.add(path+"/${i+1}.png")
        }
        FileMultitaskDownManager.with(this).create(urls).setPath(savePaths).startMultiTaskDownLoad(
            object : FileMultitaskDownManager.MultiTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    val freeNoteBean= FreeNoteBean()
                    freeNoteBean.userId=MethodManager.getAccountId()
                    freeNoteBean.title=item.title
                    freeNoteBean.date=item.date
                    freeNoteBean.isSave=true
                    freeNoteBean.bgRes=StringConverter().convertToEntityProperty(item.bgRes)
                    freeNoteBean.paths=savePaths
                    freeNoteBean.type=1
                    FreeNoteDaoManager.getInstance().insertOrReplace(freeNoteBean)
                    showToast("下载成功")
                    receivePopWindow?.setRefreshData()
                    setChangeFreeNote(freeNoteBean)
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    showToast("下载失败")
                }
            })
    }

    private fun fetchReceiveNotes(page:Int, isShow: Boolean){
        val map=HashMap<String,Any>()
        map["size"]=6
        map["page"]=page
        presenter.getReceiveNotes(map,isShow)
    }

    private fun fetchShareNotes(page:Int, isShow: Boolean){
        val map=HashMap<String,Any>()
        map["size"]=6
        map["page"]=page
        presenter.getShareNotes(map,isShow)
    }

    override fun onPause() {
        super.onPause()
        saveFreeNote()
    }
}
