package com.bll.lnkcommon.ui.adapter

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.ScoreItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class TopicMultistageScoreAdapter(layoutResId: Int, private val scoreMode:Int,  data: List<ScoreItem>?) : BaseQuickAdapter<ScoreItem, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: ScoreItem) {
        helper.setText(R.id.tv_sort,item.sortStr)
        helper.setText(R.id.tv_score,item.score.toString())
        helper.setImageResource(R.id.iv_result,if (item.result==1) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)
        helper.setGone(R.id.rv_list,!item.childScores.isNullOrEmpty())
        helper.setGone(R.id.iv_result,false)

        val recyclerView=helper.getView<RecyclerView>(R.id.rv_list)
        recyclerView?.layoutManager = LinearLayoutManager(mContext)
        val mAdapter = TopicTwoScoreAdapter(R.layout.item_topic_multi_score,scoreMode, item.childScores)
        recyclerView?.adapter = mAdapter

        helper.addOnClickListener(R.id.tv_score,R.id.iv_result)
    }

    class TopicTwoScoreAdapter(layoutResId: Int, private val scoreMode:Int, data: List<ScoreItem>?) : BaseQuickAdapter<ScoreItem, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: ScoreItem) {
            helper.setText(R.id.tv_sort,item.sortStr)
            helper.setText(R.id.tv_score,item.score.toString())
            helper.setImageResource(R.id.iv_result,if (item.result==1) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)
            helper.setGone(R.id.rv_list,!item.childScores.isNullOrEmpty())
            helper.setGone(R.id.iv_result,false)

            val recyclerView=helper.getView<RecyclerView>(R.id.rv_list)
            recyclerView?.layoutManager = GridLayoutManager(mContext,3)
            val mAdapter = ChildAdapter(R.layout.item_topic_score,scoreMode, item.childScores)
            recyclerView?.adapter = mAdapter

            helper.addOnClickListener(R.id.tv_score,R.id.iv_result)
        }

        class ChildAdapter(layoutResId: Int, private val scoreMode:Int, data: List<ScoreItem>?) : BaseQuickAdapter<ScoreItem, BaseViewHolder>(layoutResId, data) {
            override fun convert(helper: BaseViewHolder, item: ScoreItem) {
                helper.apply {
                    helper.setText(R.id.tv_sort,item.sortStr)
                    helper.setText(R.id.tv_score,if (scoreMode==1) item.score.toString() else if (item.result==1)"对" else "错")
                    helper.setImageResource(R.id.iv_result,if (item.result==1) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)
                    helper.setGone(R.id.iv_result,false)

                    addOnClickListener(R.id.tv_score,R.id.iv_result)
                }
            }
        }
    }
}
