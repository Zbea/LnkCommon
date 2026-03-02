package com.bll.lnkcommon.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BasePopupWindow
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.widget.MaxRecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PopupRadioList(
    context: Context,
    private val list: MutableList<PopupBean>,
    anchorView: View,
    layoutWidth: Int = 0,
    yOffset: Int = 0
) : BasePopupWindow(context, anchorView, layoutWidth, 0, yOffset) {

    constructor(context: Context, list: MutableList<PopupBean>, view: View, yoff: Int) :
            this(context, list, view, 0, yoff)

    override fun getLayoutResId(): Int = R.layout.popup_list

    override fun initView() {

        val mAdapter = initRecyclerView(
            rvId = R.id.rv_list,
            data = list,
        ) { layoutId, data ->
            object : BaseQuickAdapter<PopupBean, BaseViewHolder>(layoutId, data) {
                override fun convert(helper: BaseViewHolder, item: PopupBean) {
                    helper.setText(R.id.tv_name, item.name)
                    helper.setVisible(R.id.iv_check, item.isCheck)
                }
            }
        }

        mAdapter.setOnItemClickListener { adapter, _, position ->
            list.forEach { it.isCheck = false }
            list[position].apply {
                isCheck = true
                index = position
            }
            adapter.notifyDataSetChanged()
            notifySelect(list[position])
        }
    }
}