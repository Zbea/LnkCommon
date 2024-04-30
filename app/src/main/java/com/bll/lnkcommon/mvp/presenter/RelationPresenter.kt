package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class RelationPresenter(view: IContractView.IRelationView) : BasePresenter<IContractView.IRelationView>(view) {

    fun getStudents() {
        val grade = RetrofitManager.service.onStudentList()
        doRequest(grade, object : Callback<MutableList<StudentBean>>(view) {
            override fun failed(tBaseResult: BaseResult<MutableList<StudentBean>>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<MutableList<StudentBean>>) {
                if (tBaseResult.data!=null)
                    view.onListStudents(tBaseResult.data)
            }
        }, false)
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
        }, false)
    }

    fun getMessageTotal(){
        val list= RetrofitManager.service.getMessages()
        doRequest(list, object : Callback<Int>(view) {
            override fun failed(tBaseResult: BaseResult<Int>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Int>) {
                    view.onMessageTotal(tBaseResult.data!!)
            }
        })
    }


}