package com.bll.lnkcommon.ui.activity

import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity

class OperatingGuideActivity :BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.ac_list_tab
    }

    override fun initData() {
    }

    override fun initView() {
        setPageTitle("操作手册")
    }
}