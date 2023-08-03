package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.AccountOrder
import com.bll.lnkcommon.mvp.model.AccountXDList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.BasePresenter
import com.bll.lnkcommon.net.BaseResult
import com.bll.lnkcommon.net.Callback
import com.bll.lnkcommon.net.RetrofitManager


class WalletPresenter(view: IContractView.IWalletView) : BasePresenter<IContractView.IWalletView>(view) {

    //获取学豆列表
    fun getXdList(boolean: Boolean) {

        val map=HashMap<String,String>()
        map["pageIndex"] = "1"
        map["pageSize"] = "10"

        val list = RetrofitManager.service.getSMoneyList(map)
        doRequest(list, object : Callback<AccountXDList>(view) {
            override fun failed(tBaseResult: BaseResult<AccountXDList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<AccountXDList>) {
                view.onXdList(tBaseResult.data)
            }

        }, boolean)

    }



    //提交学豆订单
    fun postXdOrder(id:String)
    {
        val post = RetrofitManager.service.postOrder(id)
        doRequest(post, object : Callback<AccountOrder>(view) {
            override fun failed(tBaseResult: BaseResult<AccountOrder>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<AccountOrder>) {
                view.onXdOrder(tBaseResult.data)
            }
        }, true)
    }

    //查看订单状态
    fun checkOrder(id:String)
    {
        val order = RetrofitManager.service.getOrderStatus(id)
        doRequest(order, object : Callback<AccountOrder>(view) {
            override fun failed(tBaseResult: BaseResult<AccountOrder>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<AccountOrder>) {
                view.checkOrder(tBaseResult.data)
            }
        }, false)
    }



}