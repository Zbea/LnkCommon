package com.bll.lnkcommon.ui.activity.book

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.dialog.PreviewDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.book.Book
import com.bll.lnkcommon.mvp.model.book.BookStore
import com.bll.lnkcommon.mvp.presenter.BookStorePresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.BookStoreAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DownloadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.ToolUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.google.gson.Gson
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.ac_bookstore_type.*
import kotlinx.android.synthetic.main.ac_list_tab.rv_list
import kotlinx.android.synthetic.main.common_title.tv_subgrade
import org.greenrobot.eventbus.EventBus

/**
 * 书城分类
 */
class BookStoreTypeActivity: BaseActivity(), IContractView.IBookStoreView {

    private var type=0
    private val presenter = BookStorePresenter(this)
    private var books = mutableListOf<Book>()
    private var mAdapter: BookStoreAdapter? = null
    private var position=0
    private var popGrades = mutableListOf<PopupBean>()
    private var grade = 0

    override fun onBook(bookStore: BookStore) {
        setPageNumber(bookStore.total)
        books = bookStore.list
        mAdapter?.setNewData(books)
    }

    override fun buyBookSuccess() {
        books[position].buyStatus=1
        mAdapter?.notifyItemChanged(position)
    }

    override fun layoutId(): Int {
        return R.layout.ac_list_tab
    }

    override fun initData() {
        pageSize=12
        popGrades=DataBeanManager.popupTypeGrades
    }


    override fun initView() {
        setPageTitle("书城")
        showView(tv_subgrade)

        if (popGrades.size>0){
            grade = popGrades[0].id
            tv_subgrade?.apply {
                text = popGrades[0].name
                setOnClickListener {
                    PopupRadioList(this@BookStoreTypeActivity, popGrades, this,width, 5).builder()
                        .setOnSelectListener { item ->
                            grade = item.id
                            text = item.name
                            pageIndex=1
                            fetchData()
                        }
                }
            }
        }

        initRecyclerView()
        initTab()

        fetchData()
    }

    private fun initTab(){
        val types= DataBeanManager.bookStoreTypes
        types.forEach {
            itemTabTypes.add(ItemTypeBean().apply {
                title=it.desc
                typeId=it.type
            })
        }
        itemTabTypes[0].isCheck=true
        type=itemTabTypes[0].typeId
        mTabTypeAdapter?.setNewData(itemTabTypes)
    }

    override fun onTabClickListener(view: View, position: Int) {
        type=itemTabTypes[position].typeId
        pageIndex=1
        fetchData()
    }

    private fun initRecyclerView() {
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(this, 30f), DP2PX.dip2px(this, 25f),
            DP2PX.dip2px(this, 30f), 0
        )
        layoutParams.weight = 1f
        rv_list?.layoutParams = layoutParams

        rv_list?.apply {
            layoutManager = GridLayoutManager(this@BookStoreTypeActivity, 4)
            addItemDecoration(SpaceGridItemDeco(4, DP2PX.dip2px(this@BookStoreTypeActivity, 15f)))
        }
        mAdapter = BookStoreAdapter(R.layout.item_bookstore_buy, null).apply {
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            setOnItemClickListener { adapter, view, position ->
                val book=books[position]
                val images=if (book.previewUrl.isNullOrEmpty()){
                    mutableListOf()
                }
                else{
                    book.previewUrl.split(",")
                }
                val content="出版社："+book.version+"\n简介："+book.bookDesc
                PreviewDialog(this@BookStoreTypeActivity,book.bookName,content,images).builder()
            }
            setOnItemChildClickListener { adapter, view, position ->
                this@BookStoreTypeActivity.position=position
                val book=books[position]
                if (view.id==R.id.tv_buy){
                    if (book.buyStatus==1){
                        val localBook = BookDaoManager.getInstance().queryByBookID(book.bookId)
                        if (book.loadSate==2){
                            MethodManager.gotoBookDetails(this@BookStoreTypeActivity,1, localBook)
                            return@setOnItemChildClickListener
                        }
                        if (localBook == null) {
                            downLoadStart(book.downloadUrl,book)
                        } else {
                            book.loadSate =2
                            showToast(R.string.downloaded)
                            notifyItemChanged(position)
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
        }
    }


    //下载book
    private fun downLoadStart(url: String,book: Book){
        showLoading()
        val fileName = MD5Utils.digest(book.bookId.toString())//文件名
        val targetFileStr = FileAddress().getPathBook(fileName+ FileUtils.getUrlFormat(book.downloadUrl))
        mDownloadManager?.startSingle(url,targetFileStr, object : DownloadManager.SingleCallback {
            override fun onProgress(task: BaseDownloadTask, soFar: Long, total: Long) {
                if (task.isRunning) {
                    runOnUiThread {
                        val s = ToolUtils.getFormatNum(soFar.toDouble() / (1024 * 1024),"0.0") + "/" +
                                ToolUtils.getFormatNum(total.toDouble() / (1024 * 1024), "0.0")
                        mAdapter?.setChangeText(s,position)
                    }
                }
            }
            override fun onCompleted(task: BaseDownloadTask) {
                book.apply {
                    loadSate = 2
                    loadString=""
                    time = System.currentTimeMillis()//下载时间用于排序
                    bookPath = targetFileStr
                    bookDrawPath= FileAddress().getPathBookDraw(fileName)
                }
                mAdapter?.notifyItemChanged(position)

                BookDaoManager.getInstance().insertOrReplaceBook(book)

                EventBus.getDefault().post(Constants.BOOK_EVENT)
                showToast(book.bookName+getString(R.string.download_success))
                hideLoading()
            }
            override fun onFailed(task: BaseDownloadTask?, error: String) {
                hideLoading()
                mAdapter?.setInitText(position)
                showToast(book.bookName+getString(R.string.download_fail))
            }
        })
    }

    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        map["grade"] = grade
        map["type"] = type
        presenter.getBooks(map)
    }

    override fun onRefreshData() {
        if (mDownloadManager?.isRunning() ==false){
            fetchData()
        }
    }
}