package com.bll.lnkcommon.ui.activity.drawing

import android.view.EinkPWInterface
import android.widget.ImageView
import com.bll.lnkcommon.Constants.TEXT_BOOK_EVENT
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.CatalogDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.manager.TextbookGreenDaoManager
import com.bll.lnkcommon.mvp.book.Book
import com.bll.lnkcommon.mvp.book.TextbookBean
import com.bll.lnkcommon.mvp.model.CatalogChildBean
import com.bll.lnkcommon.mvp.model.CatalogMsg
import com.bll.lnkcommon.mvp.model.CatalogParentBean
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ac_drawing.*
import kotlinx.android.synthetic.main.common_drawing_tool.*
import org.greenrobot.eventbus.EventBus
import java.io.File


class BookDetailsActivity:BaseDrawingActivity() {
    private var book: TextbookBean?=null
    private var catalogMsg: CatalogMsg?=null
    private var catalogs= mutableListOf<MultiItemEntity>()
    private var parentItems= mutableListOf<CatalogParentBean>()
    private var childItems= mutableListOf<CatalogChildBean>()

    private var page = 0 //当前页码
    private var startCount=0

    override fun layoutId(): Int {
        return R.layout.ac_drawing
    }

    override fun initData() {
        val id=intent.getIntExtra("book_id",0)
        val type=intent.getIntExtra("book_type",0)
        book = TextbookGreenDaoManager.getInstance().queryTextBookByBookId(type,id)
        page=book?.pageIndex!!

        val catalogFilePath =FileAddress().getPathTextBookCatalog(book?.bookPath!!)
        if (FileUtils.isExist(catalogFilePath))
        {
            val cataMsgStr = FileUtils.readFileContent(FileUtils.file2InputStream(File(catalogFilePath)))
            try {
                catalogMsg = Gson().fromJson(cataMsgStr, CatalogMsg::class.java)
            } catch (e: Exception) {
            }
            if (catalogMsg!=null){
                for (item in catalogMsg?.contents!!) {
                    val catalogParent = CatalogParentBean()
                    catalogParent.title = item.title
                    catalogParent.pageNumber = item.pageNumber
                    catalogParent.picName = item.picName
                    for (ite in item.subItems) {
                        val catalogChild = CatalogChildBean()
                        catalogChild.title = ite.title
                        catalogChild.pageNumber = ite.pageNumber
                        catalogChild.picName = ite.picName
                        catalogParent.addSubItem(catalogChild)
                        childItems.add(catalogChild)
                    }
                    parentItems.add(catalogParent)
                    catalogs.add(catalogParent)
                }
                pageCount=catalogMsg?.totalCount!!
                startCount=catalogMsg?.startCount!!-1
            }
        }
    }

    override fun initView() {
        disMissView(iv_btn)
        updateScreen()
    }

    override fun onCatalog() {
        CatalogDialog(this,catalogs, 1, startCount).builder().setOnDialogClickListener(object : CatalogDialog.OnDialogClickListener {
            override fun onClick(position: Int) {
                if (page!=position-1){
                    page = position-1
                    updateScreen()
                }
            }
            override fun onEdit(position: Int, title: String) {
            }
        })
    }


    override fun onPageDown() {
        if(page<pageCount){
            page+=1
            updateScreen()
        }
    }

    override fun onPageUp() {
        if (page>1){
            page-=1
            updateScreen()
        }
    }

    //单屏翻页
    private fun updateScreen(){
        tv_page.text = if (page>=startCount) "${page-startCount+1}" else ""
        tv_page_total.text=if (page>=startCount)"${pageCount-startCount}" else ""
        loadPicture(page,elik!!,v_content)
    }


    //加载图片
    private fun loadPicture(index: Int,elik:EinkPWInterface,view:ImageView) {
        val showFile = getIndexFile(index)
        if (showFile!=null){
            book?.pageUrl=showFile.path //设置当前页面路径
            GlideUtils.setImageCacheUrl(this,showFile.path,view)

            val drawPath=book?.bookDrawPath+"/${index+1}.png"
            elik.setLoadFilePath(drawPath,true)
        }
    }

    //获得图片地址
    private fun getIndexFile(index: Int): File? {
        val path=FileAddress().getPathTextBookPicture(book?.bookPath!!)
        val listFiles = FileUtils.getFiles(path)
        return if (listFiles!=null) listFiles[index] else null
    }

    override fun onDestroy() {
        super.onDestroy()
        book?.time=System.currentTimeMillis()
        book?.pageIndex=page
        TextbookGreenDaoManager.getInstance().insertOrReplaceBook(book)
        EventBus.getDefault().post(TEXT_BOOK_EVENT)
    }
}