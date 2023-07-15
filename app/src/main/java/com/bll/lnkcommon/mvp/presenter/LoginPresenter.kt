package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class LoginPresenter(view: IContractView.ILoginView) : BasePresenter<IContractView.ILoginView>(view) {

    fun login(map: HashMap<String,Any>) {

        val body = RequestUtils.getBody(map)

        val login = RetrofitManager.service.login(body)
        doRequest(login, object : Callback<User>(view) {
            override fun failed(tBaseResult: BaseResult<User>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<User>) {
                view.getLogin(tBaseResult.data)
            }

        }, true)

    }


    fun accounts() {
        val account = RetrofitManager.service.accounts()
        doRequest(account, object : Callback<User>(view) {
            override fun failed(tBaseResult: BaseResult<User>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<User>) {
                view.getAccount(tBaseResult.data)
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
                    view.onStudentList(tBaseResult.data)
            }
        }, true)
    }

}