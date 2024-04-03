package com.bll.lnkcommon.ui.activity

import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.presenter.HomeworkPresenter
import com.bll.lnkcommon.mvp.presenter.ScoreRankPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.ScoreAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDecoScore
import kotlinx.android.synthetic.main.ac_score.*
import kotlinx.android.synthetic.main.common_title.*

class ScoreActivity:BaseActivity(),IContractView.IScoreRankView{

    private val mPresenter= ScoreRankPresenter(this)
    private var mAdapter: ScoreAdapter?=null
    private var scores= mutableListOf<Score>()

    override fun onScore(scores: MutableList<Score>) {
        this.scores=scores
        mAdapter?.setNewData(scores)
    }

    override fun onExamScore(list: ExamRankList) {
        for (item in list.list){
            scores.add(Score().apply {
                classId=item.classId
                className=item.className
                score=item.score
                name=item.studentName
            })
        }
        mAdapter?.setNewData(scores)
    }

    override fun layoutId(): Int {
        return R.layout.ac_score
    }

    override fun initData() {
        val type=intent.flags
        val id=intent.getIntExtra("id",0)
        if (type==1){
            mPresenter.onScore(id)
        }
        else{
            mPresenter.onExamScore(id)
        }
    }

    override fun initView() {
        setPageTitle("成绩统计")

        initRecyclerView()
    }

    private fun initRecyclerView(){
        mAdapter = ScoreAdapter(R.layout.item_score,null)
        rv_list.layoutManager = GridLayoutManager(this,2)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        rv_list.addItemDecoration(SpaceGridItemDecoScore(DP2PX.dip2px(this,40f),0))
    }

}