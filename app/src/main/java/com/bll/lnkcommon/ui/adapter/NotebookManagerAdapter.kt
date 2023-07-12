package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.Notebook
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class NotebookManagerAdapter(layoutResId: Int, data: List<Notebook>?) : BaseQuickAdapter<Notebook, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: Notebook) {
        helper.setText(R.id.tv_name,item.title)
        helper.addOnClickListener(R.id.iv_edit)
        helper.addOnClickListener(R.id.iv_delete)
        helper.addOnClickListener(R.id.iv_top)
    }

}
