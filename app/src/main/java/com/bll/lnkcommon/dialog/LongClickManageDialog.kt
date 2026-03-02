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
import com.bll.lnkcommon.widget.SpaceGridItemDeco2
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class LongClickManageDialog(context: Context, private val name: String, private val lists: MutableList<ItemList>) : BaseDialog(context) {

    override fun getLayoutResId(): Int = R.layout.dialog_item_select

    override fun initView(contentView: View) {
        val tvName = contentView.findViewById<TextView>(R.id.tv_name)
        tvName.text = name

        val ivClose = contentView.findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener { dismiss() }

        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        rvList?.layoutManager = GridLayoutManager(context, 2)
        val mAdapter = MyAdapter(R.layout.item_long_click, lists)
        rvList?.addItemDecoration(SpaceGridItemDeco2(20, 30))
        mAdapter.bindToRecyclerView(rvList)

        mAdapter.setOnItemClickListener { _, _, position ->
            onClickListener?.onClick(position)
            dismiss()
        }
    }

    private var onClickListener: OnDialogClickListener? = null
    fun interface OnDialogClickListener {
        fun onClick(position: Int)
    }
    fun setOnDialogClickListener(onClickListener: OnDialogClickListener?) {
        this.onClickListener = onClickListener
    }

    override fun builder(): LongClickManageDialog {
        super.builder()
        return this
    }

    class MyAdapter(layoutResId: Int, data: List<ItemList>?) : BaseQuickAdapter<ItemList, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: ItemList) {
            helper.setText(R.id.tv_name, item.name)
            helper.setImageResource(R.id.iv_image, item.resId)
        }
    }
}