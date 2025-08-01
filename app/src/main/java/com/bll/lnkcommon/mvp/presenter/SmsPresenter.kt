package com.bll.lnkcommon.mvp.presenter

import android.util.Pair
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.BasePresenter
import com.bll.lnkcommon.net.BaseResult
import com.bll.lnkcommon.net.Callback
import com.bll.lnkcommon.net.RequestUtils
import com.bll.lnkcommon.net.RetrofitManager


class SmsPresenter(view: IContractView.ISmsView) : BasePresenter<IContractView.ISmsView>(view) {

    fun sms(phone:String) {
        val sms = RetrofitManager.service.getSms(phone)
        doRequest(sms, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onSms()
            }
        }, true)
    }

    fun checkPhone(code: String) {
        val body = RequestUtils.getBody(
            Pair.create("code", code)
        )
        val editName = RetrofitManager.service.checkPhone(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onCheckSuccess()
            }
        }, true)
    }

}