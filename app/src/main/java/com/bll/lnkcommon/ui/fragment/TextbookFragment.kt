package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.Constants.TEXT_BOOK_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.mvp.model.HomeworkTypeList
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.presenter.MyHomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IMyHomeworkView
import com.bll.lnkcommon.ui.activity.drawing.BookDetailsActivity
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUploadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.SPUtil
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.common_radiogroup.*
import kotlinx.android.synthetic.main.fragment_app.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class TextbookFragment : BaseFragment(), IMyHomeworkView {

    private val presenter = MyHomeworkPresenter(this)
    private var mAdapter: BookAdapter? = null
    private var books = mutableListOf<Book>()
    private var textBook = ""//用来区分课本类型
    private var tabId = 0
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
        pageSize = 12
        setTitle(DataBeanManager.mainListTitle[5])

        initTab()
        initRecyclerView()

    }

    override fun lazyLoad() {
        if (DataBeanManager.courses.isEmpty())
            mCommonPresenter.getCommon()
        fetchData()
    }

    private fun initTab() {
        val tabStrs = DataBeanManager.textbookType
        textBook = tabStrs[0]
        for (i in tabStrs.indices) {
            rg_group.addView(getRadioButton(i, tabStrs[i], tabStrs.size - 1))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, id ->
            tabId = id
            textBook = tabStrs[id]
            pageIndex = 1
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
            val book = books[position]
            val intent = Intent(activity, BookDetailsActivity::class.java)
            intent.putExtra("book_id", book.bookId)
            intent.putExtra("book_type", book.typeId)
            customStartActivity(intent)
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
        if (tabId == 3) {
            beans.add(ItemList().apply {
                name = "删除"
                resId = R.mipmap.icon_setting_delete
            })
            beans.add(ItemList().apply {
                name = "设置作业"
                resId = R.mipmap.icon_setting_set
            })
        } else {
            beans.add(ItemList().apply {
                name = "删除"
                resId = R.mipmap.icon_setting_delete
            })
        }

        LongClickManageDialog(requireActivity(), book.bookName, beans).builder()
            .setOnDialogClickListener {
                if (it == 0) {
                    BookDaoManager.getInstance().deleteBook(book) //删除本地数据库
                    FileUtils.deleteFile(File(book.bookPath))//删除下载的书籍资源
                    FileUtils.deleteFile(File(book.bookDrawPath))
                    mAdapter?.remove(position)
                    EventBus.getDefault().post(TEXT_BOOK_EVENT)
                } else {
                    val studentId = SPUtil.getInt("studentId")
                    if (studentId == 0) {
                        showToast("请选择学生")
                        return@setOnDialogClickListener
                    }
                    val map = HashMap<String, Any>()
                    map["name"] = book.bookName
                    map["type"] = 2
                    map["childId"] = studentId
                    map["bookId"] = book.bookId
                    map["imageUrl"] = book.imageUrl
                    map["subject"] = book.subjectName
                    presenter.createHomeworkType(map)
                }
            }
    }

    /**
     * 每天上传书籍
     */
    fun upload(tokenStr: String) {
        cloudList.clear()
        val maxBooks = mutableListOf<Book>()
        val books = BookDaoManager.getInstance().queryAllTextbook()
        //遍历获取所有需要上传的书籍数目
        for (item in books) {
            if (System.currentTimeMillis() >= item.time + Constants.halfYear) {
                maxBooks.add(item)
            }
        }
        for (book in maxBooks) {
            val subTypeId = DataBeanManager.textbookType.indexOf(book.subtypeStr)
            //判读是否存在手写内容
            if (FileUtils.isExistContent(book.bookDrawPath)) {
                FileUploadManager(tokenStr).apply {
                    startUpload(book.bookDrawPath, book.bookId.toString())
                    setCallBack {
                        cloudList.add(CloudListBean().apply {
                            type = 2
                            zipUrl = book.bodyUrl
                            downloadUrl = it
                            subType = subTypeId
                            subTypeStr = book.subtypeStr
                            date = System.currentTimeMillis()
                            listJson = Gson().toJson(book)
                            bookId = book.bookId
                        })
                        if (cloudList.size == maxBooks.size)
                            mCloudUploadPresenter.upload(cloudList)
                    }
                }
            } else {
                cloudList.add(CloudListBean().apply {
                    type = 2
                    zipUrl = book.bodyUrl
                    subType = subTypeId
                    subTypeStr = book.subtypeStr
                    date = System.currentTimeMillis()
                    listJson = Gson().toJson(book)
                    bookId = book.bookId
                })
                if (cloudList.size == maxBooks.size)
                    mCloudUploadPresenter.upload(cloudList)
            }
        }
    }

    //上传完成后删除书籍
    override fun uploadSuccess(cloudIds: MutableList<Int>?) {
        super.uploadSuccess(cloudIds)
        for (item in cloudList) {
            val bookBean = BookDaoManager.getInstance().queryByBookID(0, item.bookId)
            //删除书籍
            FileUtils.deleteFile(File(bookBean.bookPath))
            FileUtils.deleteFile(File(bookBean.bookDrawPath))
            BookDaoManager.getInstance().deleteBook(bookBean)
        }
        fetchData()
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