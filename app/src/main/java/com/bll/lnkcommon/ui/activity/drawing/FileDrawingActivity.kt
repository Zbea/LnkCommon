package com.bll.lnkcommon.ui.activity.drawing

import android.graphics.BitmapFactory
import android.view.EinkPWInterface
import android.widget.ImageView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFileDrawingActivity
import com.bll.lnkcommon.utils.FileUtils
import kotlinx.android.synthetic.main.ac_drawing_file.*
import kotlinx.android.synthetic.main.common_drawing_tool.*


class FileDrawingActivity : BaseFileDrawingActivity() {

    private var path=""

    override fun layoutId(): Int {
        return R.layout.ac_drawing_file
    }

    override fun initData() {
        pageIndex = intent.getIntExtra("pageIndex", 0)
        path= intent.getStringExtra("pagePath").toString()
        pageCount=FileUtils.getFiles(path).size
    }

    override fun initView() {
        disMissView(iv_btn,iv_catalog)

        onChangeContent()
    }

    override fun onPageUp() {
        if (pageIndex > 0) {
            pageIndex -= 1
            onChangeContent()
        }
    }

    override fun onPageDown() {
        if (pageIndex<pageCount-1){
            pageIndex+=1
            onChangeContent()
        }
    }

    /**
     * 更新内容
     */
    private fun onChangeContent() {
        if (pageCount==0)
            return
        if (pageIndex>=pageCount){
            pageIndex=pageCount-1
            return
        }
        tv_page.text = "${pageIndex+1}"
        tv_page_total.text="$pageCount"
        loadPicture(pageIndex, elik!!, iv_content)
    }

    //加载图片
    private fun loadPicture(index: Int, elik: EinkPWInterface, view: ImageView) {
        val files = FileUtils.getFiles(path)
        if (index<files.size){
            val showFile=files[index]
            if (showFile != null) {
                val myBitmap= BitmapFactory.decodeFile(showFile.absolutePath)
                view.setImageBitmap(myBitmap)
                elik.setLoadFilePath(getDrawingPath(index+1), true)
            }
        }
    }

    /**
     * 得到提错本的手写路径
     */
    private fun getDrawingPath(index: Int):String{
        return "$path/drawing/$index.tch"
    }

}