package com.bll.lnkcommon.ui.activity.drawing

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList.CorrectBean
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.presenter.HomeworkCorrectPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IHomeworkCorrectView
import com.bll.lnkcommon.utils.*
import com.google.gson.Gson
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.ac_drawing.*
import kotlinx.android.synthetic.main.common_drawing_tool.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class HomeworkCorrectActivity:BaseDrawingActivity(),IHomeworkCorrectView {

    private val presenter=HomeworkCorrectPresenter(this)
    private var correctBean:CorrectBean?=null
    private var images= mutableListOf<String>()
    private val savePaths= mutableListOf<String>()
    private var posImage=0
    private val commitItems = mutableListOf<ItemList>()
    private var url=""

    override fun onList(list: HomeworkCorrectList?) {
    }
    override fun onToken(token: String) {
        showLoading()
        val commitPaths = mutableListOf<String>()
        for (item in commitItems) {
            commitPaths.add(item.url)
        }
        FileImageUploadManager(token, commitPaths).apply {
            startUpload()
            setCallBack(object : FileImageUploadManager.UploadCallBack {
                override fun onUploadSuccess(urls: List<String>) {
                    url=ToolUtils.getImagesStr(urls)
                    val map= HashMap<String, Any>()
                    map["id"]=correctBean?.id!!
                    map["changeUrl"]=url
                    presenter.commitPaperStudent(map)
                }
                override fun onUploadFail() {
                    hideLoading()
                    showToast("上传失败")
                }
            })
        }
    }
    override fun onUpdateSuccess() {
        showToast("批改成功")
        correctBean?.changeUrl=url
        correctBean?.status=3
        disMissView(tv_save)
        setDisableTouchInput(true)
        //批改完成之后删除文件夹
        FileUtils.deleteFile(File(getPath()))
        EventBus.getDefault().post(Constants.HOMEWORK_CORRECT_EVENT)
    }
    override fun onDeleteSuccess() {
    }


    override fun layoutId(): Int {
        return R.layout.ac_drawing
    }
    override fun initData() {
        correctBean= intent.getBundleExtra("bundle")?.getSerializable("correctBean") as CorrectBean?
    }
    override fun initView() {
        disMissView(iv_btn,iv_tool,iv_catalog)
        elik?.addOnTopView(tv_save)

        if (correctBean?.status==2)
        {
            showView(tv_save)
            images= correctBean?.submitUrl!!.split(",") as MutableList<String>
            for (i in images.indices){
                savePaths.add(getPath()+"/${i+1}.png")
            }
            loadPapers()
        }
        else{
            images= correctBean?.changeUrl!!.split(",") as MutableList<String>
            setContentImage()
        }

        tv_save.setOnClickListener {
            showLoading()
            //延迟以保证手写及时保存
            Handler().postDelayed({
                commitPapers()
            },500)
        }

    }

    override fun onPageDown() {
        if (posImage< images.size-1){
            posImage+=1
            setContentImage()
        }
    }

    override fun onPageUp() {
        if (posImage>0){
            posImage-=1
            setContentImage()
        }
    }

    /**
     * 下载学生作业
     */
    private fun loadPapers(){
        if (!FileUtils.isExistContent(getPath())) {
            showLoading()
            FileMultitaskDownManager.with(this).create(images).setPath(savePaths).startMultiTaskDownLoad(
                object : FileMultitaskDownManager.MultiTaskCallBack {
                    override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int, ) {
                    }
                    override fun completed(task: BaseDownloadTask?) {
                        hideLoading()
                        setContentImage()
                    }
                    override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                    }
                    override fun error(task: BaseDownloadTask?, e: Throwable?) {
                        hideLoading()
                    }
                })
        }
        else{
            setContentImage()
        }
    }

    /**
     * 设置学生提交图片展示
     */
    private fun setContentImage(){
        tv_page.text="${posImage+1}"
        tv_page_total.text="${images.size}"
        setDisableTouchInput(correctBean?.status!=2)
        //批改成功后加载提交后的图片
        if (correctBean?.status==2){
            GlideUtils.setImageUrl(this, File(savePaths[posImage]).path,v_content)
            val drawPath = getPathDrawStr(posImage+1)
            elik?.setLoadFilePath(drawPath, true)
        }
        else{
            GlideUtils.setImageUrl(this, images[posImage],v_content)
        }
    }

    /**
     * 文件路径
     */
    private fun getPath():String{
        return FileAddress().getPathCorrect(correctBean?.id!!)
    }

    /**
     * 得到当前手绘图片
     */
    private fun getPathDrawStr(index: Int):String{
        return getPath()+"/draw${index}.png"//手绘地址
    }

    /**
     * 提交学生考卷
     */
    private fun commitPapers(){
        commitItems.clear()
        //手写,图片合图
        for (i in images.indices){
            val index=i+1
            val path=savePaths[i]
            val drawPath = getPathDrawStr(index)
            Thread {
                BitmapUtils.mergeBitmap(path, drawPath)
                commitItems.add(ItemList().apply {
                    id = i
                    url = path
                })
                if (commitItems.size==images.size){
                    commitItems.sort()
                    presenter.getToken()
                }
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FileDownloader.getImpl().pauseAll()
    }

}