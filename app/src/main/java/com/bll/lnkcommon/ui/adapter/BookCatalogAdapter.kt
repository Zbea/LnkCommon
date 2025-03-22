package com.bll.lnkcommon.ui.adapter

import android.widget.LinearLayout
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.catalog.CatalogChildBean
import com.bll.lnkcommon.mvp.model.catalog.CatalogParentBean
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity

class BookCatalogAdapter(data: List<MultiItemEntity>?,private val startCount:Int) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

    init {
        addItemType(0, R.layout.item_catalog_parent)
        addItemType(1, R.layout.item_catalog_child)
    }

    override fun convert(helper: BaseViewHolder, item: MultiItemEntity?) {
        when (helper.itemViewType) {
            0 -> {
                val item= item as CatalogParentBean
                helper.setText(R.id.tv_name, item.title)
                helper.setText(R.id.tv_page, "${item.pageNumber-startCount}")
                helper.itemView.setOnClickListener { v ->
                    val pos = helper.adapterPosition
                    if (item.hasSubItem()){
                        if (item.isExpanded) {
                            collapse(pos,false)
                        } else {
                            expand(pos,false)
                        }
                    }
                    else{
                        if (listener!=null)
                            listener?.onParentClick(item.pageNumber)
                    }
                }
            }
            1-> {
                val childItem = item as CatalogChildBean
                helper.setText(R.id.tv_name, childItem.title)
                helper.setTextColor(R.id.tv_name,mContext.resources.getColor(R.color.black))
                helper.setText(R.id.tv_page,"${childItem.pageNumber-startCount}")
                helper.getView<LinearLayout>(R.id.ll_click).setOnClickListener {
                    if (listener!=null)
                        listener?.onChildClick(item.pageNumber)
                }
            }
        }

    }

    private var listener: OnCatalogClickListener? = null

    interface OnCatalogClickListener{
        fun onParentClick(page:Int)
        fun onChildClick(page:Int)
    }

    fun setOnCatalogClickListener(listener: OnCatalogClickListener?) {
        this.listener = listener
    }

}