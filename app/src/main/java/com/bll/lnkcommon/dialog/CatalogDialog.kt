package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.ui.adapter.BookCatalogAdapter
import com.bll.lnkcommon.utils.DP2PX

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * 目录弹窗
 * @param context 上下文
 * @param list 目录数据列表
 * @param isEdit 是否显示编辑按钮
 */
class CatalogDialog(context: Context, private val list: List<ItemList>, private val isEdit: Boolean) : BaseDialog(context) {

    private var onDialogClickListener: OnDialogClickListener? = null

    override val defaultGravity: Int = Gravity.BOTTOM or Gravity.START
    override val defaultXOffset: Float = 42f
    override val defaultYOffset: Float = 5f

    override fun getLayoutResId(): Int = R.layout.dialog_catalog

    override fun initView(contentView: View) {

        val tvEdit = contentView.findViewById<TextView>(R.id.tv_edit)
        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)

        tvEdit.visibility = if (isEdit) View.VISIBLE else View.GONE

        rvList.layoutManager = LinearLayoutManager(context)
        val mAdapter = CatalogAdapter(R.layout.item_catalog_parent, list)
        mAdapter.bindToRecyclerView(rvList)
        mAdapter.setOnItemClickListener { _, _, position ->
            dismiss()
            onDialogClickListener?.onClick(list[position].page)
        }

        mAdapter.setOnItemChildClickListener { _, _, position ->
            val item = list[position]
            // 调用输入弹窗
            InputContentDialog(context, item.name).builder().setOnDialogClickListener { newName ->
                    item.name = newName
                    mAdapter.notifyItemChanged(position)
                    onDialogClickListener?.onEdit(newName, mutableListOf(list[position].page))
                }
        }

        tvEdit.setOnClickListener {
            dismiss()
            CatalogEditDialog(context, list.last().page + 1).builder().setOnDialogClickListener { contentStr, pages ->
                    onDialogClickListener?.onEdit(contentStr, pages)
                }
        }
    }

    interface OnDialogClickListener {
        fun onClick(pageNumber: Int)
        fun onEdit(title: String, pages: List<Int>) {}
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): CatalogDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): CatalogDialog {
        super.builder()
        return this
    }

    class CatalogAdapter(layoutResId: Int, data: List<ItemList>) : BaseQuickAdapter<ItemList, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: ItemList) {
            helper.setText(R.id.tv_name, item.name)
            helper.setText(R.id.tv_page, (item.page + 1).toString())
            helper.setGone(R.id.iv_edit, item.isEdit)
            helper.addOnClickListener(R.id.iv_edit)
        }
    }
}