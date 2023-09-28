package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.Constants.AUTO_UPLOAD_1MONTH_EVENT
import com.bll.lnkcommon.Constants.AUTO_UPLOAD_EVENT
import com.bll.lnkcommon.Constants.CALENDER_SET_EVENT
import com.bll.lnkcommon.Constants.CHECK_PASSWORD_EVENT
import com.bll.lnkcommon.Constants.DATE_DRAWING_EVENT
import com.bll.lnkcommon.Constants.USER_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CheckPasswordDialog
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.manager.CalenderDaoManager
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.presenter.RelationPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IRelationView
import com.bll.lnkcommon.ui.activity.DateActivity
import com.bll.lnkcommon.ui.activity.drawing.DiaryActivity
import com.bll.lnkcommon.ui.activity.drawing.FreeNoteActivity
import com.bll.lnkcommon.ui.activity.drawing.PlanOverviewActivity
import com.bll.lnkcommon.ui.adapter.AppListAdapter
import com.bll.lnkcommon.utils.*
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
    private var nowDayPos=1
    private var nowDay=0L
    private var calenderPath=""

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
            customStartActivity(Intent(activity, FreeNoteActivity::class.java))
        }

        tv_plan.setOnClickListener {
            customStartActivity(Intent(activity, PlanOverviewActivity::class.java))
        }

        tv_diary.setOnClickListener {
            if (checkPassword!=null&&checkPassword?.isSet==true){
                CheckPasswordDialog(requireActivity()).builder()?.setOnDialogClickListener{
                    customStartActivity(Intent(activity, DiaryActivity::class.java))
                }
            }
            else{
                customStartActivity(Intent(activity, DiaryActivity::class.java))
            }
        }

        v_up.setOnClickListener{
            nowDay-=Constants.dayLong
            setDateView()
            if (nowDayPos>1){
                nowDayPos-=1
                setCalenderBg()
            }
        }

        v_down.setOnClickListener {
            nowDay+=Constants.dayLong
            setDateView()
            val allDay=if (DateUtils().isYear(DateUtils.getYear())) 366 else 365
            if (nowDayPos<=allDay){
                nowDayPos+=1
                setCalenderBg()
            }
        }

        iv_change.setOnClickListener {
            if (!isLoginState())
                return@setOnClickListener
            if (ll_calender.isVisible){
                disMissView(ll_calender)
            }
            else{
                showView(ll_calender)
            }
        }

        initRecyclerView()
        nowDay=DateUtils.getStartOfDayInMillis()
    }
    override fun lazyLoad() {
        if (DataBeanManager.courses.isEmpty())
            mCommonPresenter.getCommon()
        if (isLoginState()){
            presenter.getStudents()
            presenter.getFriends()
        }
        setDeleteOldCalender()
        setDateView()
        setCalenderView()
        findAppData()
    }

    /**
     * 设置当天时间以及图片
     */
    private fun setDateView() {
        tv_date_today.text = SimpleDateFormat("MM月dd日 E", Locale.CHINA).format(Date(nowDay))
        val dates=DateUtils.getDateNumber(nowDay)
        val solar= Solar()
        solar.solarYear=dates[0]
        solar.solarMonth=dates[1]
        solar.solarDay=dates[2]
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

        val path= FileAddress().getPathDate(DateUtils.longToStringCalender(nowDay))+"/draw.png"
        GlideUtils.setImageNoCacheUrl(activity,path,iv_date)
    }

    /**
     * 删掉过期台历
     */
    private fun setDeleteOldCalender(){
        //删除过期台历
        val oldItems=CalenderDaoManager.getInstance().queryListOld(DateUtils.getYear())
        for (item in oldItems){
            FileUtils.deleteFile(File(item.path))
        }
        CalenderDaoManager.getInstance().deleteBeans(oldItems)
    }

    /**
     * 设置台历
     */
    private fun setCalenderView(){
        val calenderUtils=CalenderUtils(DateUtils.longToStringDataNoHour(nowDay))
        nowDayPos=calenderUtils.elapsedTime()
        val item=CalenderDaoManager.getInstance().queryCalenderBean()
        if (item!=null){
            showView(ll_calender)
            calenderPath=item.path
            setCalenderBg()
        }
        else{
            disMissView(ll_calender)
        }
    }

    private fun setCalenderBg(){
        val listFiles=FileUtils.getFiles(calenderPath)
        if (listFiles!=null&&listFiles.size>nowDayPos-1){
            val file=listFiles[nowDayPos-1]
            GlideUtils.setImageFile(requireActivity(),file,iv_calender_bg)
        }
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

    /**
     * 查找菜单应用
     */
    private fun findAppData(){
        apps= AppDaoManager.getInstance().queryMenu()
        mAdapter?.setNewData(apps)
    }


    override fun onEventBusMessage(msgFlag: String) {
        when (msgFlag) {
            USER_EVENT->{
                checkPassword=getCheckPasswordObj()
                lazyLoad()
                setCalenderView()
            }
            DATE_DRAWING_EVENT -> {
                setDateView()
            }
            CALENDER_SET_EVENT->{
                setCalenderView()
            }
            AUTO_UPLOAD_1MONTH_EVENT->{
                setDeleteOldCalender()
            }
            AUTO_UPLOAD_EVENT->{
                nowDay=DateUtils.getStartOfDayInMillis()
                setDateView()
                setCalenderView()
            }
            CHECK_PASSWORD_EVENT->{
                checkPassword=getCheckPasswordObj()
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
        findAppData()
    }

}