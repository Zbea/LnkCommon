package com.bll.lnkcommon.ui.activity.book

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.view.EinkPWInterface
import android.view.PWDrawObjectHandler
import android.widget.ImageView
import com.bll.lnkcommon.Constants.TEXT_BOOK_EVENT
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.AppToolDialog
import com.bll.lnkcommon.dialog.CatalogDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.mvp.model.CatalogChildBean
import com.bll.lnkcommon.mvp.model.CatalogMsg
import com.bll.lnkcommon.mvp.model.CatalogParentBean
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ac_drawing.*
import kotlinx.android.synthetic.main.common_drawing_bottom.*
import org.greenrobot.eventbus.EventBus
import java.io.File


class BookDetailsActivity:BaseDrawingActivity() {
    private var book: Book?=null
    private var catalogMsg: CatalogMsg?=null
    private var catalogs= mutableListOf<MultiItemEntity>()
    private var parentItems= mutableListOf<CatalogParentBean>()
    private var childItems= mutableListOf<CatalogChildBean>()

    private var count = 1
    private var page = 0 //当前页码

    override fun layoutId(): Int {
        return R.layout.ac_drawing
    }

    override fun initData() {
        val id=intent.getIntExtra("book_id",0)
        val type=intent.getIntExtra("book_type",0)
        book = BookDaoManager.getInstance().queryTextBookByBookID(type,id)
        page=book?.pageIndex!!

        val catalogFilePath =FileAddress().getPathTextBookCatalog(book?.bookPath!!)
        val catalogMsgStr = FileUtils.readFileContent(FileUtils.file2InputStream(File(catalogFilePath)))
        catalogMsg=Gson().fromJson(catalogMsgStr, CatalogMsg::class.java)
        if (catalogMsg==null) return
        for (item in catalogMsg?.contents!!)
        {
            val catalogParentBean=CatalogParentBean()
            catalogParentBean.title=item.title
            catalogParentBean.pageNumber=item.pageNumber
            catalogParentBean.picName=item.picName
            for (ite in item.subItems){
                val catalogChildBean= CatalogChildBean()
                catalogChildBean.title=ite.title
                catalogChildBean.pageNumber=ite.pageNumber
                catalogChildBean.picName=ite.picName

                catalogParentBean.addSubItem(catalogChildBean)
                childItems.add(catalogChildBean)
            }
            parentItems.add(catalogParentBean)
            catalogs.add(catalogParentBean)
        }
    }

    override fun initView() {
        setDrawingTitleClick(false)
        if (catalogMsg!=null){
            tv_page_title.text=catalogMsg?.title!!
            count=catalogMsg?.totalCount!!
        }

        updateScreen()

        iv_catalog.setOnClickListener {
            CatalogDialog(this,catalogs,1).builder().
            setOnDialogClickListener { position ->
                page = position-1
                updateScreen()
            }

        }

    }

    override fun onPageDown() {
        if(page<count){
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
        tv_page.text="${page+1}/$count"
        loadPicture(page,elik!!,v_content)
    }


    //加载图片
    private fun loadPicture(index: Int,elik:EinkPWInterface,view:ImageView) {
        val showFile = getIndexFile(index)
        if (showFile!=null){
            book?.pageUrl=showFile.path //设置当前页面路径
            GlideUtils.setImageFile(this,showFile,view)

            val drawPath=book?.bookDrawPath+"/${index+1}.tch"
            elik.setLoadFilePath(drawPath,true)
            elik.setDrawEventListener(object : EinkPWInterface.PWDrawEvent {
                override fun onTouchDrawStart(p0: Bitmap?, p1: Boolean) {
                }

                override fun onTouchDrawEnd(p0: Bitmap?, p1: Rect?, p2: ArrayList<Point>?) {
                }

                override fun onOneWordDone(p0: Bitmap?, p1: Rect?) {
                    elik.saveBitmap(true) {}
                }

            })
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
        BookDaoManager.getInstance().insertOrReplaceBook(book)
            EventBus.getDefault().post(TEXT_BOOK_EVENT)
    }



}