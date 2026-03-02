package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.FriendList.FriendBean
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.bll.lnkcommon.base.BaseDialog

/**
 * 自由笔记好友管理弹窗：继承BaseDialog
 * @param context 上下文
 * @param friends 好友列表数据
 */
class FreeNoteFriendManageDialog(context: Context, private val friends: MutableList<FriendBean>) : BaseDialog(context) {

    private var onDialogClickListener: OnDialogClickListener? = null

    override fun getLayoutResId(): Int = R.layout.dialog_freenote_friend_select

    override fun initView(contentView: View) {

        val ivShare = contentView.findViewById<ImageView>(R.id.iv_share)
        val ivDelete = contentView.findViewById<ImageView>(R.id.iv_delete)
        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)

        rvList.layoutManager = GridLayoutManager(context, 3)
        val mAdapter = MyAdapter(R.layout.item_freenote_friend_select, friends)
        rvList.addItemDecoration(SpaceGridItemDeco1(3, 0, 30))
        mAdapter.bindToRecyclerView(rvList)

        mAdapter.setOnItemClickListener { _, _, position ->
            val item = mAdapter.getItem(position) ?: return@setOnItemClickListener
            item.isCheck = !item.isCheck
            mAdapter.notifyItemChanged(position)
        }

        ivShare.setOnClickListener {
            val ids = mutableListOf<Int>()
            mAdapter.data.filter { it.isCheck }.forEach { ids.add(it.friendId) }
            if (ids.isNotEmpty()) {
                onDialogClickListener?.onClick(0, ids)
                dismiss()
            }
        }

        ivDelete.setOnClickListener {
            val ids = mutableListOf<Int>()
            mAdapter.data.filter { it.isCheck }.forEach { ids.add(it.friendId) }
            if (ids.isNotEmpty()) {
                onDialogClickListener?.onClick(1, ids)
                dismiss()
            }
        }
    }

    fun interface OnDialogClickListener {
        fun onClick(type: Int, ids: List<Int>)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): FreeNoteFriendManageDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): FreeNoteFriendManageDialog {
        super.builder()
        return this
    }

    class MyAdapter(layoutResId: Int, data: List<FriendBean>?) : BaseQuickAdapter<FriendBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: FriendBean) {
            helper.setText(R.id.tv_name, item.nickname)
            helper.setChecked(R.id.cb_check, item.isCheck)
        }
    }
}