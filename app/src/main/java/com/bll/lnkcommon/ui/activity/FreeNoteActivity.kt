package com.bll.lnkcommon.ui.activity

import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.utils.DateUtils
import com.google.gson.Gson
import java.util.Date

class FreeNoteActivity:BaseActivity() {

    override fun layoutId(): Int {
        return R.layout.ac_free_note
    }
    override fun initData() {
        showLog(Gson().toJson(getUser()))
    }
    override fun initView() {
        setPageTitle(DateUtils.intToStringDataNoHour(Date().time))
    }

}