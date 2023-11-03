package com.bll.lnkcommon.ui.activity.book

import android.os.Handler
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.BookDetailsDialog
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.presenter.BookStorePresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileDownManager
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.ac_bookstore.*
import kotlinx.android.synthetic.main.common_title.*
import java.text.DecimalFormat

/**
 * 书城
 */
class BookStoreActivity : BaseActivity(), IContractView.IBookStoreView {

    private var tabStr=""
    private var type=0
    private val presenter = BookStorePresenter(this)
    private var books = mutableListOf<Book>()
    private var mAdapter: BookAdapter? = null
    private var grade = 0
    private var subTypeStr = ""//子类
    private var subType=0
    private var bookDetailsDialog: BookDetailsDialog? = null
    private var position=0
    private var gradeList = mutableListOf<PopupBean>()
    private var subTypeList = mutableListOf<ItemList>()
    private var bookNameStr=""

    override fun onBook(bookStore: BookStore) {
        setPageNumber(bookStore.total)
        books = bookStore.list
        mAdapter?.setNewData(books)
    }

    override fun onType(bookStoreType: BookStoreType) {
        //子分类
        val types = bookStoreType.subType[tabStr]
        if (types?.size!! >0){
            subTypeList=types
            subTypeStr=types[0].desc
            subType=types[0].type
            initTab()
        }

        fetchData()
    }

    override fun buyBookSuccess() {
        books[position].buyStatus=1
        bookDetailsDialog?.setChangeStatus()
        mAdapter?.notifyItemChanged(position)
    }


    override fun layoutId(): Int {
        return R.layout.ac_bookstore
    }

    override fun initData() {
        pageSize=12
        type = intent.flags
        tabStr= DataBeanManager.bookStoreTypes[type-1].name
        gradeList=DataBeanManager.popupTypeGrades
        if (gradeList.size>0){
            grade = gradeList[0].id
            initSelectorView()
        }
        presenter.getBookType()
    }


    override fun initView() {
        setPageTitle(tabStr)
        showView(tv_grade,ll_search)

        initRecyclerView()

        et_search.addTextChangedListener {
            bookNameStr =it.toString()
            if (bookNameStr.isNotEmpty()){
                pageIndex=1
                fetchData()
            }
        }
    }

    /**
     * 设置分类选择
     */
    private fun initSelectorView() {
        tv_grade.text = gradeList[0].name
        tv_grade.setOnClickListener {
            PopupRadioList(this, gradeList, tv_grade, 5).builder()
            .setOnSelectListener { item ->
                grade = item.id
                tv_grade.text = item.name
                typeFindData()
            }
        }

    }

    /**
     * 分类查找上
     */
    private fun typeFindData(){
        pageIndex = 1
        bookNameStr=""//清除搜索标记
        fetchData()
    }


    //设置tab分类
    private fun initTab() {
        for (i in subTypeList.indices) {
            rg_group.addView(getRadioButton(i,subTypeList[i].desc,subTypeList.size-1))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, i ->
            subTypeStr = subTypeList[i].desc
            subType=subTypeList[i].type
            typeFindData()
        }
    }

    private fun initRecyclerView() {
        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = BookAdapter(R.layout.item_bookstore, books)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        rv_list?.addItemDecoration(SpaceGridItemDeco1(4, DP2PX.dip2px(this, 22f), 60))
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            this.position=position
            showBookDetails(books[position])
        }
    }


    /**
     * 展示书籍详情
     */
    private fun showBookDetails(book: Book) {
        bookDetailsDialog = BookDetailsDialog(this, book)
        bookDetailsDialog?.builder()
        bookDetailsDialog?.setOnClickListener {
            if (book.buyStatus==1){
                val localBook = BookDaoManager.getInstance().queryByBookID(1,book.bookId)
                if (localBook == null) {
                    downLoadStart(book.bodyUrl,book)
                } else {
                    book.loadSate =2
                    showToast("已下载")
                    mAdapter?.notifyItemChanged(position)
                    bookDetailsDialog?.setDissBtn()
                }
            }
            else{
                val map = HashMap<String, Any>()
                map["type"] = 3
                map["bookId"] = book.bookId
                presenter.buyBook(map)
            }
        }
    }

    //下载book
    private fun downLoadStart(url: String,book: Book): BaseDownloadTask? {
        showLoading()
        val formatStr=book.bodyUrl.substring(book.bodyUrl.lastIndexOf("."))
        val fileName = MD5Utils.digest(book.bookId.toString())//文件名
        val targetFileStr = FileAddress().getPathBook(fileName+formatStr)
        val download = FileDownManager.with(this).create(url).setPath(targetFileStr)
            .startSingleTaskDownLoad(object :
                FileDownManager.SingleTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                    if (task != null && task.isRunning) {
                        runOnUiThread {
                            val s = getFormatNum(soFarBytes.toDouble() / (1024 * 1024)) + "/" +
                                    getFormatNum(totalBytes.toDouble() / (1024 * 1024))
                            bookDetailsDialog?.setUnClickBtn(s)
                        }
                    }
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    book.apply {
                        loadSate = 2
                        category=1
                        typeId = this@BookStoreActivity.type
                        subtypeStr = ""
                        time = System.currentTimeMillis()//下载时间用于排序
                        bookPath = targetFileStr
                        bookDrawPath=FileAddress().getPathBookDraw(fileName)
                    }
                    //下载解压完成后更新存储的book
                    BookDaoManager.getInstance().insertOrReplaceBook(book)
                    //更新列表
                    mAdapter?.notifyItemChanged(position)
                    bookDetailsDialog?.dismiss()
                    Handler().postDelayed({
                        hideLoading()
                        showToast(book.bookName+"下载完成")
                    },500)
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    //删除缓存 poolmap
                    hideLoading()
                    showToast(book.bookName+"下载失败")
                    bookDetailsDialog?.setChangeStatus()
                }
            })
        return download
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
        hideKeyboard()
        books.clear()
        mAdapter?.notifyDataSetChanged()

        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        map["grade"] = grade
        map["type"] = type
        map["subType"] = subType
        if (bookNameStr.isNotEmpty())
            map["bookName"] = bookNameStr
        presenter.getBooks(map)
    }

}