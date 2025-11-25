package com.bll.lnkcommon.ui.adapter.teaching

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.teaching.Score
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class ScoreAdapter(layoutResId: Int, data: List<Score>?) : BaseQuickAdapter<Score, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: Score) {
        helper.setText(R.id.tv_name,item.name)
        helper.setText(R.id.tv_score,item.score.toString())
        helper.setText(R.id.tv_rank,(helper.adapterPosition+1).toString())
        helper.setText(R.id.tv_class_name,item.className)
    }

}
