package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.MessageList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*

class MessagePresenter(view: IContractView.IMessageView): BasePresenter<IContractView.IMessageView>(view) {


    fun getList(map: HashMap<String,Any>){
        val list= RetrofitManager.service.getMessages(map)
        doRequest(list, object : Callback<MessageList>(view) {
            override fun failed(tBaseResult: BaseResult<MessageList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<MessageList>) {
                if (tBaseResult?.data!=null)
                    view.onList(tBaseResult.data)
            }
        })
    }

    fun commitMessage(map: HashMap<String,Any>){
        val body=RequestUtils.getBody(map)
        val list= RetrofitManager.service.commitMessage(body)
        doRequest(list, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                    view.onCommitSuccess()
            }
        },true)
    }

}