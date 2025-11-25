package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.teaching.ExamRankList
import com.bll.lnkcommon.mvp.model.teaching.Score
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.BasePresenter
import com.bll.lnkcommon.net.BaseResult
import com.bll.lnkcommon.net.Callback
import com.bll.lnkcommon.net.RetrofitManager


class ScoreRankPresenter(view: IContractView.IScoreRankView) : BasePresenter<IContractView.IScoreRankView>(view) {

    fun onScore(id:Int) {
        val map=HashMap<String,Any>()
        map["id"]=id
        val grade = RetrofitManager.service.getScore(map)
        doRequest(grade, object : Callback<MutableList<Score>>(view) {
            override fun failed(tBaseResult: BaseResult<MutableList<Score>>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<MutableList<Score>>) {
                if (!tBaseResult.data.isNullOrEmpty())
                    view.onScore(tBaseResult.data)
            }
        }, true)
    }

    fun onExamScore(id:Int) {
        val map=HashMap<String,Any>()
        map["schoolExamJobId"]=id
        val grade = RetrofitManager.service.getExamScore(map)
        doRequest(grade, object : Callback<ExamRankList>(view) {
            override fun failed(tBaseResult: BaseResult<ExamRankList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<ExamRankList>) {
                if (tBaseResult.data!=null)
                    view.onExamScore(tBaseResult.data)
            }
        }, true)
    }

}