package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.HomeworkCorrectList
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*

class HomeworkCorrectPresenter(view: IContractView.IHomeworkCorrectView):BasePresenter<IContractView.IHomeworkCorrectView>(view) {


    fun getCorrects(map: HashMap<String,Any>) {
        val grade = RetrofitManager.service.getHomeworkCorrects(map)
        doRequest(grade, object : Callback<HomeworkCorrectList>(view) {
            override fun failed(tBaseResult: BaseResult<HomeworkCorrectList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<HomeworkCorrectList>) {
                if (tBaseResult.data!=null)
                    view.onList(tBaseResult.data)
            }
        }, false)
    }

    fun deleteCorrect(map: HashMap<String,Any>) {
        val body=RequestUtils.getBody(map)
        val grade = RetrofitManager.service.deleteCorrect(body)
        doRequest(grade, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onDeleteSuccess()
            }
        }, true)
    }


    fun getToken(){
        val token = RetrofitManager.service.getQiniuToken()
        doRequest(token, object : Callback<String>(view) {
            override fun failed(tBaseResult: BaseResult<String>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<String>) {
                if (tBaseResult.data!=null)
                    view.onToken(tBaseResult.data)
            }
        }, true)
    }

    fun commitPaperStudent(map:HashMap<String,Any>){
        val body= RequestUtils.getBody(map)
        val commit = RetrofitManager.service.commitPaperStudent(body)
        doRequest(commit, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onUpdateSuccess()
            }
        }, true)
    }

}