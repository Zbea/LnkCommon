package com.bll.lnkcommon.ui.activity.book

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants.BOOK_EVENT
import com.bll.lnkcommon.Constants.BOOK_TYPE_EVENT
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.BookcaseDetailsDialog
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.book.Book
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.ui.adapter.BookcaseTypeAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.ac_list_tab.rv_list
import kotlinx.android.synthetic.main.ac_list_tab.rv_tab
import kotlinx.android.synthetic.main.common_title.tv_setting

/**
 * 书架分类
 */
class BookcaseTypeActivity : BaseActivity() {

    private var mAdapter: BookAdapter? = null
    private var books = mutableListOf<Book>()
    private var tabPos=0
    private val mBookDaoManager= BookDaoManager.getInstance()
    private var mTabAdapter: BookcaseTypeAdapter? = null

    override fun layoutId(): Int {
        return R.layout.ac_list_tab
    }

    override fun initData() {
        pageSize = 12

        initTab()
    }

    override fun initView() {
        setPageTitle("分类展示")
        showView(tv_setting)

        tv_setting.text = "书架明细"
        tv_setting.setOnClickListener {
            BookcaseDetailsDialog(this).builder()
        }

        initRecycleView()

        fetchData()
    }

    private fun initTab() {
        itemTabTypes = ItemTypeDaoManager.getInstance().queryAll(2)
        if (itemTabTypes.size > 0) {
            for (item in itemTabTypes) {
                item.isCheck = false
            }
            itemTabTypes[0].isCheck = true
        }

        rv_tab.layoutManager = GridLayoutManager(this, 7)//创建布局管理
        mTabAdapter = BookcaseTypeAdapter(R.layout.item_bookcase_type, itemTabTypes).apply {
            rv_tab.adapter = this
            bindToRecyclerView(rv_tab)
            setOnItemClickListener { adapter, view, position ->
                itemTabTypes[tabPos].isCheck=false
                tabPos = position
                itemTabTypes[tabPos].isCheck = true
                //修改当前分类状态
                ItemTypeDaoManager.getInstance().saveBookBean( itemTabTypes[tabPos].title, false)
                notifyDataSetChanged()
                pageIndex = 1
                fetchData()
            }
        }
    }

    override fun onTabClickListener(view: View, position: Int) {
        pageIndex = 1
        tabPos=position
        fetchData()
    }

    private fun initRecycleView(){
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(this, 30f), DP2PX.dip2px(this, 30f),
            DP2PX.dip2px(this, 30f), 0
        )
        layoutParams.weight = 1f
        rv_list.layoutParams = layoutParams

        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = BookAdapter(R.layout.item_bookstore, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            rv_list?.addItemDecoration(SpaceGridItemDeco(4,  DP2PX.dip2px(this@BookcaseTypeActivity, 30f)))
            setEmptyView(R.layout.common_empty)
            setOnItemClickListener { adapter, view, position ->
                MethodManager.gotoBookDetails(this@BookcaseTypeActivity,1,books[position])
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                CommonDialog(this@BookcaseTypeActivity).setContent(R.string.tips_is_delete).builder().setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                    override fun ok() {
                        MethodManager.deleteBook(books[position])
                    }
                })
                true
            }
        }
    }

    private fun getTypeStr():String{
        return itemTabTypes[tabPos].title
    }

    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            BOOK_EVENT->{
                fetchData()
            }
            BOOK_TYPE_EVENT -> {
                itemTabTypes = ItemTypeDaoManager.getInstance().queryAll(2)
                itemTabTypes[tabPos].isCheck = true
                mTabAdapter?.setNewData(itemTabTypes)
            }
        }
    }

    override fun fetchData() {
        books=mBookDaoManager.queryAllBook(getTypeStr(), pageIndex, pageSize)
        val total = mBookDaoManager.queryAllBook(getTypeStr())
        setPageNumber(total.size)
        mAdapter?.setNewData(books)
    }

}