package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.teaching.ExamList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class ExamPresenter(view: IContractView.IExamView) : BasePresenter<IContractView.IExamView>(view) {

    fun getExams(map: HashMap<String,Any>) {
        val grade = RetrofitManager.service.getExams(map)
        doRequest(grade, object : Callback<ExamList>(view) {
            override fun failed(tBaseResult: BaseResult<ExamList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<ExamList>) {
                if (tBaseResult.data!=null)
                    view.onList(tBaseResult.data)
            }
        }, false)
    }

    fun deleteExam(map: HashMap<String,Any>) {
        val body=RequestUtils.getBody(map)
        val grade = RetrofitManager.service.deleteExams(body)
        doRequest(grade, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onDeleteSuccess()
            }
        }, true)
    }

}