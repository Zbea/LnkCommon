package com.bll.lnkcommon.mvp.presenter

import android.util.Pair
import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class AccountInfoPresenter(view: IContractView.IAccountInfoView) : BasePresenter<IContractView.IAccountInfoView>(view) {

    fun editName(name: String) {
        val body = RequestUtils.getBody(
            Pair.create("nickName", name)
        )
        val editName = RetrofitManager.service.editName(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onEditNameSuccess()
            }
        }, true)
    }


    fun onBindStudent(account: String) {
        val map=HashMap<String,Any>()
        map["account"]=account
        val body = RequestUtils.getBody(map)
        val editName = RetrofitManager.service.onBindStudent(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onBind()
            }
        }, true)
    }

    fun unbindStudent(id: Int) {
        val map=HashMap<String,Any>()
        map["childId"]=id
        val body = RequestUtils.getBody(map)
        val editName = RetrofitManager.service.onUnbindStudent(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onUnbind()
            }
        }, true)
    }

    fun getStudents() {
        val editName = RetrofitManager.service.onStudentList()
        doRequest(editName, object : Callback<MutableList<StudentBean>>(view) {
            override fun failed(tBaseResult: BaseResult<MutableList<StudentBean>>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<MutableList<StudentBean>>) {
                if (tBaseResult.data!=null)
                    view.onListStudent(tBaseResult.data)
            }
        }, true)
    }

}