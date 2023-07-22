package com.bll.lnkcommon.ui.activity

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.MyApplication
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.mvp.model.AreaBean
import com.bll.lnkcommon.mvp.model.MainListBean
import com.bll.lnkcommon.ui.adapter.MainListAdapter
import com.bll.lnkcommon.ui.fragment.*
import com.bll.lnkcommon.ui.fragment.homework.HomeworkFragment
import com.bll.lnkcommon.utils.FileUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private var teachFragment: HomeworkManagerFragment?=null
    private var textbookFragment:TextbookFragment?=null

    override fun layoutId(): Int {
        return R.layout.ac_main
    }

    override fun initData() {

        val areaJson = FileUtils.readFileContent(resources.assets.open("city.json"))
        val type= object : TypeToken<List<AreaBean>>() {}.type
        DataBeanManager.provinces = Gson().fromJson(areaJson, type)

        mData= DataBeanManager.getMainData()
        //如果账号有关联学生
        if (mUser?.isBind == true)
            changeData()
    }


    override fun initView() {

        homeFragment = HomeFragment()
        bookcaseFragment = BookcaseFragment()
        noteFragment= NoteFragment()
        appFragment = AppFragment()
        textbookFragment= TextbookFragment()
        teachFragment= HomeworkManagerFragment()

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
                4 -> switchFragment(lastFragment, textbookFragment)
                5 -> switchFragment(lastFragment, teachFragment)
            }
            lastPosition=position
        }

        iv_user.setOnClickListener {
            startActivity(Intent(this,AccountInfoActivity::class.java))
        }

    }

    private fun refreshData(boolean: Boolean){
        if (boolean){
            if (mData.size==4)
                changeData()
        }
        else{
            if (mData.size>4)
            {
                mData.removeLast()
                mData.removeLast()
            }

        }
        mHomeAdapter?.notifyDataSetChanged()
    }

    private fun changeData(){
        mData.add(MainListBean().apply {
            icon = MyApplication.mContext.getDrawable(R.mipmap.icon_main_jc)
            icon_check = MyApplication.mContext.getDrawable(R.mipmap.icon_main_jc_check)
            checked = false
            name = DataBeanManager.mainListTitle[4]
        })
        mData.add(MainListBean().apply {
            icon = MyApplication.mContext.getDrawable(R.mipmap.icon_main_jx)
            icon_check = MyApplication.mContext.getDrawable(R.mipmap.icon_main_jx_check)
            checked = false
            name = DataBeanManager.mainListTitle[5]
        })
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
                ft.add(R.id.frame_layout, to).commitAllowingStateLoss()
            } else {
                if (from != null) {
                    ft.hide(from)
                }
                ft.show(to).commitAllowingStateLoss()
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

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag==Constants.STUDENT_EVENT){
            if(DataBeanManager.students.size>0){
                refreshData(true)
            }
            else{
                refreshData(false)
                if (lastPosition==4){
                    switchFragment(textbookFragment, homeFragment)
                }
                if (lastPosition==5){
                    switchFragment(teachFragment, homeFragment)
                }
                lastPosition=0
                mHomeAdapter?.updateItem(0, true)//更新新的位置
            }
        }
    }

}