package com.bll.lnkcommon.mvp.presenter

import android.util.Log
import com.bll.lnkcommon.mvp.model.HomeworkTypeList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class MyHomeworkPresenter(view: IContractView.IMyHomeworkView) : BasePresenter<IContractView.IMyHomeworkView>(view) {

    fun getHomeworks(map: HashMap<String,Any>) {
        val grade = RetrofitManager.service.getHomeworkTypes(map)
        doRequest(grade, object : Callback<HomeworkTypeList>(view) {
            override fun failed(tBaseResult: BaseResult<HomeworkTypeList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<HomeworkTypeList>) {
                if (tBaseResult.data!=null)
                    view.onList(tBaseResult.data)
            }
        }, false)
    }

    fun createHomeworkType(map: HashMap<String,Any>) {
        val body=RequestUtils.getBody(map)
        val grade = RetrofitManager.service.createHomeworkType(body)
        doRequest(grade, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onCreateSuccess()
            }
        }, true)
    }

    fun deleteHomeworkType(map: HashMap<String,Any>) {
        val body=RequestUtils.getBody(map)
        val grade = RetrofitManager.service.deleteHomeworkType(body)
        doRequest(grade, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onDeleteSuccess()
            }
        }, true)
    }

    fun sendHomework(map: HashMap<String,Any>) {
        val body=RequestUtils.getBody(map)
        val grade = RetrofitManager.service.sendHomework(body)
        doRequest(grade, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onSendSuccess()
            }
        }, true)
    }

}