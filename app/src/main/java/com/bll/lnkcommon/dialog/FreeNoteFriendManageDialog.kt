package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.FriendList.FriendBean
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class FreeNoteFriendManageDialog(val context: Context, val friends:MutableList<FriendBean>) {

    fun builder(): FreeNoteFriendManageDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_freenote_friend_select)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val iv_share = dialog.findViewById<ImageView>(R.id.iv_share)
        val iv_delete = dialog.findViewById<ImageView>(R.id.iv_delete)

        val rv_list=dialog.findViewById<RecyclerView>(R.id.rv_list)
        rv_list?.layoutManager = GridLayoutManager(context,3)
        val mAdapter = MyAdapter(R.layout.item_freenote_friend_select, friends)
        rv_list?.adapter = mAdapter
        rv_list?.addItemDecoration(SpaceGridItemDeco1(3, 0, 40))
        mAdapter.bindToRecyclerView(rv_list)
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val item=mAdapter.getItem(position)
            item?.isCheck=!item?.isCheck!!
            mAdapter.notifyItemChanged(position)
        }

        iv_share.setOnClickListener {
            val ids= mutableListOf<Int>()
            for (item in mAdapter.data){
                if (item.isCheck)
                    ids.add(item.friendId)
            }
            if (ids.size>0){
                listener?.onClick(0,ids)
                dialog.dismiss()
            }
        }

        iv_delete.setOnClickListener {
            val ids= mutableListOf<Int>()
            for (item in mAdapter.data){
                if (item.isCheck)
                    ids.add(item.friendId)
            }
            if (ids.size>0){
                listener?.onClick(1,ids)
                dialog.dismiss()
            }
        }

        return this
    }

    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onClick(type:Int,ids: List<Int>)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    class MyAdapter(layoutResId: Int, data: List<FriendBean>?) : BaseQuickAdapter<FriendBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: FriendBean) {
            helper.setText(R.id.tv_name,item.nickname)
            helper.setChecked(R.id.cb_check,item.isCheck)
        }
    }

}