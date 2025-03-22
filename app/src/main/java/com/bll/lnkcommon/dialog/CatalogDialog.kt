package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.ui.adapter.BookCatalogAdapter
import com.bll.lnkcommon.utils.DP2PX

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity

class CatalogDialog(val context: Context, val list: List<ItemList>, private val isEdit:Boolean) {

    fun builder(): CatalogDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_catalog)
        val window = dialog.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        val layoutParams = window.attributes
        layoutParams.gravity = Gravity.BOTTOM or  Gravity.START
        layoutParams.y=DP2PX.dip2px(context,5f)
        layoutParams.x=DP2PX.dip2px(context,42f)
        dialog.show()

        val tv_edit = dialog.findViewById<TextView>(R.id.tv_edit)
        if (isEdit)
            tv_edit.visibility= View.VISIBLE

        val rv_list = dialog.findViewById<RecyclerView>(R.id.rv_list)
        rv_list?.layoutManager = LinearLayoutManager(context)
        val mAdapter= CatalogAdapter(R.layout.item_catalog_parent, list)
        rv_list?.adapter = mAdapter
        mAdapter.bindToRecyclerView(rv_list)
        mAdapter.setOnItemClickListener  { adapter, view, position ->
            dialog.dismiss()
            if (listener!=null)
                listener?.onClick(list[position].page)
        }
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item=list[position]
            InputContentDialog(context, item.name).builder().setOnDialogClickListener{
                item.name=it
                mAdapter.notifyItemChanged(position)
                listener?.onEdit(it, mutableListOf(list[position].page))
            }
        }
        tv_edit.setOnClickListener {
            dialog.dismiss()
            CatalogEditDialog(context,list.last().page+1).builder().setOnDialogClickListener{
                    contentStr,pages->
                listener?.onEdit(contentStr,pages)
            }
        }
        return this
    }

    private var listener: OnDialogClickListener? = null

    interface OnDialogClickListener {
        fun onClick(pageNumber: Int)
        fun onEdit(title:String,pages:List<Int>){}
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    class CatalogAdapter(layoutResId: Int, data: List<ItemList>) : BaseQuickAdapter<ItemList, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: ItemList) {
            helper.setText(R.id.tv_name, item.name)
            helper.setText(R.id.tv_page, (item.page+1).toString())
            helper.setGone(R.id.iv_edit,item.isEdit)
            helper.addOnClickListener(R.id.iv_edit)
        }

    }

}