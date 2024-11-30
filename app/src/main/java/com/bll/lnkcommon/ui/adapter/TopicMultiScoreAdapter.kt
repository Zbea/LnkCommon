package com.bll.lnkcommon.ui.adapter

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.mvp.model.ExamScoreItem
import com.bll.lnkcommon.utils.ToolUtils
import com.bll.lnkcommon.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class TopicMultiScoreAdapter(layoutResId: Int, private var scoreType: Int, data: List<ExamScoreItem>?) : BaseQuickAdapter<ExamScoreItem, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: ExamScoreItem) {
        helper.setText(R.id.tv_sort, ToolUtils.numbers[item.sort+1])
        helper.setText(R.id.tv_score,if (scoreType==1) item.score.toString() else if (item.result==1)"对" else "错")
        helper.setGone(R.id.rv_list,!item.childScores.isNullOrEmpty())
        helper.setGone(R.id.iv_result,item.childScores.isNullOrEmpty())
        helper.setImageResource(R.id.iv_result,if (item.result==1) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)

        val recyclerView = helper.getView<RecyclerView>(R.id.rv_list)
        recyclerView?.layoutManager = GridLayoutManager(mContext, 2)
        val mAdapter = ChildAdapter(R.layout.item_topic_child_score, scoreType, item.childScores)
        recyclerView?.adapter = mAdapter

    }

    class ChildAdapter(layoutResId: Int, private var scoreType: Int, data: List<ExamScoreItem>?) : BaseQuickAdapter<ExamScoreItem, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: ExamScoreItem) {
            helper.apply {
                helper.setText(R.id.tv_sort, "${item.sort+1}")
                helper.setText(R.id.tv_score, if (scoreType == 1) item.score.toString() else if (item.result == 1) "对" else "错")
                helper.setImageResource(R.id.iv_result, if (item.result == 1) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)
                addOnClickListener(R.id.tv_score, R.id.iv_result)
            }
        }
    }

}
