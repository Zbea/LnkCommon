package com.bll.lnkcommon.mvp.presenter

import android.util.Pair
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*

class CloudUploadPresenter(view: IContractView.ICloudUploadView):
    BasePresenter<IContractView.ICloudUploadView>(view) {

    fun upload(list:MutableList<CloudListBean>) {
        if (list.isEmpty())
        {
            return
        }
        val body= RequestUtils.getBody(
            Pair.create("listModel",list)
        )
        val type = RetrofitManager.service.cloudUpload(body)
        doRequest(type, object : Callback<MutableList<Int>>(view) {
            override fun failed(tBaseResult: BaseResult<MutableList<Int>>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<MutableList<Int>>) {
                view.onSuccess(tBaseResult.data)
            }
        }, false)
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
                view.onDeleteSuccess()
            }
        }, false)

    }

}