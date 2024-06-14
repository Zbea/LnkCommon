package com.bll.lnkcommon.mvp.presenter

import android.util.Pair
import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class PermissionSettingPresenter(view: IContractView.IPermissionSettingView) : BasePresenter<IContractView.IPermissionSettingView>(view) {


    fun onStudent(id: Int) {
        val map=HashMap<String,Any>()
        map["childId"]=id
        val editName = RetrofitManager.service.getStudent(map)
        doRequest(editName, object : Callback<StudentBean>(view) {
            override fun failed(tBaseResult: BaseResult<StudentBean>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<StudentBean>) {
                if (tBaseResult.data!=null)
                    view.onStudent(tBaseResult.data)
            }
        }, true)
    }


    fun onInsertTime(map: HashMap<String,Any>) {
        val body = RequestUtils.getBody(map)
        val editName = RetrofitManager.service.onInsertPermissionTime(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onSuccess()
            }
        }, true)
    }

    fun onEditTime(map: HashMap<String,Any>) {
        val body = RequestUtils.getBody(map)
        val editName = RetrofitManager.service.onEditPermissionTime(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onEditSuccess()
            }
        }, true)
    }


    fun onDeleteTime(map: HashMap<String,Any>) {
        val body = RequestUtils.getBody(map)
        val editName = RetrofitManager.service.onDeletePermissionTime(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onSuccess()
            }
        }, true)
    }


    fun onChangeAllow(map: HashMap<String,Any>) {
        val body = RequestUtils.getBody(map)
        val editName = RetrofitManager.service.onUpdatePermissionAllow(body)
        doRequest(editName, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onChangeSuccess()
            }
        }, true)
    }

}