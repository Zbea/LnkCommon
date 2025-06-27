package com.bll.lnkcommon.ui.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.*
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.manager.CalenderDaoManager
import com.bll.lnkcommon.manager.DiaryDaoManager
import com.bll.lnkcommon.manager.FreeNoteDaoManager
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.manager.NoteContentDaoManager
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.manager.RecordDaoManager
import com.bll.lnkcommon.manager.WallpaperDaoManager
import com.bll.lnkcommon.mvp.model.AreaBean
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.presenter.QiniuPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IQiniuView
import com.bll.lnkcommon.ui.adapter.MainListAdapter
import com.bll.lnkcommon.ui.fragment.*
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.SPUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.ac_main.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*

class MainActivity : BaseActivity(){

    private var lastPosition = 0
    private var mHomeAdapter: MainListAdapter? = null
    private var mData=mutableListOf<ItemList>()
    private var lastFragment: Fragment? = null

    private var mainFragment: MainFragment? = null
    private var bookcaseFragment: BookcaseFragment? = null
    private var noteFragment: NoteFragment? = null
    private var appFragment: AppFragment? = null
    private var teachFragment: HomeworkManagerFragment?=null
    private var textbookFragment:TextbookFragment?=null
    private var documentFragment:DocumentFragment?=null

    private val myBroadcastReceiver=MyBroadcastReceiver()

    override fun layoutId(): Int {
        return R.layout.ac_main
    }

    override fun initData() {
        val targetFileStr = FileAddress().getLauncherPath()
        if (FileUtils.isExist(targetFileStr)){
            FileUtils.deleteFile(File(targetFileStr))
        }

        mData= DataBeanManager.getMainData()
        //如果账号有关联学生
        if (MethodManager.isLogin()&&DataBeanManager.students.size>0){
            changeData()
        }
    }


    override fun initView() {
        val intentFilter= IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(myBroadcastReceiver,intentFilter)

        val isTips=SPUtil.getBoolean("SpecificationTips")
        if (!isTips){
            showView(ll_tips)
        }

        setLoginView()

        mainFragment = MainFragment()
        bookcaseFragment = BookcaseFragment()
        documentFragment=DocumentFragment()
        noteFragment= NoteFragment()
        appFragment = AppFragment()
        textbookFragment= TextbookFragment()
        teachFragment= HomeworkManagerFragment()

        switchFragment(lastFragment, mainFragment)

        rv_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mHomeAdapter = MainListAdapter(R.layout.item_main_list, mData)
        rv_list.adapter = mHomeAdapter
        mHomeAdapter?.bindToRecyclerView(rv_list)
        mHomeAdapter?.setOnItemClickListener { adapter, view, position ->
            mHomeAdapter?.updateItem(lastPosition, false)//原来的位置去掉勾选
            mHomeAdapter?.updateItem(position, true)//更新新的位置
            when (position) {
                0 -> switchFragment(lastFragment, mainFragment)
                1 -> switchFragment(lastFragment, bookcaseFragment)
                2 -> switchFragment(lastFragment, documentFragment)
                3 -> switchFragment(lastFragment, noteFragment)
                4 -> switchFragment(lastFragment, appFragment)
                5 -> switchFragment(lastFragment, textbookFragment)
                6 -> switchFragment(lastFragment, teachFragment)
            }
            lastPosition=position
        }

        startRemind()

        iv_user.setOnClickListener {
            if (MethodManager.isLogin()){
                customStartActivity(Intent(this,AccountInfoActivity::class.java))
            }
            else{
                customStartActivity(Intent(this,AccountLoginActivity::class.java))
            }
        }

        ll_tips.setOnClickListener {
            disMissView(ll_tips)
            SPUtil.putBoolean("SpecificationTips",true)
        }

    }

    /**
     * 开始每天定时自动刷新
     */
    private fun startRemind() {

        Calendar.getInstance().apply {
            val currentTimeMillisLong = System.currentTimeMillis()
            timeInMillis = currentTimeMillisLong
            timeZone = TimeZone.getTimeZone("GMT+8")
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            var selectLong = timeInMillis

            if (currentTimeMillisLong > selectLong) {
                add(Calendar.DAY_OF_MONTH, 1)
                selectLong = timeInMillis
            }

            val intent = Intent(this@MainActivity, MyBroadcastReceiver::class.java)
            intent.action = Constants.ACTION_DAY_REFRESH
            val pendingIntent =if (Build.VERSION.SDK_INT >= 31)
                PendingIntent.getBroadcast(this@MainActivity, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            else
                PendingIntent.getBroadcast(this@MainActivity, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(
                AlarmManager.SYS_RTC_WAKEUP, selectLong,
                AlarmManager.INTERVAL_DAY, pendingIntent
            )
        }
    }


    private fun setLoginView(){
        tv_login.text=if (MethodManager.isLogin()) "登录" else "未登录"
    }

    private fun refreshData(boolean: Boolean){
        if (boolean){
            if (mData.size==5)
                changeData()
        }
        else{
            if (mData.size>5)
            {
                mData.removeLast()
                mData.removeLast()
            }

        }
        mHomeAdapter?.notifyDataSetChanged()
    }

    private fun changeData(){
        mData.add(ItemList().apply {
            icon = MyApplication.mContext.getDrawable(R.mipmap.icon_tab_textbook)
            icon_check = MyApplication.mContext.getDrawable(R.mipmap.icon_tab_textbook_check)
            name = DataBeanManager.mainListTitle[5]
        })
        mData.add(ItemList().apply {
            icon = MyApplication.mContext.getDrawable(R.mipmap.icon_tab_homework)
            icon_check = MyApplication.mContext.getDrawable(R.mipmap.icon_tab_homework_check)
            name = DataBeanManager.mainListTitle[6]
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

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    override fun onEventBusMessage(msgFlag: String) {
        when (msgFlag) {
            Constants.USER_EVENT -> {
                setLoginView()
            }
            Constants.STUDENT_EVENT -> {
                if(DataBeanManager.students.size>0){
                    refreshData(true)
                }
                else{
                    refreshData(false)
                    if (lastPosition==5){
                        switchFragment(textbookFragment, mainFragment)
                        lastPosition=0
                        mHomeAdapter?.updateItem(0, true)//更新新的位置
                    }
                    if (lastPosition==6){
                        switchFragment(teachFragment, mainFragment)
                        lastPosition=0
                        mHomeAdapter?.updateItem(0, true)//更新新的位置
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadcastReceiver)
    }

}