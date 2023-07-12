package com.bll.lnkcommon.ui.activity

import android.content.Intent
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import kotlinx.android.synthetic.main.ac_bookstore_type.*

/**
 * 书城分类
 */
class BookStoreTypeActivity: BaseActivity() {


    override fun layoutId(): Int {
        return R.layout.ac_bookstore_type
    }

    override fun initData() {

    }
    override fun initView() {
        iv_jc?.setOnClickListener {

        }

        iv_gj?.setOnClickListener {
            gotoBookStore(1)
        }

        iv_zrkx?.setOnClickListener {
            gotoBookStore(2)
        }

        iv_shkx?.setOnClickListener {
            gotoBookStore(3)
        }

        iv_swkx?.setOnClickListener {
            gotoBookStore(4)
        }

        iv_ydcy?.setOnClickListener {
            gotoBookStore(5)
        }
    }

    private fun gotoBookStore(type: Int){
        val intent=Intent(this, BookStoreActivity::class.java)
        intent.flags=type
        startActivity(intent)
    }

}