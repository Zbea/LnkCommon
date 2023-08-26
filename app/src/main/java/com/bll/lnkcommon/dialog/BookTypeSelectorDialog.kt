package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.manager.BookTypeDaoManager
import com.bll.lnkcommon.mvp.model.BookTypeBean
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class BookTypeSelectorDialog(val context: Context,val okStr: String) {

    fun builder(): BookTypeSelectorDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_book_type_selector)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val tv_name = dialog.findViewById<TextView>(R.id.tv_name)
        tv_name.text=okStr
        val iv_close = dialog.findViewById<ImageView>(R.id.iv_close)

        val lists=BookTypeDaoManager.getInstance().queryAllList()
        val rv_list=dialog.findViewById<RecyclerView>(R.id.rv_list)
        rv_list?.layoutManager = GridLayoutManager(context,2)
        val mAdapter = MyAdapter(R.layout.item_book_type_name, lists)
        rv_list?.adapter = mAdapter
        rv_list?.addItemDecoration(SpaceGridItemDeco1(2, 20, 40))
        mAdapter.bindToRecyclerView(rv_list)
        mAdapter.setOnItemClickListener { adapter, view, position ->
            listener?.onClick(lists[position].name)
            dialog.dismiss()
        }

        iv_close.setOnClickListener {
            dialog.dismiss()
        }

        return this
    }

    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onClick(string: String)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    class MyAdapter(layoutResId: Int, data: List<BookTypeBean>?) : BaseQuickAdapter<BookTypeBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: BookTypeBean) {
            helper.setText(R.id.tv_name,item.name)
            helper.setChecked(R.id.cb_check,item.isCheck)
        }
    }

}