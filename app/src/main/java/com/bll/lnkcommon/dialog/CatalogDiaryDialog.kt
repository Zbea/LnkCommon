package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.DiaryBean
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder


class CatalogDiaryDialog(val context: Context, val list: List<DiaryBean>) {

    fun builder(): CatalogDiaryDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_catalog)
        val window = dialog.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        val layoutParams = window.attributes
        layoutParams.gravity = Gravity.BOTTOM or  Gravity.START
        layoutParams.y=DP2PX.dip2px(context,5f)
        layoutParams.x=DP2PX.dip2px(context,42f)
        dialog.show()

        val rv_list = dialog.findViewById<RecyclerView>(R.id.rv_list)
        rv_list?.layoutManager = LinearLayoutManager(context)

        val mAdapter= CatalogAdapter(R.layout.item_catalog_parent, list)
        rv_list?.adapter = mAdapter
        mAdapter.bindToRecyclerView(rv_list)
        mAdapter.setOnItemClickListener  { adapter, view, position ->
            dialog.dismiss()
            listener?.onClick(position)
        }
        return this
    }


    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onClick(position: Int)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    class CatalogAdapter(layoutResId: Int, data: List<DiaryBean>) : BaseQuickAdapter<DiaryBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: DiaryBean) {
            helper.setText(R.id.tv_name, item.title)
            helper.setText(R.id.tv_page, DateUtils.longToStringDataNoYear(item.date))
        }

    }

}