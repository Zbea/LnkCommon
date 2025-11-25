package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.ItemSelectorDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.TextbookGreenDaoManager
import com.bll.lnkcommon.mvp.model.book.TextbookBean
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.presenter.MyHomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IMyHomeworkView
import com.bll.lnkcommon.ui.activity.book.TextBookDetailsActivity
import com.bll.lnkcommon.ui.adapter.TextbookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUploadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_list_tab.*

class TextbookFragment : BaseFragment(), IMyHomeworkView {

    private val presenter = MyHomeworkPresenter(this)
    private var mAdapter: TextbookAdapter? = null
    private var textbooks = mutableListOf<TextbookBean>()
    private var tabId = 0
    private var position = 0
    private var textTypes= mutableListOf<ItemTypeBean>()

    override fun onCreateSuccess() {
        showToast("设置作业本成功")
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_tab
    }

    override fun initView() {
        pageSize = 9
        setTitle(DataBeanManager.mainListTitle[5])

        initTab()
        initRecyclerView()
    }

    override fun lazyLoad() {
        fetchData()
    }

    private fun initTab() {
        textTypes=DataBeanManager.textBookTypes
        mTabTypeAdapter?.setNewData(textTypes)
    }

    override fun onTabClickListener(view: View, position: Int) {
        tabId = position
        pageIndex = 1
        fetchData()
    }

    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(requireActivity(),20f), DP2PX.dip2px(requireActivity(),40f),
            DP2PX.dip2px(requireActivity(),20f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams
        rv_list.layoutManager = GridLayoutManager(activity, 3)//创建布局管理
        mAdapter = TextbookAdapter(R.layout.item_textbook, null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        rv_list?.addItemDecoration(SpaceGridItemDeco(3, 40))
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            val book = textbooks[position]
            val intent = Intent(activity, TextBookDetailsActivity::class.java)
            intent.putExtra("book_id", book.bookId)
            intent.putExtra("book_type", book.category)
            customStartActivity(intent)
        }
        mAdapter?.onItemLongClickListener =
            BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this.position = position
                onLongClick(textbooks[position])
                true
            }
    }

    //长按显示课本管理
    private fun onLongClick(book: TextbookBean) {
        val beans = mutableListOf<ItemList>()
        beans.add(ItemList().apply {
            name = "删除"
            resId = R.mipmap.icon_setting_delete
        })
//        if (tabId >1&&DataBeanManager.students.size>0) {
//            beans.add(ItemList().apply {
//                name = "设置作业"
//                resId = R.mipmap.icon_setting_set
//            })
//        }

        LongClickManageDialog(requireActivity(), book.bookName, beans).builder()
            .setOnDialogClickListener {
                if (it == 0) {
                    MethodManager.deleteTextbook(book)
                } else {
                    val students=DataBeanManager.students
                    if (students.size==1){
                        val map = HashMap<String, Any>()
                        map["name"] = book.bookName
                        map["type"] = 2
                        map["childId"] = students[0].accountId
                        map["bookId"] = book.bookId
                        map["imageUrl"] = book.imageUrl
                        map["subject"] = book.subject
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
                            map["subject"] = book.subject
                            presenter.createHomeworkType(map)
                        }
                    }
                }
            }
    }

    override fun fetchData() {
        textbooks = TextbookGreenDaoManager.getInstance().queryAllTextBook(tabId, pageIndex, 9)
        val total = TextbookGreenDaoManager.getInstance().queryAllTextBook(tabId)
        setPageNumber(total.size)
        mAdapter?.setNewData(textbooks)
    }

    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            Constants.AUTO_REFRESH_EVENT->{
                if (MethodManager.isLogin())
                    mQiniuPresenter.getToken()
            }
            Constants.TEXT_BOOK_EVENT->{
                fetchData()
            }
        }
    }

    /**
     * 每天上传书籍
     */
    override fun onUpload(token: String){
        cloudList.clear()
        val books = TextbookGreenDaoManager.getInstance().queryTextBookByHalfYear()
        for (book in books) {
            //判读是否存在手写内容
            if (FileUtils.isExistContent(book.bookDrawPath)) {
                FileUploadManager(token).apply {
                    setCallBack(object : FileUploadManager.UploadCallBack {
                        override fun onUploadSuccess(url: String) {
                            cloudList.add(CloudListBean().apply {
                                type = 2
                                zipUrl = book.downloadUrl
                                downloadUrl = url
                                subTypeStr = DataBeanManager.textBookTypes[book.category].title
                                date = System.currentTimeMillis()
                                listJson = Gson().toJson(book)
                                bookId = book.bookId
                                bookTypeId=book.category
                            })
                            if (cloudList.size == books.size)
                                mCloudUploadPresenter.upload(cloudList)
                        }
                        override fun onUploadFail() {
                        }
                    })
                    startZipUpload(book.bookDrawPath, book.bookId.toString())
                }
            } else {
                cloudList.add(CloudListBean().apply {
                    type = 2
                    zipUrl = book.downloadUrl
                    subTypeStr = DataBeanManager.textBookTypes[book.category].title
                    date = System.currentTimeMillis()
                    listJson = Gson().toJson(book)
                    bookId = book.bookId
                    bookTypeId=book.category
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
            val bookBean = TextbookGreenDaoManager.getInstance().queryTextBookByBookId(item.bookTypeId, item.bookId)
            MethodManager.deleteTextbook(bookBean)
        }
        fetchData()
    }
}