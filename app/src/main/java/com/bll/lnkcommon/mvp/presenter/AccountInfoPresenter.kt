package com.bll.lnkcommon.mvp.presenter

import android.util.Pair
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


    fun editSchool(id: Int) {
        val map=HashMap<String,Any>()
        map["schoolId"]=id
        val body = RequestUtils.getBody(map)
        val editName = RetrofitManager.service.editSchool(body)

        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {

            }
        }, true)
    }
}