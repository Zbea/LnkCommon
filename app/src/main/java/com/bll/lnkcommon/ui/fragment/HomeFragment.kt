package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.Constants.DATE_EVENT
import com.bll.lnkcommon.Constants.USER_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.presenter.RelationPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IRelationView
import com.bll.lnkcommon.ui.activity.DateActivity
import com.bll.lnkcommon.ui.activity.FreeNoteActivity
import com.bll.lnkcommon.ui.adapter.AppListAdapter
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.utils.SPUtil
import com.bll.lnkcommon.utils.date.LunarSolarConverter
import com.bll.lnkcommon.utils.date.Solar
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.rv_list
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment:BaseFragment(),IRelationView {

    private val presenter=RelationPresenter(this)
    private var apps= mutableListOf<AppBean>()
    private var mAdapter: AppListAdapter?=null

    override fun onListStudents(list: MutableList<StudentBean>) {
        DataBeanManager.students=list
        if (list.size>0){
            SPUtil.putInt("studentId",list[0].childId)
            EventBus.getDefault().post(Constants.STUDENT_EVENT)
        }
    }
    override fun onListFriend(list: FriendList) {
        DataBeanManager.friends=list.list
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }
    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[0])

        ll_date.setOnClickListener {
            customStartActivity(Intent(activity,DateActivity::class.java))
        }

        tv_free_note.setOnClickListener {
            customStartActivity(Intent(activity,FreeNoteActivity::class.java))
        }

        initRecyclerView()

    }
    override fun lazyLoad() {
        if (DataBeanManager.courses.isEmpty())
            mCommonPresenter.getCommon()
        if (isLoginState()){
            presenter.getStudents()
            presenter.getFriends()
        }
        setDateView()
        findAppData()
    }

    /**
     * 设置当天时间以及图片
     */
    private fun setDateView() {
        tv_date_today.text = SimpleDateFormat("MM月dd日 E", Locale.CHINA).format(Date())
        val path= FileAddress().getPathDate(DateUtils.longToStringCalender(Date().time))+"/draw.png"
        if (File(path).exists())
            GlideUtils.setImageNoCacheUrl(activity,path,iv_date)

        val solar= Solar()
        solar.solarYear=DateUtils.getYear()
        solar.solarMonth=DateUtils.getMonth()
        solar.solarDay=DateUtils.getDay()
        val lunar=LunarSolarConverter.SolarToLunar(solar)
        tv_date_luna.text=lunar.getChinaMonthString(lunar.lunarMonth)+"月"+lunar.getChinaDayString(lunar.lunarDay)

        val str = if (!solar.solar24Term.isNullOrEmpty()) {
            "24节气   "+solar.solar24Term
        } else {
            if (!solar.solarFestivalName.isNullOrEmpty()) {
                "节日   "+solar.solarFestivalName
            } else {
                if (!lunar.lunarFestivalName.isNullOrEmpty()) {
                    "节日   "+lunar.lunarFestivalName
                }
                else{
                    ""
                }
            }
        }
        tv_date_festival.text=str
    }

    private fun initRecyclerView(){
        rv_list.layoutManager = GridLayoutManager(activity,5)//创建布局管理
        mAdapter = AppListAdapter(R.layout.item_main_app, 1,null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            val packageName= apps[position].packageName
            AppUtils.startAPP(activity,packageName)
        }
    }

    private fun findAppData(){
        apps= AppDaoManager.getInstance().queryMenu()
        mAdapter?.setNewData(apps)
    }


    override fun onEventBusMessage(msgFlag: String) {
        when (msgFlag) {
            USER_EVENT->{
                lazyLoad()
            }
            DATE_EVENT -> {
                setDateView()
            }
        }
    }

    override fun onRefreshData() {
        super.onRefreshData()
        if (DataBeanManager.courses.isEmpty())
            mCommonPresenter.getCommon()
        if (isLoginState()){
            if (DataBeanManager.students.size==0)
                presenter.getStudents()
            presenter.getFriends()
        }
        setDateView()
    }

}