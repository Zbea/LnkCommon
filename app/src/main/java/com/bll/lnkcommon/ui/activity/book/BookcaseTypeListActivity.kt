package com.bll.lnkcommon.ui.activity.book

import PopupClick
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants.BOOK_EVENT
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.dialog.ItemSelectorDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.ac_list_tab.*
import kotlinx.android.synthetic.main.common_title.*

/**
 * 书架分类
 */
class BookcaseTypeListActivity : BaseActivity() {

    private var mAdapter: BookAdapter? = null
    private var books = mutableListOf<Book>()
    private var typeStr = ""//当前分类
    private var pos = 0 //当前书籍位置
    private val mBookDaoManager = BookDaoManager.getInstance()
    private var bookTypes = mutableListOf<ItemTypeBean>()
    private var popupBeans = mutableListOf<PopupBean>()
    private var longBeans = mutableListOf<ItemList>()

    override fun layoutId(): Int {
        return R.layout.ac_list_tab
    }

    override fun initData() {
        pageSize = 12
        popupBeans.add(PopupBean(0, getString(R.string.type_create_str), false))
        popupBeans.add(PopupBean(1, getString(R.string.type_delete_str), false))
    }

    override fun initView() {
        setPageTitle(R.string.type_list_str)
        showView(iv_manager)

        iv_manager?.setOnClickListener {
            PopupClick(this, popupBeans, iv_manager, 5).builder().setOnSelectListener { item ->
                when (item.id) {
                    0 -> {
                        InputContentDialog(this, getString(R.string.type_create_str)).builder().setOnDialogClickListener {
                            if (ItemTypeDaoManager.getInstance().isExist(it, 2)) {
                                showToast(R.string.existed)
                                return@setOnDialogClickListener
                            }
                            val bookTypeBean = ItemTypeBean()
                            bookTypeBean.type = 2
                            bookTypeBean.date = System.currentTimeMillis()
                            bookTypeBean.title = it
                            ItemTypeDaoManager.getInstance().insertOrReplace(bookTypeBean)
                            mTabTypeAdapter?.addData(bookTypes.size-1,bookTypeBean)
                        }
                    }
                    1 -> {
                        val types = ItemTypeDaoManager.getInstance().queryAll(2)
                        val lists = mutableListOf<ItemList>()
                        for (i in types.indices) {
                            lists.add(ItemList(i, types[i].title))
                        }
                        ItemSelectorDialog(this, getString(R.string.type_delete_str), lists).builder().setOnDialogClickListener {
                            val typeNameStr = types[it].title
                            val books = mBookDaoManager.queryAllBook(typeNameStr)
                            if (books.size > 0) {
                                showToast(R.string.toast_type_exist_book)
                                return@setOnDialogClickListener
                            }
                            ItemTypeDaoManager.getInstance().deleteBean(types[it])
                            var index = 0
                            for (i in bookTypes.indices) {
                                if (typeNameStr == bookTypes[i].title) {
                                    index = i
                                }
                            }
                            mTabTypeAdapter?.remove(index)
                            if (typeStr==typeNameStr){
                                bookTypes[0].isCheck=true
                                typeStr=""
                                mTabTypeAdapter?.notifyItemChanged(0)
                                fetchData()
                            }
                        }
                    }
                }
            }
        }

        initRecycleView()
        initTab()
    }

    private fun initTab() {
        bookTypes = ItemTypeDaoManager.getInstance().queryAll(2)
        bookTypes.add(ItemTypeBean().apply {
            title = "全部"
        })
        bookTypes[pos].isCheck=true
        mTabTypeAdapter?.setNewData(bookTypes)
        fetchData()
    }

    override fun onTabClickListener(view: View, position: Int) {
        pageIndex = 1
        typeStr = bookTypes[position].title
        if (position == 0)
            typeStr = ""
        fetchData()
    }


    private fun initRecycleView() {
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(this, 28f), DP2PX.dip2px(this, 20f),
            DP2PX.dip2px(this, 28f), 0
        )
        layoutParams.weight = 1f
        rv_list.layoutParams = layoutParams

        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = BookAdapter(R.layout.item_bookstore, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            rv_list?.addItemDecoration(
                SpaceGridItemDeco1(
                    4,
                    DP2PX.dip2px(this@BookcaseTypeListActivity, 22f),
                    DP2PX.dip2px(this@BookcaseTypeListActivity, 25f)
                )
            )
            setOnItemClickListener { adapter, view, position ->
                val bookBean = books[position]
                MethodManager.gotoBookDetails(this@BookcaseTypeListActivity,1, bookBean)
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                pos = position
                onLongClick()
                true
            }
        }
    }

    //删除书架书籍
    private fun onLongClick() {
        val book = books[pos]
        longBeans.clear()
        longBeans.add(ItemList().apply {
            name = getString(R.string.delete)
            resId = R.mipmap.icon_setting_delete
        })
        if (typeStr == "") {
            longBeans.add(ItemList().apply {
                name = "分类"
                resId = R.mipmap.icon_setting_set
            })
        } else {
            longBeans.add(ItemList().apply {
                name = getString(R.string.shift_out)
                resId = R.mipmap.icon_setting_out
            })
        }

        LongClickManageDialog(this, book.bookName, longBeans).builder()
            .setOnDialogClickListener {
                if (it == 0) {
                    mAdapter?.remove(pos)
                    deleteBook(book)
                    fetchData()
                } else {
                    if (typeStr == "") {
                        val types = ItemTypeDaoManager.getInstance().queryAll(2)
                        val lists = mutableListOf<ItemList>()
                        for (i in types.indices) {
                            lists.add(ItemList(i, types[i].title))
                        }
                        ItemSelectorDialog(this, getString(R.string.type_set_str), lists).builder().setOnDialogClickListener {
                            val typeStr = types[it].title
                            book.subtypeStr = typeStr
                            mBookDaoManager.insertOrReplaceBook(book)
                            fetchData()
                        }
                    } else {
                        book.subtypeStr = ""
                        mBookDaoManager.insertOrReplaceBook(book)
                        fetchData()
                    }
                }
            }
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag == BOOK_EVENT) {
            fetchData()
        }
    }

    override fun fetchData() {
        books = mBookDaoManager.queryAllBook(typeStr, pageIndex, pageSize)
        val total = mBookDaoManager.queryAllBook(typeStr)
        setPageNumber(total.size)
        mAdapter?.setNewData(books)
    }

}