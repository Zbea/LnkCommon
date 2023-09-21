package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.JournalList
import com.bll.lnkcommon.mvp.model.WallpaperList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class JournalPresenter(view: IContractView.IJournalView) : BasePresenter<IContractView.IJournalView>(view) {

    fun getList(map: HashMap<String,Any>) {

        val list = RetrofitManager.service.getJournalList(map)

        doRequest(list, object : Callback<JournalList>(view) {
            override fun failed(tBaseResult: BaseResult<JournalList>): Boolean {
                return false
            }

            override fun success(tBaseResult: BaseResult<JournalList>) {
                if (tBaseResult.data!=null)
                    view.onList(tBaseResult.data)
            }

        }, true)
    }


}