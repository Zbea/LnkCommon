package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.ui.adapter.BookCatalogAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * 书籍目录弹窗
 * @param context 上下文
 * @param list 目录数据列表（MultiItemEntity类型）
 * @param startCount 起始计数（用于目录定位）
 */
class CatalogBookDialog(context: Context, private val list: List<MultiItemEntity>, private val startCount: Int) : BaseDialog(context) {

    private var onDialogClickListener: OnDialogClickListener? = null

    override val defaultGravity: Int = Gravity.BOTTOM or Gravity.START // 左下位置
    override val defaultXOffset: Float = 42f // X偏移42dp
    override val defaultYOffset: Float = 5f // Y偏移5dp

    override fun getLayoutResId(): Int = R.layout.dialog_catalog

    override fun initView(contentView: View) {

        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        val layoutManager = LinearLayoutManager(context)
        rvList?.layoutManager = layoutManager
        val mAdapter = BookCatalogAdapter(list, startCount)
        mAdapter.bindToRecyclerView(rvList)

        mAdapter.setOnCatalogClickListener(object : BookCatalogAdapter.OnCatalogClickListener {
            override fun onParentClick(page: Int) {
                dismiss()
                onDialogClickListener?.onClick(page)
            }
            override fun onChildClick(page: Int) {
                dismiss()
                onDialogClickListener?.onClick(page)
            }
        })
    }

    fun interface OnDialogClickListener {
        fun onClick(position: Int)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): CatalogBookDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): CatalogBookDialog {
        super.builder()
        return this
    }
}