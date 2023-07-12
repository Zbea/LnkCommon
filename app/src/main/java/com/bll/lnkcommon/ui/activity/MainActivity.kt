package com.bll.lnkcommon.ui.activity

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.mvp.model.MainListBean
import com.bll.lnkcommon.ui.adapter.MainListAdapter
import com.bll.lnkcommon.ui.fragment.AppFragment
import com.bll.lnkcommon.ui.fragment.BookcaseFragment
import com.bll.lnkcommon.ui.fragment.HomeFragment
import com.bll.lnkcommon.ui.fragment.NoteFragment
import kotlinx.android.synthetic.main.ac_main.*

class MainActivity : BaseActivity() {

    private var lastPosition = 0
    private var mHomeAdapter: MainListAdapter? = null
    private var mData=mutableListOf<MainListBean>()
    private var lastFragment: Fragment? = null

    private var homeFragment: HomeFragment? = null
    private var bookcaseFragment: BookcaseFragment? = null
    private var noteFragment: NoteFragment? = null
    private var appFragment: AppFragment? = null

    override fun layoutId(): Int {
        return R.layout.ac_main
    }

    override fun initData() {
        mData= DataBeanManager.getMainData()
    }


    override fun initView() {

        homeFragment = HomeFragment()
        bookcaseFragment = BookcaseFragment()
        noteFragment= NoteFragment()
        appFragment = AppFragment()

        switchFragment(lastFragment, homeFragment)

        rv_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mHomeAdapter = MainListAdapter(R.layout.item_main_list, mData)
        rv_list.adapter = mHomeAdapter
        mHomeAdapter?.bindToRecyclerView(rv_list)
        mHomeAdapter?.setOnItemClickListener { adapter, view, position ->
            mHomeAdapter?.updateItem(lastPosition, false)//原来的位置去掉勾选
            mHomeAdapter?.updateItem(position, true)//更新新的位置
            when (position) {
                0 -> switchFragment(lastFragment, homeFragment)
                1 -> switchFragment(lastFragment, bookcaseFragment)
                2 -> switchFragment(lastFragment, noteFragment)
                3 -> switchFragment(lastFragment, appFragment)
            }
            lastPosition=position
        }

        iv_user.setOnClickListener {
            startActivity(Intent(this,AccountInfoActivity::class.java))
        }

    }

    //页码跳转
    private fun switchFragment(from: Fragment?, to: Fragment?) {
        if (from != to) {
            lastFragment = to
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()

            if (!to!!.isAdded) {
                if (from != null) {
                    ft.hide(from)
                }
                ft.add(R.id.frame_layout, to).commit()
            } else {
                if (from != null) {
                    ft.hide(from)
                }
                ft.show(to).commit()
            }
        }
    }

//    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        return if (event.getKeyCode() === KeyEvent.KEYCODE_BACK) {
//            true
//        } else {
//            super.dispatchKeyEvent(event)
//        }
//    }

}