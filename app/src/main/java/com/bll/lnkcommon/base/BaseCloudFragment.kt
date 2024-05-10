package com.bll.lnkcommon.base

import com.bll.lnkcommon.mvp.model.CloudList
import com.bll.lnkcommon.mvp.presenter.CloudPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.IBaseView


abstract class BaseCloudFragment : BaseFragment(), IContractView.ICloudView , IBaseView {

    val mCloudPresenter= CloudPresenter(this)
    var types= mutableListOf<String>()

    override fun onList(item: CloudList) {
        onCloudList(item)
    }
    override fun onType(types: MutableList<String>) {
        onCloudType(types)
    }
    override fun onDelete() {
        onCloudDelete()
    }

    /**
     * 获取云数据
     */
    open fun onCloudList(cloudList: CloudList){

    }
    /**
     * 获取云分类
     */
    open fun onCloudType(types: MutableList<String>){

    }
    /**
     * 删除云数据
     */
    open fun onCloudDelete(){

    }

}
