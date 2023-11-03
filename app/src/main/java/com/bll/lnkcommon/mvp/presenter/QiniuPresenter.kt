package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.BasePresenter
import com.bll.lnkcommon.net.BaseResult
import com.bll.lnkcommon.net.Callback
import com.bll.lnkcommon.net.RetrofitManager

class QiniuPresenter(view: IContractView.IQiniuView):
    BasePresenter<IContractView.IQiniuView>(view) {

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


}