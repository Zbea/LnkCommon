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

class PopupUpClick(context:Context, val list:MutableList<PopupBean>,view: View, width:Int, val xoff:Int, val yoff:Int)
    :BasePopupWindow(context,view,width,xoff,yoff){

    override fun getLayoutResId(): Int {
        return R.layout.popup_list_up
    }

    override fun initView() {
        val mAdapter = initRecyclerView(
            rvId = R.id.rv_list,
            data = list,
            adapterBuilder = { layoutResId, data ->
                object : BaseQuickAdapter<PopupBean, BaseViewHolder>(layoutResId, data) {
                    override fun convert(helper: BaseViewHolder, item: PopupBean) {
                        helper.setText(R.id.tv_name, item.name)
                        helper.setImageResource(R.id.iv_check, item.resId)
                        helper.setGone(R.id.iv_check, item.resId != 0)
                    }
                }
            }
        )
        mAdapter.setOnItemClickListener { _, _, position ->
            notifySelect(list[position])
        }
    }

    override fun show() {
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val height = contentView.measuredHeight
        mPopupWindow?.showAsDropDown(anchorView, xoff, -height + yoff, Gravity.START)
    }

}