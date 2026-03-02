package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDeco2
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class ItemSelectorDialog(context: Context, private val titleStr: String, private val items: MutableList<ItemList>) : BaseDialog(context) {

    override fun getLayoutResId(): Int = R.layout.dialog_item_select

    override fun initView(contentView: View) {
        val tvName = contentView.findViewById<TextView>(R.id.tv_name)
        tvName.text = titleStr

        val ivClose = contentView.findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener { dismiss() }

        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        rvList?.layoutManager = GridLayoutManager(context, 2)
        val mAdapter = MyAdapter(R.layout.item_select_name, items)
        rvList?.addItemDecoration(SpaceGridItemDeco2(20, DP2PX.dip2px(context,15f)))
        mAdapter.bindToRecyclerView(rvList)

        mAdapter.setOnItemClickListener { _, _, position ->
            listener?.onClick(position)
            dismiss()
        }
    }

    private var listener: OnDialogClickListener? = null
    fun interface OnDialogClickListener {
        fun onClick(pos: Int)
    }
    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    override fun builder(): ItemSelectorDialog {
        super.builder()
        return this
    }

    class MyAdapter(layoutResId: Int, data: List<ItemList>?) : BaseQuickAdapter<ItemList, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: ItemList) {
            helper.setText(R.id.tv_name, item.name)
        }
    }
}