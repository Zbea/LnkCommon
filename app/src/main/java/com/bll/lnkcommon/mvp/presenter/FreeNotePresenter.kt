package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.ShareNoteList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*

class FreeNotePresenter(view: IContractView.IFreeNoteView):BasePresenter<IContractView.IFreeNoteView>(view) {

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


    fun getReceiveNotes(map: HashMap<String,Any>, isShow:Boolean) {
        val grade = RetrofitManager.service.getReceiveList(map)
        doRequest(grade, object : Callback<ShareNoteList>(view) {
            override fun failed(tBaseResult: BaseResult<ShareNoteList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<ShareNoteList>) {
                if (tBaseResult.data!=null)
                    view.onReceiveList(tBaseResult.data)
            }
        }, isShow)
    }

    fun getShareNotes(map: HashMap<String,Any>, isShow:Boolean) {
        val grade = RetrofitManager.service.getShareList(map)
        doRequest(grade, object : Callback<ShareNoteList>(view) {
            override fun failed(tBaseResult: BaseResult<ShareNoteList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<ShareNoteList>) {
                if (tBaseResult.data!=null)
                    view.onShareList(tBaseResult.data)
            }
        }, isShow)
    }

    fun deleteShareNote(map: HashMap<String,Any>) {
        val body=RequestUtils.getBody(map)
        val grade = RetrofitManager.service.deleteShare(body)
        doRequest(grade, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onDeleteSuccess()
            }
        }, true)
    }

    fun commitShare(map:HashMap<String,Any>){
        val body= RequestUtils.getBody(map)
        val commit = RetrofitManager.service.shareFreeNote(body)
        doRequest(commit, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onShare()
            }
        }, true)
    }

    fun onBindFriend(account: String) {
        val map=HashMap<String,Any>()
        map["account"]=account
        val body = RequestUtils.getBody(map)
        val editName = RetrofitManager.service.onBindFriend(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onBind()
            }
        }, true)
    }

    fun unbindFriend(ids: List<Int>) {
        val map=HashMap<String,Any>()
        map["ids"]= ids
        val body = RequestUtils.getBody(map)
        val editName = RetrofitManager.service.onUnbindFriend(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onUnbind()
            }
        }, true)
    }

    fun getFriends() {
        val editName = RetrofitManager.service.onFriendList()
        doRequest(editName, object : Callback<FriendList>(view) {
            override fun failed(tBaseResult: BaseResult<FriendList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<FriendList>) {
                if (tBaseResult.data!=null)
                    view.onListFriend(tBaseResult.data)
            }
        }, true)
    }

}