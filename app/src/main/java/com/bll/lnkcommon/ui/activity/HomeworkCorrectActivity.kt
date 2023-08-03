package com.bll.lnkcommon.ui.activity

import android.content.res.AssetManager
import android.content.res.AssetManager.AssetInputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.view.EinkPWInterface
import android.view.PWDrawObjectHandler
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList.CorrectBean
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.presenter.HomeworkCorrectPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IHomeworkCorrectView
import com.bll.lnkcommon.utils.*
import kotlinx.android.synthetic.main.ac_homework_correct.*
import kotlinx.android.synthetic.main.common_drawing_bottom.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class HomeworkCorrectActivity:BaseActivity(),IHomeworkCorrectView {

    private val presenter=HomeworkCorrectPresenter(this)
    private var elik: EinkPWInterface?=null
    private var isErasure=false
    private var correctBean:CorrectBean?=null
    private var images= mutableListOf<String>()
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
        //批改完成之后删除文件夹
        FileUtils.deleteFile(File(getPath()))
        elik?.setPWEnabled(false)
        EventBus.getDefault().post(Constants.HOMEWORK_CORRECT_EVENT)
    }
    override fun onDeleteSuccess() {
    }

    override fun layoutId(): Int {
        return R.layout.ac_homework_correct
    }
    override fun initData() {
        correctBean= intent.getBundleExtra("bundle").getSerializable("correctBean") as CorrectBean?
    }
    override fun initView() {
        setPageTitle(correctBean?.content.toString())
        elik=iv_image.pwInterFace
        if (correctBean?.status==2)
        {
            showView(iv_manager)
            iv_manager.setImageResource(R.mipmap.icon_save)
            images= correctBean?.submitUrl!!.split(",") as MutableList<String>
            loadPapers()
        }
        else{
            elik?.setPWEnabled(false)
            images= correctBean?.changeUrl!!.split(",") as MutableList<String>
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

        iv_page_up.setOnClickListener {
            if (posImage>0){
                posImage-=1
                setContentImage()
            }
        }
        iv_page_down.setOnClickListener {
            if (posImage< images.size-1){
                posImage+=1
                setContentImage()
            }
        }

        iv_manager.setOnClickListener {
            showLoading()
            Handler().postDelayed( {
                commitPapers()
            },1000)
        }

    }

    /**
     * 下载学生作业
     */
    private fun loadPapers(){
        showLoading()
        val file = File(getPath())
        val files = FileUtils.getFiles(file.path)
        if (files.isNullOrEmpty()) {
            val imageDownLoad = ImageDownLoadUtils(this, images.toTypedArray(), file.path)
            imageDownLoad.startDownload()
            imageDownLoad.setCallBack(object : ImageDownLoadUtils.ImageDownLoadCallBack {
                override fun onDownLoadSuccess(map: MutableMap<Int, String>?) {
                    hideLoading()
                    runOnUiThread {
                        setContentImage()
                    }
                }
                override fun onDownLoadFailed(unLoadList: MutableList<Int>?) {
                    imageDownLoad.reloadImage()
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
        hideLoading()
        tv_page.text="${posImage+1}/${images.size}"
        //批改成功后加载提交后的图片
        if (correctBean?.status==3){
            GlideUtils.setImageUrl(this, images[posImage],iv_image)
        }
        else{
            elik?.setPWEnabled(true)
            val masterImage="${getPath()}/${posImage+1}.png"//原图
            GlideUtils.setImageFile(this, File(masterImage),iv_image)

            val drawPath = getPathDrawStr(posImage+1)
            elik?.setLoadFilePath(drawPath, true)
            elik?.setDrawEventListener(object : EinkPWInterface.PWDrawEvent {
                override fun onTouchDrawStart(p0: Bitmap?, p1: Boolean) {
                }

                override fun onTouchDrawEnd(p0: Bitmap?, p1: Rect?, p2: ArrayList<Point>?) {
                }

                override fun onOneWordDone(p0: Bitmap?, p1: Rect?) {
                    elik?.saveBitmap(true) {}
                }
            })
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
        return getPath()+"/draw${index}.tch"//手绘地址
    }

    /**
     * 提交学生考卷
     */
    private fun commitPapers(){
        commitItems.clear()
        //手写,图片合图
        for (i in images.indices){
            val index=i+1
            val masterImage="${getPath()}/${index}.png"//原图
            val drawPath = getPathDrawStr(index).replace("tch","png")
            val mergePath = getPath()//合并后的路径
            var mergePathStr = "${getPath()}/merge${index}.png"//合并后图片地址
            Thread {
                val oldBitmap = BitmapFactory.decodeFile(masterImage)
                val drawBitmap = BitmapFactory.decodeFile(drawPath)
                if (drawBitmap!=null){
                    val mergeBitmap = BitmapUtils.mergeBitmap(oldBitmap, drawBitmap)
                    BitmapUtils.saveBmpGallery(this, mergeBitmap, mergePath, "merge${index}")
                }
                else{
                    mergePathStr=masterImage
                }
                commitItems.add(ItemList().apply {
                    id = i
                    url = mergePathStr
                })
                if (commitItems.size==images.size){
                    commitItems.sort()
                    presenter.getToken()
                }
            }.start()

        }
    }



}