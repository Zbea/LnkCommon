package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants.TEXT_BOOK_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.BookManageDialog
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.mvp.model.HomeworkTypeList
import com.bll.lnkcommon.mvp.presenter.MyHomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IMyHomeworkView
import com.bll.lnkcommon.ui.activity.book.BookDetailsActivity
import com.bll.lnkcommon.ui.activity.book.TextBookStoreActivity
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.SPUtil
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.common_fragment_title.*
import kotlinx.android.synthetic.main.common_radiogroup.*
import kotlinx.android.synthetic.main.fragment_app.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class TextbookFragment:BaseFragment(),IMyHomeworkView {

    private val presenter=MyHomeworkPresenter(this)
    private var mAdapter: BookAdapter? = null
    private var books = mutableListOf<Book>()
    private var textBook = ""//用来区分课本类型
    private var tabId=0
    private var position = 0

    override fun onList(homeworkTypeList: HomeworkTypeList?) {
    }
    override fun onCreateSuccess() {
        showToast("设置作业本成功")
    }
    override fun onDeleteSuccess() {
    }
    override fun onSendSuccess() {
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_textbook
    }

    override fun initView() {
        pageSize=12
        setTitle(DataBeanManager.mainListTitle[4])
        showSearch(true)

        initTab()
        initRecyclerView()

        ll_search.setOnClickListener {
            startActivity(Intent(activity, TextBookStoreActivity::class.java))
        }
    }

    override fun lazyLoad() {
        if (DataBeanManager.courses.isEmpty())
            mCommonPresenter.getCommon()
        fetchData()
    }

    private fun initTab() {
        val tabStrs = DataBeanManager.textbookType
        textBook=tabStrs[0]
        for (i in tabStrs.indices) {
            rg_group.addView(getRadioButton(i, tabStrs[i], tabStrs.size - 1))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, id ->
            tabId=id
            textBook = tabStrs[id]
            pageIndex=1
            fetchData()
        }
    }

    private fun initRecyclerView() {
        rv_list.layoutManager = GridLayoutManager(activity, 3)//创建布局管理
        mAdapter = BookAdapter(R.layout.item_textbook, null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        rv_list?.addItemDecoration(SpaceGridItemDeco1(3, DP2PX.dip2px(activity, 33f), 38))
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            val book=books[position]
            val intent = Intent(activity, BookDetailsActivity::class.java)
            intent.putExtra("book_id", book.bookId)
            intent.putExtra("book_type", book.typeId)
            startActivity(intent)
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
        val type=if (tabId==2||tabId==3) 2 else 1
        BookManageDialog(requireActivity(), book,type).builder()
            .setOnDialogClickListener (object : BookManageDialog.OnDialogClickListener {
                override fun onDelete() {
                    BookDaoManager.getInstance().deleteBook(book) //删除本地数据库
                    FileUtils.deleteFile(File(book.bookPath))//删除下载的书籍资源
                    FileUtils.deleteFile(File(book.bookDrawPath))
                    mAdapter?.remove(position)
                    EventBus.getDefault().post(TEXT_BOOK_EVENT)
                }
                override fun onSet() {
                    val studentId=SPUtil.getInt("studentId")
                    if (studentId==0){
                        showToast("请选择学生")
                        return
                    }
                    val map=HashMap<String,Any>()
                    map["name"]=book.bookName
                    map["type"]=2
                    map["childId"]=studentId
                    map["bookId"]=book.bookId
                    map["imageUrl"]=book.imageUrl
                    map["subject"]=book.subjectName
                    presenter.createHomeworkType(map)
                }
            })
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag == TEXT_BOOK_EVENT) {
            fetchData()
        }
    }

    override fun fetchData() {
        books = BookDaoManager.getInstance().queryAllTextBook( textBook, pageIndex, 9)
        val total = BookDaoManager.getInstance().queryAllTextBook( textBook)
        setPageNumber(total.size)
        mAdapter?.setNewData(books)
    }



}