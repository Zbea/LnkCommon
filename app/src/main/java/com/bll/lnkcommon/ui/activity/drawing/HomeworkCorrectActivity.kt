package com.bll.lnkcommon.ui.activity.drawing

import android.os.Handler
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList.CorrectBean
import com.bll.lnkcommon.mvp.presenter.HomeworkCorrectPresenter
import com.bll.lnkcommon.mvp.presenter.QiniuPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IHomeworkCorrectView
import com.bll.lnkcommon.mvp.view.IContractView.IQiniuView
import com.bll.lnkcommon.utils.BitmapUtils
import com.bll.lnkcommon.utils.FileImageUploadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.utils.ToolUtils
import kotlinx.android.synthetic.main.ac_drawing.v_content
import kotlinx.android.synthetic.main.common_drawing_tool.iv_btn
import kotlinx.android.synthetic.main.common_drawing_tool.iv_catalog
import kotlinx.android.synthetic.main.common_drawing_tool.iv_tool
import kotlinx.android.synthetic.main.common_drawing_tool.tv_page
import kotlinx.android.synthetic.main.common_drawing_tool.tv_page_total
import org.greenrobot.eventbus.EventBus
import java.io.File

class HomeworkCorrectActivity:BaseDrawingActivity(),IHomeworkCorrectView ,IQiniuView{

    private val presenter=HomeworkCorrectPresenter(this)
    private var mQiniuPresenter= QiniuPresenter(this)
    private var correctBean:CorrectBean?=null
    private var images= mutableListOf<String>()
    private var posImage=0
    private var url=""

    override fun onToken(token: String) {
        showLoading()
        //获取合图的图片，没有手写的页面那原图
        val paths= mutableListOf<String>()
        for (i in images.indices){
            val mergePath=getPathMergeStr(i+1)
            if (File(mergePath).exists()){
                paths.add(mergePath)
            }
        }
        FileImageUploadManager(token, paths).apply {
            startUpload()
            setCallBack(object : FileImageUploadManager.UploadCallBack {
                override fun onUploadSuccess(urls: List<String>) {
                    //校验正确图片，没有手写图片拿原图
                    val uploadPaths= mutableListOf<String>()
                    var index=0
                    for (i in images.indices){
                        val mergePath=getPathMergeStr(i+1)
                        if (File(mergePath).exists()){
                            uploadPaths.add(urls[index])
                            index+=1
                        }
                        else{
                            uploadPaths.add(images[i])
                        }
                    }
                    url=ToolUtils.getImagesStr(uploadPaths)
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
        disMissView(iv_catalog)
        setDisableTouchInput(true)
        //批改完成之后删除文件夹
        FileUtils.deleteFile(File(getPath()))
        EventBus.getDefault().post(Constants.HOMEWORK_CORRECT_EVENT)
    }

    override fun layoutId(): Int {
        return R.layout.ac_drawing
    }
    override fun initData() {
        correctBean= intent.getBundleExtra("bundle")?.getSerializable("correctBean") as CorrectBean?
        val path=if (correctBean?.status==2) correctBean?.submitUrl else correctBean?.changeUrl
        images=path!!.split(",") as MutableList<String>

    }
    override fun initView() {
        disMissView(iv_btn,iv_tool,iv_catalog)
        iv_catalog.setImageResource(R.mipmap.icon_draw_commit)

        if (correctBean?.status==2)
            showView(iv_catalog)

        iv_catalog.setOnClickListener {
            CommonDialog(this).setContent("确定批改以及发送？").builder().setDialogClickListener(object :
                CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    mQiniuPresenter.getToken()
                }
            })
        }

        setContentImage()
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
     * 设置学生提交图片展示
     */
    private fun setContentImage(){
        tv_page.text="${posImage+1}"
        tv_page_total.text="${images.size}"
        setDisableTouchInput(correctBean?.status!=2)
        GlideUtils.setImageCacheUrl(this, images[posImage],v_content)
        val drawPath = getPathDrawStr(posImage+1)
        elik?.setLoadFilePath(drawPath, true)
    }

    override fun onElikSava() {
        Thread {
            BitmapUtils.saveScreenShot(v_content, getPathMergeStr(posImage+1))
        }.start()
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
        return getPath()+"/draw/${index}.png"//手绘地址
    }

    /**
     * 得到当前合图地址
     */
    private fun getPathMergeStr(index: Int):String{
        return getPath()+"/merge/${index}.png"//手绘地址
    }

}