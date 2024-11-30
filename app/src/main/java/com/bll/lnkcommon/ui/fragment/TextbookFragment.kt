package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.Constants.TEXT_BOOK_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.ItemSelectorDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.book.Book
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.presenter.MyHomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IMyHomeworkView
import com.bll.lnkcommon.ui.activity.drawing.BookDetailsActivity
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUploadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_list_tab.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class TextbookFragment : BaseFragment(), IMyHomeworkView {

    private val presenter = MyHomeworkPresenter(this)
    private var mAdapter: BookAdapter? = null
    private var books = mutableListOf<Book>()
    private var textBook = ""//用来区分课本类型
    private var tabId = 0
    private var position = 0
    private var textTypes= mutableListOf<ItemTypeBean>()

    override fun onList(homeworkTypeList: HomeworkTypeList?) {
    }

    override fun onCreateSuccess() {
        showToast("设置作业本成功")
    }

    override fun onEditSuccess() {
    }

    override fun onDeleteSuccess() {
    }

    override fun onSendSuccess() {
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_tab
    }

    override fun initView() {
        pageSize = 12
        setTitle(DataBeanManager.mainListTitle[4])

        initTab()
        initRecyclerView()
    }

    override fun lazyLoad() {
        fetchData()
    }

    private fun initTab() {
        textTypes=DataBeanManager.textBookTypes
        textBook=textTypes[0].title
        mTabTypeAdapter?.setNewData(textTypes)
    }

    override fun onTabClickListener(view: View, position: Int) {
        tabId = position
        textBook = textTypes[position].title
        pageIndex = 1
        fetchData()
    }

    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(requireActivity(),20f), DP2PX.dip2px(requireActivity(),50f),
            DP2PX.dip2px(requireActivity(),20f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams
        rv_list.layoutManager = GridLayoutManager(activity, 3)//创建布局管理
        mAdapter = BookAdapter(R.layout.item_textbook, null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        rv_list?.addItemDecoration(SpaceGridItemDeco1(3, DP2PX.dip2px(activity, 33f), 60))
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            val book = books[position]
            if (tabId<2){
                MethodManager.gotoBookDetails(requireActivity(),2,book)
            }
            else{
                val intent = Intent(activity, BookDetailsActivity::class.java)
                intent.putExtra("book_id", book.bookId)
                intent.putExtra("book_type", book.typeId)
                customStartActivity(intent)
            }
        }
        mAdapter?.onItemLongClickListener =
            BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this.position = position
                onLongClick(books[position])
                true
            }
    }

    //长按显示课本管理
    private fun onLongClick(book: Book) {
        //题卷本可以设置为作业
        val beans = mutableListOf<ItemList>()
        beans.add(ItemList().apply {
            name = "删除"
            resId = R.mipmap.icon_setting_delete
        })
        if (tabId >1&&DataBeanManager.students.size>0) {
            beans.add(ItemList().apply {
                name = "设置作业"
                resId = R.mipmap.icon_setting_set
            })
        }

        LongClickManageDialog(requireActivity(), book.bookName, beans).builder()
            .setOnDialogClickListener {
                if (it == 0) {
                    MethodManager.deleteBook(book,0)
                } else {
                    val students=DataBeanManager.students
                    if (students.size==1){
                        val map = HashMap<String, Any>()
                        map["name"] = book.bookName
                        map["type"] = 2
                        map["childId"] = students[0].accountId
                        map["bookId"] = book.bookId
                        map["imageUrl"] = book.imageUrl
                        map["subject"] = book.subjectName
                        presenter.createHomeworkType(map)
                    }
                    else{
                        val lists= mutableListOf<ItemList>()
                        for (item in students){
                            lists.add(ItemList(item.accountId,item.nickname))
                        }
                        ItemSelectorDialog(requireActivity(),"选择学生",lists).builder().setOnDialogClickListener{pos->
                            val map = HashMap<String, Any>()
                            map["name"] = book.bookName
                            map["type"] = 2
                            map["childId"] = students[pos].accountId
                            map["bookId"] = book.bookId
                            map["imageUrl"] = book.imageUrl
                            map["subject"] = book.subjectName
                            presenter.createHomeworkType(map)
                        }
                    }
                }
            }
    }

    /**
     * 每天上传书籍
     */
    fun upload(tokenStr: String) {
        cloudList.clear()
        val books = BookDaoManager.getInstance().queryAllByHalfYear(0)
        for (book in books) {
            //判读是否存在手写内容
            if (FileUtils.isExistContent(book.bookDrawPath)) {
                FileUploadManager(tokenStr).apply {
                    startUpload(book.bookDrawPath, book.bookId.toString())
                    setCallBack {
                        cloudList.add(CloudListBean().apply {
                            type = 2
                            zipUrl = book.bodyUrl
                            downloadUrl = it
                            subTypeStr = book.subtypeStr
                            date = System.currentTimeMillis()
                            listJson = Gson().toJson(book)
                            bookId = book.bookId
                        })
                        if (cloudList.size == books.size)
                            mCloudUploadPresenter.upload(cloudList)
                    }
                }
            } else {
                cloudList.add(CloudListBean().apply {
                    type = 2
                    zipUrl = book.bodyUrl
                    subTypeStr = book.subtypeStr
                    date = System.currentTimeMillis()
                    listJson = Gson().toJson(book)
                    bookId = book.bookId
                })
                if (cloudList.size == books.size)
                    mCloudUploadPresenter.upload(cloudList)
            }
        }
    }

    //上传完成后删除书籍
    override fun uploadSuccess(cloudIds: MutableList<Int>?) {
        super.uploadSuccess(cloudIds)
        for (item in cloudList) {
            val bookBean = BookDaoManager.getInstance().queryByBookID(0, item.bookId)
            MethodManager.deleteBook(bookBean,0)
        }
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag == TEXT_BOOK_EVENT) {
            fetchData()
        }
    }

    override fun fetchData() {
        books = BookDaoManager.getInstance().queryAllTextBook(textBook, pageIndex, 9)
        val total = BookDaoManager.getInstance().queryAllTextBook(textBook)
        setPageNumber(total.size)
        mAdapter?.setNewData(books)
    }


}