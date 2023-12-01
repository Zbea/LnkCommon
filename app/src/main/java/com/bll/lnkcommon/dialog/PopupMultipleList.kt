package com.bll.lnkcommon.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.PopupBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * 多选弹框
 */
class PopupMultipleList(val context:Context, val list:MutableList<PopupBean>, val view: View, val width:Int, val yoff:Int) {

    private var mPopupWindow:PopupWindow?=null
    private var xoff=0

    constructor(context: Context, list: MutableList<PopupBean>, view: View, yoff: Int):this(context, list, view, 0,yoff)

    fun builder(): PopupMultipleList?{
        val popView = LayoutInflater.from(context).inflate(R.layout.popup_list, null, false)
        mPopupWindow = PopupWindow(context).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // 设置PopupWindow的内容view
            contentView=popView
            isFocusable=true // 设置PopupWindow可获得焦点
            isTouchable=true // 设置PopupWindow可触摸
            isOutsideTouchable=true // 设置非PopupWindow区域可触摸
            isClippingEnabled = false
            if (this@PopupMultipleList.width!=0){
                width=this@PopupMultipleList.width
            }
        }

        val rvList=popView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)//创建布局管理
        val mAdapter = MAdapter(R.layout.item_popwindow_list, list)
        rvList.adapter = mAdapter
        mAdapter.bindToRecyclerView(rvList)
        mAdapter.setOnItemClickListener { adapter, view, position ->
            list[position].isCheck=!list[position].isCheck
            mAdapter.notifyDataSetChanged()
        }

        mPopupWindow?.setOnDismissListener {
            val checkList= mutableListOf<PopupBean>()
            for (item in list){
                if (item.isCheck)
                    checkList.add(item)
            }
            if (checkList.size>0)
                onSelectListener?.onSelect(checkList)
        }

        popView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        xoff = mPopupWindow?.contentView?.measuredWidth!!

        show()
        return this
    }

    fun dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow?.dismiss()
        }
    }

    fun show() {
        if (mPopupWindow != null) {
            mPopupWindow?.showAsDropDown(view,if (width!=0)0 else -xoff, yoff,Gravity.RIGHT);
        }
    }

   private var onSelectListener: OnSelectListener?=null

    fun setOnSelectListener(onSelectListener: OnSelectListener)
    {
        this.onSelectListener=onSelectListener
    }

    fun interface OnSelectListener{
        fun onSelect(items: List<PopupBean>)
    }


    private class MAdapter(layoutResId: Int, data: List<PopupBean>?) : BaseQuickAdapter<PopupBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: PopupBean) {

            helper.setText(R.id.tv_name,item.name)
            helper.setVisible(R.id.iv_check,item.isCheck)

        }

    }

}