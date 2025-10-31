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
import com.bll.lnkcommon.dialog.DownloadTextbookDialog
import com.bll.lnkcommon.dialog.PopupCityList
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.manager.TextbookGreenDaoManager
import com.bll.lnkcommon.mvp.model.book.BookStoreType
import com.bll.lnkcommon.mvp.model.book.TextbookBean
import com.bll.lnkcommon.mvp.model.book.TextbookStore
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.presenter.BookStorePresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.TextbookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileBigDownManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.ToolUtils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.ac_list_tab.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class TextBookStoreActivity : BaseActivity(), IContractView.IBookStoreView {

    private var tabId = 0
    private var tabStr = ""
    private val presenter = BookStorePresenter(this)
    private var textbooks = mutableListOf<TextbookBean>()
    private var mAdapter: TextbookAdapter? = null

    private var provinceStr = ""
    private var gradeId=0
    private var semester=0
    private var courseId=0//科目
    private var bookDetailsDialog: DownloadTextbookDialog? = null

    private var cityPopWindow: PopupCityList?=null
    private var subjectList = mutableListOf<PopupBean>()
    private var semesterList = mutableListOf<PopupBean>()
    private var gradeList = mutableListOf<PopupBean>()
    private var position=0

    override fun onTextbook(bookStore: TextbookStore) {
        setPageNumber(bookStore.total)
        textbooks = bookStore.list
        mAdapter?.setNewData(textbooks)
    }
    override fun onType(bookStoreType: BookStoreType) {
    }
    override fun buyBookSuccess() {
        textbooks[position].buyStatus = 1
        bookDetailsDialog?.setChangeStatus()
        mAdapter?.notifyItemChanged(position)
    }


    override fun layoutId(): Int {
        return R.layout.ac_list_tab
    }

    override fun initData() {
        pageSize=12

        semesterList=DataBeanManager.popupSemesters
        semester= semesterList[0].id

        provinceStr= MethodManager.getProvinces(this)[0].children[0].value

        subjectList=DataBeanManager.popupCourses(1)
        gradeList=DataBeanManager.popupGrades
    }

    override fun initView() {
        setPageTitle("教材")
        showView(tv_province,tv_course,tv_grade,tv_semester)

        if (subjectList.size>0){
            courseId=subjectList[0].id
            gradeId=gradeList[0].id
            initSelectorView()
        }

        initRecyclerView()
        initTab()
        fetchData()
    }


    /**
     * 设置分类选择
     */
    private fun initSelectorView() {
        tv_province.text = provinceStr
        tv_grade.text = gradeList[0].name
        tv_semester.text = DataBeanManager.popupSemesters[semester-1].name
        tv_course.text = subjectList[0].name

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
            if (cityPopWindow==null){
                cityPopWindow=PopupCityList(this,tv_province,tv_province.width).builder()
                cityPopWindow?.setOnSelectListener { item ->
                    provinceStr = item.name
                    tv_province.text = item.name
                    pageIndex = 1
                    fetchData()
                }
            }
            else{
                cityPopWindow?.show()
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


    }

    private fun initTab(){
        itemTabTypes=DataBeanManager.textBookTypes
        tabStr=itemTabTypes[0].title
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
        tabStr = itemTabTypes[position].title
        pageIndex = 1
        fetchData()
    }

    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(DP2PX.dip2px(this,30f),DP2PX.dip2px(this,50f),DP2PX.dip2px(this,30f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = TextbookAdapter(R.layout.item_bookstore, textbooks)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            this.position=position
            showBookDetails(textbooks[position])
        }
        rv_list?.addItemDecoration(SpaceGridItemDeco(4,60))
    }

    /**
     * 展示书籍详情
     */
    private fun showBookDetails(book: TextbookBean) {
        bookDetailsDialog = DownloadTextbookDialog(this, book)
        bookDetailsDialog?.builder()
        bookDetailsDialog?.setOnClickListener {
            if (book.buyStatus == 1) {
                val localBook = TextbookGreenDaoManager.getInstance().queryTextBookByBookId(tabId,book.bookId)
                if (localBook == null) {
                    downLoadStart(book)
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
    private fun downLoadStart(book: TextbookBean): BaseDownloadTask? {
        showLoading()
        val fileName = MD5Utils.digest(book.bookId.toString())//文件名
        val zipPath = FileAddress().getPathZip(fileName)
        val download = FileBigDownManager.with().create(book.downloadUrl).setPath(zipPath)
            .startSingleTaskDownLoad(object : FileBigDownManager.SingleTaskCallBack {

                override fun progress(task: BaseDownloadTask?, soFarBytes: Long, totalBytes: Long) {
                    if (task != null && task.isRunning) {
                        runOnUiThread {
                            val s = ToolUtils.getFormatNum(soFarBytes.toDouble() / (1024 * 1024), "0.0M")+ "/"+
                                    ToolUtils.getFormatNum(totalBytes.toDouble() / (1024 * 1024), "0.0M")
                            bookDetailsDialog?.setUnClickBtn(s)
                        }
                    }
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Long, totalBytes: Long) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    book.apply {
                        loadSate = 2
                        category = tabId
                        time = System.currentTimeMillis()//下载时间用于排序
                    }
                    if (tabId<2){
                        book.bookPath = FileAddress().getPathTextBook(fileName)
                        book.bookDrawPath=FileAddress().getPathTextBookDraw(fileName)
                    }
                    else{
                        book.bookPath = FileAddress().getPathHomeworkBook(fileName)
                        book.bookDrawPath=FileAddress().getPathHomeworkBookDraw(fileName)
                    }
                    ZipUtils.unzip(zipPath, book.bookPath, object : IZipCallback {
                        override fun onFinish() {
                            TextbookGreenDaoManager.getInstance().insertOrReplaceBook(book)
                            FileUtils.deleteFile(File(zipPath))
                            EventBus.getDefault().post(TEXT_BOOK_EVENT)
                            bookDetailsDialog?.dismiss()
                            Handler().postDelayed({
                                hideLoading()
                                showToast(book.bookName+"下载成功")
                            },500)
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
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    hideLoading()
                    showToast("${book.bookName}下载失败")
                    bookDetailsDialog?.setChangeStatus()
                }
            })
        return download
    }

    override fun onDestroy() {
        super.onDestroy()
        FileDownloader.getImpl().pauseAll()
    }

    override fun fetchData() {
        textbooks.clear()
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
}