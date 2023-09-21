package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.WallpaperList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class WallpaperPresenter(view: IContractView.IWallpaperView) : BasePresenter<IContractView.IWallpaperView>(view) {

    fun getList(map: HashMap<String,Any>) {

        val app = RetrofitManager.service.getWallpaperList(map)

        doRequest(app, object : Callback<WallpaperList>(view) {
            override fun failed(tBaseResult: BaseResult<WallpaperList>): Boolean {
                return false
            }

            override fun success(tBaseResult: BaseResult<WallpaperList>) {
                if (tBaseResult.data!=null)
                    view.onList(tBaseResult.data)
            }

        }, true)
    }

    fun onBuy(map: HashMap<String, Any> ) {

        val requestBody= RequestUtils.getBody(map)
        val download = RetrofitManager.service.onBuy(requestBody)

        doRequest(download, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }

            override fun success(tBaseResult: BaseResult<Any>) {
                view.buySuccess()
            }

        }, true)

    }

}