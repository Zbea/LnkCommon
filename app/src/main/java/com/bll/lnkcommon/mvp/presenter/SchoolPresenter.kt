package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.SchoolBean
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.BasePresenter
import com.bll.lnkcommon.net.BaseResult
import com.bll.lnkcommon.net.Callback
import com.bll.lnkcommon.net.RetrofitManager


class SchoolPresenter(view: IContractView.ISchoolView) : BasePresenter<IContractView.ISchoolView>(view) {

    fun getSchool() {
        val grade = RetrofitManager.service.getCommonSchool()
        doRequest(grade, object : Callback<MutableList<SchoolBean>>(view) {
            override fun failed(tBaseResult: BaseResult<MutableList<SchoolBean>>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<MutableList<SchoolBean>>) {
                if (!tBaseResult.data.isNullOrEmpty())
                    view.onListSchools(tBaseResult.data)
            }
        }, true)
    }

}