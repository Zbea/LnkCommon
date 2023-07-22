package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.SchoolBean
import com.bll.lnkcommon.mvp.model.Score
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class HomeworkPresenter(view: IContractView.IHomeworkView) : BasePresenter<IContractView.IHomeworkView>(view) {

    fun getHomeworks(map: HashMap<String,Any>) {
        val grade = RetrofitManager.service.getHomeworks(map)
        doRequest(grade, object : Callback<TeacherHomeworkList>(view) {
            override fun failed(tBaseResult: BaseResult<TeacherHomeworkList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<TeacherHomeworkList>) {
                if (tBaseResult.data!=null)
                    view.onList(tBaseResult.data)
            }
        }, true)
    }

    fun deleteHomeworks(map: HashMap<String,Any>) {
        val body=RequestUtils.getBody(map)
        val grade = RetrofitManager.service.deleteHomeworks(body)
        doRequest(grade, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onDeleteSuccess()
            }
        }, true)
    }

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

}