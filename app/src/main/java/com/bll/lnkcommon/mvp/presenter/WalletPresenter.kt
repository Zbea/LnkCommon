package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.AccountOrder
import com.bll.lnkcommon.mvp.model.AccountQdBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class WalletPresenter(view: IContractView.IWalletView) : BasePresenter<IContractView.IWalletView>(view) {

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

    //获取学豆列表
    fun getXdList(boolean: Boolean) {
        val list = RetrofitManager.service.getSMoneyList()
        doRequest(list, object : Callback<MutableList<AccountQdBean>>(view) {
            override fun failed(tBaseResult: BaseResult<MutableList<AccountQdBean>>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<MutableList<AccountQdBean>>) {
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

    fun transferQd(map: HashMap<String,Any>)
    {
        val body=RequestUtils.getBody(map)
        val post = RetrofitManager.service.transferQd(body)
        doRequest(post, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.transferSuccess()
            }
        }, true)
    }


}