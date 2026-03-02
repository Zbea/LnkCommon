package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.mvp.model.DiaryBean
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * 日记目录弹窗
 * @param context 上下文
 * @param list 日记数据列表
 */
class CatalogDiaryDialog(context: Context, private val list: List<DiaryBean>) : BaseDialog(context) {

    private var onDialogClickListener: OnDialogClickListener? = null

    override val defaultGravity: Int = Gravity.BOTTOM or Gravity.START
    override val defaultXOffset: Float = 42f
    override val defaultYOffset: Float = 5f

    override fun getLayoutResId(): Int = R.layout.dialog_catalog

    override fun initView(contentView: View) {

        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)
        val mAdapter = CatalogAdapter(R.layout.item_catalog_parent, list)
        mAdapter.bindToRecyclerView(rvList)

        mAdapter.setOnItemClickListener { _, _, position ->
            dismiss()
            onDialogClickListener?.onClick(position)
        }
    }

    fun interface OnDialogClickListener {
        fun onClick(position: Int)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): CatalogDiaryDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): CatalogDiaryDialog {
        super.builder()
        return this
    }

    class CatalogAdapter(layoutResId: Int, data: List<DiaryBean>) : BaseQuickAdapter<DiaryBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: DiaryBean) {
            helper.setText(R.id.tv_name, item.title)
            helper.setText(R.id.tv_page, DateUtils.longToStringDataNoYear(item.date))
        }
    }
}