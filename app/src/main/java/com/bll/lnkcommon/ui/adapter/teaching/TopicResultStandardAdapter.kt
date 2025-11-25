package com.bll.lnkcommon.ui.adapter.teaching

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.teaching.ResultStandardItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class TopicResultStandardAdapter(layoutResId: Int, data: List<ResultStandardItem.ResultChildItem>?) : BaseQuickAdapter<ResultStandardItem.ResultChildItem, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: ResultStandardItem.ResultChildItem) {
        helper.setText(R.id.tv_score,item.sortStr)
        helper.setImageResource(R.id.iv_result,if (item.isCheck) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)
    }

}
