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
import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.bll.lnkcommon.widget.SpaceGridItemDecoScore
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class FriendSelectorDialog(val context: Context,val lists:MutableList<FriendList.FriendBean>) {

    fun builder(): FriendSelectorDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_book_type_selector)
        val window=dialog.window
        window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val tv_name = dialog.findViewById<TextView>(R.id.tv_name)
        tv_name.text="分享好友"
        val iv_close = dialog.findViewById<ImageView>(R.id.iv_close)

        val rv_list=dialog.findViewById<RecyclerView>(R.id.rv_list)
        rv_list?.layoutManager = GridLayoutManager(context,2)
        val mAdapter = MyAdapter(R.layout.item_friend_name, lists)
        rv_list?.adapter = mAdapter
        rv_list?.addItemDecoration(SpaceGridItemDeco1(2, 0, 40))
        mAdapter.bindToRecyclerView(rv_list)
        mAdapter.setOnItemClickListener { adapter, view, position ->
            listener?.onClick(lists[position].friendId)
            dialog.dismiss()
        }

        iv_close.setOnClickListener {
            dialog.dismiss()
        }

        return this
    }

    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onClick(id: Int)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    class MyAdapter(layoutResId: Int, data: List<FriendList.FriendBean>?) : BaseQuickAdapter<FriendList.FriendBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: FriendList.FriendBean) {
            helper.setText(R.id.tv_name,item.nickname)
        }
    }

}