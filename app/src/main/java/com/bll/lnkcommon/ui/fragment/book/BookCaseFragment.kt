package com.bll.lnkcommon.ui.fragment.book

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants.BOOK_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.book.Book
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.fragment_list_content_tab.rv_list

class BookCaseFragment: BaseFragment() {

    private var mAdapter: BookAdapter? = null
    private var books = mutableListOf<Book>()
    private var type=0

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_content_tab
    }

    override fun initView() {
        pageSize = 12

        itemTabTypes.add(ItemTypeBean().apply {
            title="全部"
            typeId=0
            isCheck=true
        })
        val types= DataBeanManager.bookStoreTypes
        types.forEach {
            itemTabTypes.add(ItemTypeBean().apply {
                title=it.desc
                typeId=it.type
            })
        }

        mTabTypeAdapter?.setNewData(itemTabTypes)

        initRecyclerView()
    }

    override fun lazyLoad() {
        fetchData()
    }

    override fun onTabClickListener(view: View, position: Int) {
        type=itemTabTypes[position].typeId
        fetchData()
    }


    private fun initRecyclerView(){
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(requireActivity(), 15f), DP2PX.dip2px(requireActivity(), 30f),
            DP2PX.dip2px(requireActivity(), 15f), 0
        )
        layoutParams.weight = 1f
        rv_list?.layoutParams = layoutParams

        rv_list?.layoutManager = GridLayoutManager(requireActivity(), 4)//创建布局管理
        mAdapter = BookAdapter(R.layout.item_bookstore, null).apply {
            bindToRecyclerView(rv_list)
            rv_list?.addItemDecoration(SpaceGridItemDeco(4, DP2PX.dip2px(requireActivity(), 25f)))
            setEmptyView(R.layout.common_empty)
            setOnItemClickListener { adapter, view, position ->
                MethodManager.gotoBookDetails(requireActivity(),1, books[position])
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                CommonDialog(requireActivity()).setContent(R.string.tips_is_delete).builder().setOnDialogClickListener(object : CommonDialog.OnDialogClickListener {
                    override fun ok() {
                        MethodManager.deleteBook(books[position])
                    }
                })
                true
            }
        }
    }

    override fun fetchData() {
        val bookDaoManager=BookDaoManager.getInstance()
        books.clear()
        var total=0
        if (type==0){
            books = bookDaoManager.queryAllBook(pageIndex, pageSize)
            total = bookDaoManager.queryAllBook().size
        }
        else{
            books = bookDaoManager.queryAllBook(type,pageIndex, pageSize)
            total = bookDaoManager.queryAllBook(type).size
        }
        setPageNumber(total)
        mAdapter?.setNewData(books)
    }


    override fun onRefreshData() {
        fetchData()
    }

}