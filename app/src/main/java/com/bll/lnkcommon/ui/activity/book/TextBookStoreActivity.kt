package com.bll.lnkcommon.ui.activity.book

import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants.TEXT_BOOK_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.BookDetailsDialog
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.book.Book
import com.bll.lnkcommon.mvp.book.BookStore
import com.bll.lnkcommon.mvp.book.BookStoreType
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.presenter.BookStorePresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileBigDownManager
import com.bll.lnkcommon.utils.FileDownManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.ac_list_tab.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.DecimalFormat

class TextBookStoreActivity : BaseActivity(), IContractView.IBookStoreView {

    private var tabId = 0
    private var tabStr = ""
    private val presenter = BookStorePresenter(this)
    private var books = mutableListOf<Book>()
    private var mAdapter: BookAdapter? = null

    private var provinceStr = ""
    private var gradeId=0
    private var semester=0
    private var courseId=0//科目
    private var subType=0//教库分类
    private var bookDetailsDialog: BookDetailsDialog? = null
    private var mBook: Book? = null

    private var subjectList = mutableListOf<PopupBean>()
    private var semesterList = mutableListOf<PopupBean>()
    private var provinceList = mutableListOf<PopupBean>()
    private var gradeList = mutableListOf<PopupBean>()
    private var tabList = mutableListOf<String>()
    private var subTypeList = mutableListOf<PopupBean>()
    private var bookVersion = mutableListOf<ItemList>()

    override fun onBook(bookStore: BookStore) {
        setPageNumber(bookStore.total)
        books = bookStore.list
        mAdapter?.setNewData(books)
    }

    override fun onType(bookStoreType: BookStoreType) {
        if (!bookStoreType.bookLibType.isNullOrEmpty()){
            subTypeList.clear()
            val types=bookStoreType.bookLibType
            for (item in types){
                subTypeList.add(PopupBean(item.type,item.desc,types.indexOf(item)==0))
            }
            subType=subTypeList[0].id

            if (subjectList.size>0){
                courseId=subjectList[0].id
                gradeId=gradeList[0].id
                initSelectorView()
                fetchData()
            }
        }
        bookVersion=bookStoreType.bookVersion
    }

    override fun buyBookSuccess() {
        mBook?.buyStatus = 1
        bookDetailsDialog?.setChangeStatus()
        mAdapter?.notifyDataSetChanged()
    }


    override fun layoutId(): Int {
        return R.layout.ac_list_tab
    }

    override fun initData() {
        pageSize=12

        tabList = DataBeanManager.textbookType.toMutableList()
        tabStr = tabList[0]

        semesterList=DataBeanManager.popupSemesters
        semester= semesterList[0].id

        for (i in DataBeanManager.provinces.indices){
            provinceList.add(PopupBean(i,DataBeanManager.provinces[i].value,i==0))
        }
        provinceStr=provinceList[0].name

        subjectList=DataBeanManager.popupCourses(1)
        gradeList=DataBeanManager.popupGrades

        presenter.getBookType()
    }

    override fun initView() {
        setPageTitle("教材")
        showView(tv_province,tv_course,tv_grade,tv_semester)

        mDialog?.setOutside(true)

        initRecyclerView()
        initTab()
    }


    /**
     * 设置分类选择
     */
    private fun initSelectorView() {
        tv_province.text = provinceStr
        tv_grade.text = gradeList[0].name
        tv_semester.text = DataBeanManager.popupSemesters[semester-1].name
        tv_course.text = subjectList[0].name
        tv_type.text=subTypeList[0].name

        tv_grade.setOnClickListener {
            PopupRadioList(this, gradeList, tv_grade, tv_grade.width,5).builder()
               .setOnSelectListener { item ->
                gradeId = item.id
                tv_grade.text = item.name
                pageIndex = 1
                fetchData()
            }
        }

        tv_province.setOnClickListener {
            PopupRadioList(this, provinceList, tv_province,tv_province.width, 5).builder()
             .setOnSelectListener { item ->
                provinceStr = item.name
                tv_province.text = item.name
                pageIndex = 1
                fetchData()
            }
        }

        tv_semester.setOnClickListener {
            PopupRadioList(this, semesterList, tv_semester, tv_semester.width, 5).builder()
                .setOnSelectListener { item ->
                    tv_semester.text = item.name
                    semester=item.id
                    pageIndex = 1
                    fetchData()
                }
        }

        tv_course.setOnClickListener {
            PopupRadioList(this, subjectList, tv_course, tv_course.width, 5).builder()
                .setOnSelectListener { item ->
                    courseId = item.id
                    tv_course.text = item.name
                    pageIndex = 1
                    fetchData()
                }
        }

        tv_type.setOnClickListener {
            PopupRadioList(this, subTypeList, tv_type, 5).builder()
                .setOnSelectListener { item ->
                    subType = item.id
                    tv_type.text = item.name
                    pageIndex = 1
                    fetchData()
                }
        }

    }

    private fun initTab(){
        for (i in tabList.indices) {
            itemTabTypes.add(ItemTypeBean().apply {
                title=tabList[i]
                isCheck=i==0
            })
        }
        mTabTypeAdapter?.setNewData(itemTabTypes)
    }

    override fun onTabClickListener(view: View, position: Int) {
        when(position){
            0,2->{
                showView(tv_course,tv_grade,tv_semester,tv_province)
                disMissView(tv_type)
            }
            1,3->{
                showView(tv_grade,tv_course,tv_semester)
                disMissView(tv_province,tv_type)
            }
        }
        tabId = position
        tabStr = tabList[position]
        pageIndex = 1
        fetchData()
    }


    /**
     * 得到课本主类型
     */
    private fun getHostType():Int{
        return when(tabId){0,1->0 else->6}
    }

    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(DP2PX.dip2px(this,30f),DP2PX.dip2px(this,50f),DP2PX.dip2px(this,30f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = BookAdapter(R.layout.item_bookstore, books)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            mBook = books[position]
            showBookDetails(mBook!!)
        }
        rv_list?.addItemDecoration(SpaceGridItemDeco(4,60))
    }

    /**
     * 展示书籍详情
     */
    private fun showBookDetails(book: Book) {
        bookDetailsDialog = BookDetailsDialog(this, book)
        bookDetailsDialog?.builder()
        bookDetailsDialog?.setOnClickListener {
            if (book.buyStatus == 1) {
                val localBook = BookDaoManager.getInstance().queryTextBookByBookID(getHostType(),book.bookId)
                if (localBook == null) {
                    downLoadStart(book.bodyUrl, book)
                } else {
                    book.loadSate = 2
                    showToast("已下载")
                    bookDetailsDialog?.setDissBtn()
                }
            } else {
                val map = HashMap<String, Any>()
                map["bookId"] = book.bookId
                when(tabId){
                    0,1->{
                        map["type"] = 2
                    }
                    2,3->{
                        map["type"] = 1
                    }
                }
                presenter.buyBook(map)
            }
        }
    }

    /**
     * 下载解压书籍
     */
    private fun downLoadStart(url: String, book: Book): BaseDownloadTask? {
        showLoading()
        val fileName =  book.bookId.toString()//文件名
        val path = if (tabId>1){
            FileAddress().getPathZip(fileName)
        } else{
            FileAddress().getPathBook(fileName+ MethodManager.getUrlFormat(book.bodyUrl))
        }
        val download = FileBigDownManager.with(this).create(url).setPath(path)
            .startSingleTaskDownLoad(object : FileBigDownManager.SingleTaskCallBack {

                override fun progress(task: BaseDownloadTask?, soFarBytes: Long, totalBytes: Long) {
                    if (task != null && task.isRunning) {
                        runOnUiThread {
                            val s = getFormatNum(soFarBytes.toDouble() / (1024 * 1024),) + "/" +
                                    getFormatNum(totalBytes.toDouble() / (1024 * 1024),)
                            bookDetailsDialog?.setUnClickBtn(s)
                        }
                    }
                }

                override fun paused(task: BaseDownloadTask?, soFarBytes: Long, totalBytes: Long) {
                }

                override fun completed(task: BaseDownloadTask?) {
                    book.apply {
                        loadSate = 2
                        category = 0
                        typeId=getHostType()
                        subtypeStr=tabStr
                        time = System.currentTimeMillis()//下载时间用于排序
                    }
                    if (tabId<2){
                        book.bookPath = path
                        book.bookDrawPath=FileAddress().getPathBookDraw(fileName)
                        BookDaoManager.getInstance().insertOrReplaceBook(book)
                        refreshView(book)
                    }
                    else{
                        val fileTargetPath = FileAddress().getPathTextBook(fileName)
                        book.bookPath = fileTargetPath
                        book.bookDrawPath=FileAddress().getPathTextBookDraw(fileName)
                        unzip(book, path, fileTargetPath)
                    }
                }

                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    //删除缓存 poolmap
                    hideLoading()
                    showToast("${book.bookName}下载失败")
                    bookDetailsDialog?.setChangeStatus()
                }
            })
        return download
    }

    /**
     * 下载完成书籍解压
     */
    private fun unzip(book: Book, zipPath: String, fileTargetPath: String) {
        ZipUtils.unzip(zipPath, fileTargetPath, object : IZipCallback {
            override fun onFinish() {
                //下载解压完成后更新存储的book
                BookDaoManager.getInstance().insertOrReplaceBook(book)
                FileUtils.deleteFile(File(zipPath))
                refreshView(book)
            }
            override fun onProgress(percentDone: Int) {
            }
            override fun onError(msg: String?) {
                hideLoading()
                showToast(book.bookName+msg!!)
                bookDetailsDialog?.setChangeStatus()
            }
            override fun onStart() {
            }
        })
    }

    private fun refreshView(book: Book){
        EventBus.getDefault().post(TEXT_BOOK_EVENT)
        bookDetailsDialog?.dismiss()
        Handler().postDelayed({
            hideLoading()
            showToast(book.bookName+"下载成功")
        },500)
    }


    fun getFormatNum(pi: Double): String? {
        val df = DecimalFormat("0.0M")
        return df.format(pi)
    }

    override fun onDestroy() {
        super.onDestroy()
        FileDownloader.getImpl().pauseAll()
    }

    override fun fetchData() {
        books.clear()
        mAdapter?.notifyDataSetChanged()

        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        map["subjectName"]=courseId
        map["grade"] = gradeId
        map["semester"]=semester
        when (tabId) {
            0->{
                map["type"] = 1
                map["area"] = provinceStr
                presenter.getTextBooks(map)
            }
            1->{
                map["type"] = 2
                presenter.getTextBooks(map)
            }
            2->{
                map["type"] = 1
                map["area"] = provinceStr
                presenter.getHomeworkBooks(map)
            }
            3->{
                map["type"] = 2
                presenter.getHomeworkBooks(map)
            }
        }
    }

    override fun onRefreshData() {
        presenter.getBookType()
    }
}