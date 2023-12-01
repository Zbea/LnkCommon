package com.bll.lnkcommon.mvp.presenter

import android.util.Pair
import com.bll.lnkcommon.mvp.model.CloudList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*

class CloudPresenter(view: IContractView.ICloudView) : BasePresenter<IContractView.ICloudView>(view){

    /**
     * 获取分类
     */
    fun getType(type:Int) {
        val map=HashMap<String,Any>()
        map["type"]=type
        val manager = RetrofitManager.service.getCloudType(map)
        doRequest(manager, object : Callback<MutableList<String>>(view) {
            override fun failed(tBaseResult: BaseResult<MutableList<String>>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<MutableList<String>>) {
                if (tBaseResult.data!=null)
                    view.onType(tBaseResult.data)
            }
        }, true)
    }

    /**
     * 获取列表
     */
    fun getList(map :HashMap<String,Any>) {
        val type = RetrofitManager.service.getCloudList(map)
        doRequest(type, object : Callback<CloudList>(view) {
            override fun failed(tBaseResult: BaseResult<CloudList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<CloudList>) {
                if (tBaseResult.data!=null)
                    view.onList(tBaseResult.data)
            }
        }, true)
    }

    fun deleteCloud(ids:List<Int>) {
        val body = RequestUtils.getBody(
            Pair.create("ids", ids.toIntArray())
        )
        val delete = RetrofitManager.service.deleteCloudList(body)

        doRequest(delete, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onDelete()
            }
        }, true)

    }

}