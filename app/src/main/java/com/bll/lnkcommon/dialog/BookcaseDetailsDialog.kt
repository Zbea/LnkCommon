package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.book.Book
import com.bll.lnkcommon.mvp.model.ItemDetailsBean
import com.bll.lnkcommon.widget.FlowLayoutManager
import com.bll.lnkcommon.widget.MaxRecyclerView
import com.bll.lnkcommon.widget.SpaceItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class BookcaseDetailsDialog(val context: Context) {

    fun builder(): BookcaseDetailsDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_bookcase_list)
        val window= dialog.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val total=BookDaoManager.getInstance().queryAllBook().size

        val tv_total=dialog.findViewById<TextView>(R.id.tv_total)
        tv_total.text="总计：${total}本"

        val items= mutableListOf<ItemDetailsBean>()
        val item =ItemDetailsBean()
        item.typeStr="全部"
        val books= BookDaoManager.getInstance().queryAllBook("")
        if (books.isNotEmpty()){
            item.num=books.size
            item.books=books
            items.add(item)
        }

        val itemTypes= ItemTypeDaoManager.getInstance().queryAll(2)
        for (itemTypeBean in itemTypes){
            val bookcaseDetailsBean =ItemDetailsBean()
            bookcaseDetailsBean.typeStr=itemTypeBean.title
            val books= BookDaoManager.getInstance().queryAllBook(itemTypeBean.title)
            if (books.isNotEmpty()){
                bookcaseDetailsBean.num=books.size
                bookcaseDetailsBean.books=books
                items.add(bookcaseDetailsBean)
            }
        }

        val rv_list=dialog.findViewById<MaxRecyclerView>(R.id.rv_list)
        rv_list?.layoutManager = LinearLayoutManager(context)
        val mAdapter = BookcaseDetailsAdapter(R.layout.item_bookcase_list, items)
        rv_list?.adapter = mAdapter
        mAdapter.bindToRecyclerView(rv_list)
        rv_list?.addItemDecoration(SpaceItemDeco(30))
        mAdapter.setOnChildClickListener{
            dialog.dismiss()
            MethodManager.gotoBookDetails(context,1,it)
        }

        return this
    }


    class BookcaseDetailsAdapter(layoutResId: Int, data: List<ItemDetailsBean>?) : BaseQuickAdapter<ItemDetailsBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: ItemDetailsBean) {
            helper.setText(R.id.tv_book_type,item.typeStr)
            helper.setText(R.id.tv_book_num,"(${item.num}本)")

            val recyclerView = helper.getView<RecyclerView>(R.id.rv_list)
            recyclerView?.layoutManager = FlowLayoutManager()
            val mAdapter = ChildAdapter(R.layout.item_bookcase_name,item.books)
            recyclerView?.adapter = mAdapter
            mAdapter.setOnItemClickListener { adapter, view, position ->
                listener?.onClick(item.books[position])
            }
        }

        class ChildAdapter(layoutResId: Int,  data: List<Book>?) : BaseQuickAdapter<Book, BaseViewHolder>(layoutResId, data) {
            override fun convert(helper: BaseViewHolder, item: Book) {
                helper.apply {
                    helper.setText(R.id.tv_name, item.bookName)
                }
            }
        }

        private var listener: OnChildClickListener? = null

        fun interface OnChildClickListener {
            fun onClick(book: Book)
        }

        fun setOnChildClickListener(listener: OnChildClickListener?) {
            this.listener = listener
        }

    }


}