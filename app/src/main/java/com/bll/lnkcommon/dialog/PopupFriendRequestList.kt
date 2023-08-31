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
import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.utils.DP2PX
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PopupFriendRequestList(var context: Context, var view: View,val lists:MutableList<FriendList.FriendBean>) {

    private var mPopupWindow: PopupWindow? = null

    fun builder(): PopupFriendRequestList {
        val popView = LayoutInflater.from(context).inflate(R.layout.popup_friend_request_list, null, false)
        mPopupWindow = PopupWindow(context).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // 设置PopupWindow的内容view
            contentView = popView
            isFocusable = true // 设置PopupWindow可获得焦点
            isTouchable = true // 设置PopupWindow可触摸
            isOutsideTouchable = true // 设置非PopupWindow区域可触摸
            width=DP2PX.dip2px(context,280f)
        }

        val rvList = popView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)//创建布局管理
        val mAdapter = FriendAdapter(R.layout.item_friend_request, lists)
        rvList.adapter=mAdapter
        mAdapter.bindToRecyclerView(rvList)
        mAdapter.setEmptyView(R.layout.common_empty)
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id==R.id.iv_close){
                onSelectListener?.onSelect(position,1)
                dismiss()
            }
            if (view.id==R.id.iv_agree){
                onSelectListener?.onSelect(position,2)
                dismiss()
            }
        }

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
            mPopupWindow?.showAsDropDown(view, 0, 10, Gravity.RIGHT)
        }
    }

    private var onSelectListener: OnSelectListener?=null

    fun setOnSelectListener(onSelectListener: OnSelectListener)
    {
        this.onSelectListener=onSelectListener
    }

    fun interface OnSelectListener{
        fun onSelect(position:Int,type:Int)
    }


    class FriendAdapter(layoutResId: Int, data: MutableList<FriendList.FriendBean>?) : BaseQuickAdapter<FriendList.FriendBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: FriendList.FriendBean) {
            helper.apply {
                setText(R.id.tv_name,item.nickname)
                addOnClickListener(R.id.iv_close,R.id.iv_agree)
            }
        }
    }

}