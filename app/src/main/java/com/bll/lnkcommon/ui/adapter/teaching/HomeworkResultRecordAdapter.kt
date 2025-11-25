package com.bll.lnkcommon.ui.adapter.teaching

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.teaching.ResultStandardItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class HomeworkResultRecordAdapter(layoutResId: Int, data: List<ResultStandardItem>?) : BaseQuickAdapter<ResultStandardItem, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: ResultStandardItem) {
        helper.setText(R.id.tv_sort,item.title)
        helper.setText(R.id.tv_score,item.score.toString())
    }
}

