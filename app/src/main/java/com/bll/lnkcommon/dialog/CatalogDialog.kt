package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.ui.adapter.BookCatalogAdapter
import com.bll.lnkcommon.utils.DP2PX

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity

class CatalogDialog(val context: Context, val list: List<Any>,val type:Int=0,val startCount:Int) {

    private var dialog:Dialog?=null

    constructor(context: Context, list: List<Any>):this(context, list, 0, 0)

    fun builder(): CatalogDialog {

        dialog = Dialog(context)
        dialog?.setContentView(R.layout.dialog_catalog)
        val window = dialog?.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        val layoutParams = window.attributes
        layoutParams.gravity = Gravity.BOTTOM or  Gravity.START
        layoutParams.y=DP2PX.dip2px(context,10f)
        layoutParams.x=DP2PX.dip2px(context,42f)
        dialog?.show()

        val rv_list = dialog?.findViewById<RecyclerView>(R.id.rv_list)

        rv_list?.layoutManager = LinearLayoutManager(context)

        if (type==0){
            val mAdapter= CatalogAdapter(R.layout.item_catalog_parent, list as List<ItemList>)
            rv_list?.adapter = mAdapter
            mAdapter.bindToRecyclerView(rv_list)
            mAdapter.setOnItemClickListener  { adapter, view, position ->
                dismiss()
                if (listener!=null)
                    listener?.onClick(position)
            }
        }
        else{
            val mAdapter = BookCatalogAdapter(list as List<MultiItemEntity>,startCount)
            rv_list?.adapter = mAdapter
            mAdapter.bindToRecyclerView(rv_list)
            mAdapter.setOnCatalogClickListener(object : BookCatalogAdapter.OnCatalogClickListener {
                override fun onParentClick(page: Int) {
                    dismiss()
                    if (listener!=null)
                        listener?.onClick(page)
                }
                override fun onChildClick(page: Int) {
                    dismiss()
                    if (listener!=null)
                        listener?.onClick(page)
                }
            })
        }
        return this
    }

    fun dismiss(){
        if(dialog!=null)
            dialog?.dismiss()
    }

    fun show(){
        if(dialog!=null)
            dialog?.show()
    }

    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onClick(position: Int)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    class CatalogAdapter(layoutResId: Int, data: List<ItemList>) : BaseQuickAdapter<ItemList, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: ItemList) {
            helper.setText(R.id.tv_name, item.name)
            helper.setText(R.id.tv_page, (item.page+1).toString())
        }

    }

}