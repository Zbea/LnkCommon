package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.ScoreItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class TopicScoreAdapter(layoutResId: Int, val scoreMode:Int, data: List<ScoreItem>?) : BaseQuickAdapter<ScoreItem, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: ScoreItem) {
        helper.setText(R.id.tv_sort,item.sortStr)
        helper.setText(R.id.tv_score,if (scoreMode==1)item.score.toString() else if (item.result==1)"对" else "错" )
        helper.setImageResource(R.id.iv_result,if (item.result==1) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)
        helper.setGone(R.id.iv_result,false)
        helper.addOnClickListener(R.id.tv_score,R.id.iv_result)
    }
}
