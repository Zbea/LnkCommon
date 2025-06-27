package com.bll.lnkcommon.ui.activity.drawing

import android.graphics.BitmapFactory
import android.view.EinkPWInterface
import android.widget.ImageView
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFileDrawingActivity
import com.bll.lnkcommon.dialog.CatalogDialog
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.utils.FileUtils
import kotlinx.android.synthetic.main.ac_drawing_file.*
import kotlinx.android.synthetic.main.common_drawing_tool.*
import java.io.File


class FileDrawingActivity : BaseFileDrawingActivity() {

    private var path=""

    override fun layoutId(): Int {
        return R.layout.ac_drawing_file
    }

    override fun initData() {
        pageIndex = intent.getIntExtra("pageIndex", 0)
        path= intent.getStringExtra("pagePath").toString()
        pageCount=FileUtils.getAscTimeFiles(path).size
    }

    override fun initView() {
        disMissView(iv_btn)

        onChangeContent()
    }

    override fun onCatalog() {
        val files = FileUtils.getAscTimeFiles(path)
        val list= mutableListOf<ItemList>()
        for (file in files){
            val itemList= ItemList()
            itemList.name=file.name.replace(".png","")
            itemList.page=files.indexOf(file)
            list.add(itemList)
        }
        CatalogDialog(this,list,false).builder().setOnDialogClickListener(object : CatalogDialog.OnDialogClickListener {
            override fun onClick(pageNumber: Int) {
                if (pageIndex!=pageNumber){
                    pageIndex = pageNumber
                    onChangeContent()
                }
            }
        })
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
        val files = FileUtils.getAscTimeFiles(path)
        if (index<files.size){
            val showFile=files[index]
            MethodManager.setImageFile(showFile.absolutePath,view)
            elik.setLoadFilePath(getDrawingPath(showFile), true)
        }
    }

    /**
     * 得到提错本的手写路径
     */
    private fun getDrawingPath(file: File):String{
        return "$path/drawing/${file.name}"
    }

}