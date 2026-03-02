package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BasePopupWindow
import com.bll.lnkcommon.mvp.model.catalog.CatalogChildBean
import com.bll.lnkcommon.mvp.model.catalog.CatalogParentBean
import com.bll.lnkcommon.utils.DP2PX
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * 操作指南目录弹窗
 * @param context 上下文
 * @param anchorView 锚点View，弹窗依附显示的View
 */
class PopupOperatingGuideCatalog(
    context: Context,
    anchorView: View
) : BasePopupWindow(
    context = context,
    anchorView = anchorView,
    layoutWidth = DP2PX.dip2px(context,320f), // 固定宽度320dp
) {

    override fun getLayoutResId(): Int = R.layout.popup_operating_guide_catalog

    override fun initView() {
        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)
        val mAdapter = CatalogAdapter(DataBeanManager.operatingGuideInfo())
        mAdapter.bindToRecyclerView(rvList)
        mAdapter.setOnCatalogClickListener { position, page ->
            onSelectListener?.onClick(position, page)
            dismiss()
        }
    }

    // 重写show方法
    override fun show() {
        if (mPopupWindow != null) {
            mPopupWindow?.showAsDropDown(anchorView, 0, 5,Gravity.END)
        }
    }

    private var onSelectListener: OnSelectListener? = null

    fun setOnSelectListener(onSelectListener: OnSelectListener) {
        this.onSelectListener = onSelectListener
    }

    fun interface OnSelectListener {
        fun onClick(position: Int, page: Int)
    }

    // ===================== 目录适配器 =====================
    class CatalogAdapter(data: List<MultiItemEntity>?) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
        init {
            // 注册不同的item类型
            addItemType(0, R.layout.item_catalog_parent)
            addItemType(1, R.layout.item_catalog_child)
        }

        override fun convert(helper: BaseViewHolder, multiItemEntity: MultiItemEntity?) {
            when (helper.itemViewType) {
                // 父项（折叠/展开）
                0 -> {
                    val item = multiItemEntity as CatalogParentBean
                    helper.setText(R.id.tv_name, item.title)
                    helper.itemView.setOnClickListener {
                        val pos = helper.adapterPosition
                        if (item.isExpanded) {
                            collapse(pos, false)
                        } else {
                            expand(pos, false)
                        }
                    }
                }
                // 子项（可点击）
                1 -> {
                    val childItem = multiItemEntity as CatalogChildBean
                    helper.setText(R.id.tv_name, "       " + childItem.title)
                    helper.setTextColor(
                        R.id.tv_name,
                        mContext.resources.getColor(R.color.black)
                    )
                    helper.setText(R.id.tv_page, "${childItem.pageNumber}")
                    helper.getView<LinearLayout>(R.id.ll_click).setOnClickListener {
                        listener?.onClick(childItem.parentPosition, childItem.pageNumber)
                    }
                }
            }
        }

        // 子项点击监听
        private var listener: OnCatalogClickListener? = null

        fun interface OnCatalogClickListener {
            fun onClick(position: Int, page: Int)
        }

        fun setOnCatalogClickListener(listener: OnCatalogClickListener?) {
            this.listener = listener
        }
    }
}