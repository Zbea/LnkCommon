package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.AppUpdateBean
import com.bll.lnkcommon.mvp.model.CommonData
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.BasePresenter
import com.bll.lnkcommon.net.BaseResult
import com.bll.lnkcommon.net.Callback
import com.bll.lnkcommon.net.RetrofitManager


class CommonPresenter(view: IContractView.ICommonView) : BasePresenter<IContractView.ICommonView>(view) {

    fun getCommon() {
        val editName = RetrofitManager.service.getCommonGrade()
        doRequest(editName, object : Callback<CommonData>(view,false) {
            override fun failed(tBaseResult: BaseResult<CommonData>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<CommonData>) {
                if (tBaseResult.data!=null)
                    view.onCommon(tBaseResult.data)
            }
        }, false)
    }

    //获取更新信息
    fun getAppUpdate() {
        val list= RetrofitManager.service.onAppUpdate()
        doRequest(list, object : Callback<AppUpdateBean>(view,false) {
            override fun failed(tBaseResult: BaseResult<AppUpdateBean>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<AppUpdateBean>) {
                if (tBaseResult.data!=null)
                    view.onAppUpdate(tBaseResult.data)
            }
        }, false)
    }

}