package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.SystemUpdateInfo
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.RequestUtils
import com.bll.lnkcommon.net.RetrofitManager
import com.bll.lnkcommon.net.system.BasePresenter1
import com.bll.lnkcommon.net.system.BaseResult1
import com.bll.lnkcommon.net.system.Callback1


class SystemUpdateManagerPresenter(view: IContractView.ISystemView) : BasePresenter1<IContractView.ISystemView>(view) {

    fun checkSystemUpdate(map: Map<String,String>) {

        val body = RequestUtils.getBody(map)

        val request = RetrofitManager.service1.RELEASE_CHECK_UPDATE(body)
        doRequest(request, object : Callback1<SystemUpdateInfo>(view,false) {
            override fun failed(tBaseResult: BaseResult1<SystemUpdateInfo>): Boolean {
                return false
            }

            override fun success(tBaseResult: BaseResult1<SystemUpdateInfo>) {
                if (tBaseResult.Data!=null)
                    view.onUpdateInfo(tBaseResult.Data)
            }
        }, false)
    }
}