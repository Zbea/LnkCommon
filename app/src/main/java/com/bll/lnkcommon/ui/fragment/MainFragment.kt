package com.bll.lnkcommon.ui.fragment

import PopupClick
import android.content.Intent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.Constants.AUTO_REFRESH_YEAR_EVENT
import com.bll.lnkcommon.Constants.AUTO_REFRESH_EVENT
import com.bll.lnkcommon.Constants.CALENDER_SET_EVENT
import com.bll.lnkcommon.Constants.CHECK_PASSWORD_EVENT
import com.bll.lnkcommon.Constants.DATE_DRAWING_EVENT
import com.bll.lnkcommon.Constants.USER_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.PrivacyPasswordDialog
import com.bll.lnkcommon.manager.*
import com.bll.lnkcommon.mvp.model.*
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
import com.google.gson.Gson
import kotlinx.android.synthetic.main.common_fragment_title.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.rv_list
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class MainFragment:BaseFragment(),IRelationView {

    private val presenter=RelationPresenter(this)
    private var apps= mutableListOf<AppBean>()
    private var mAdapter: AppListAdapter?=null
    private var nowDayPos=1
    private var nowDay=0L
    private var calenderPath=""
    private var popNotes= mutableListOf<PopupBean>()
    private var uploadType=0//上传类型

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
        showView(iv_manager)

        popNotes.add(PopupBean(0,getString(R.string.free_note),R.mipmap.icon_freenote))
        popNotes.add(PopupBean(1,getString(R.string.diary),R.mipmap.icon_diary))
        popNotes.add(PopupBean(2,getString(R.string.overview),R.mipmap.icon_plan))

        ll_date.setOnClickListener {
            customStartActivity(Intent(activity,DateActivity::class.java))
        }

        iv_manager.setOnClickListener {
            PopupClick(requireActivity(),popNotes,iv_manager,5).builder().setOnSelectListener{
                when (it.id) {
                    0 -> {
                        startActivity(Intent(requireActivity(),FreeNoteActivity::class.java))
                    }
                    1->{
                        if (privacyPassword!=null&&privacyPassword?.isSet==true){
                            PrivacyPasswordDialog(requireActivity()).builder()?.setOnDialogClickListener{
                                startActivity(Intent(requireActivity(),DiaryActivity::class.java))
                            }
                        } else{
                            startActivity(Intent(requireActivity(),DiaryActivity::class.java))
                        }
                    }
                    else -> {
                        startActivity(Intent(requireActivity(),PlanOverviewActivity::class.java))
                    }
                }
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
    }
    override fun lazyLoad() {
        if (NetworkUtil.isNetworkAvailable(requireActivity())) {
            if (DataBeanManager.courses.isEmpty())
                mCommonPresenter.getCommon()
            if (isLoginState()){
                presenter.getStudents()
                presenter.getFriends()
            }
        }
        nowDay=DateUtils.getStartOfDayInMillis()
        setDateView()
        setCalenderView()
        findAppData()
    }

    /**
     * 设置当天时间以及图片
     */
    private fun setDateView() {
        tv_date_month.text=SimpleDateFormat("MM").format(nowDay)
        tv_date_day.text=SimpleDateFormat("dd").format(nowDay)
        tv_date_week.text=SimpleDateFormat("EEEE").format(Date(nowDay))

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
        if (File(path).exists()){
            GlideUtils.setImageNoCacheUrl(activity,path,iv_date)
        }
        else{
            iv_date.setImageResource(0)
        }
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
        mAdapter = AppListAdapter(R.layout.item_main_app, 0,null)
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
                privacyPassword=getCheckPasswordObj()
                lazyLoad()
                setCalenderView()
            }
            DATE_DRAWING_EVENT -> {
                setDateView()
            }
            CALENDER_SET_EVENT->{
                setCalenderView()
            }
            AUTO_REFRESH_YEAR_EVENT->{

            }
            AUTO_REFRESH_EVENT->{
                nowDay=DateUtils.getStartOfDayInMillis()
                setDateView()
                setCalenderView()
                findAppData()
            }
            CHECK_PASSWORD_EVENT->{
                privacyPassword=getCheckPasswordObj()
            }
        }
    }

    override fun onRefreshData() {
        lazyLoad()
    }

    /**
     * 每年上传日记
     */
    fun uploadDiary(token:String){
        cloudList.clear()
        val nullItems= mutableListOf<DiaryBean>()
        val diarys=DiaryDaoManager.getInstance().queryList()
        for (diaryBean in diarys){
            val fileName=DateUtils.longToString(diaryBean.date)
            val path=FileAddress().getPathDiary(fileName)
            if (FileUtils.isExistContent(path)){
                FileUploadManager(token).apply {
                    startUpload(path,fileName)
                    setCallBack{
                        cloudList.add(CloudListBean().apply {
                            type=4
                            subType=-1
                            subTypeStr="日记"
                            year=DateUtils.getYear()
                            date=System.currentTimeMillis()
                            listJson= Gson().toJson(diaryBean)
                            downloadUrl=it
                        })
                        //当加入上传的内容等于全部需要上传时候，则上传
                        if (cloudList.size== diarys.size-nullItems.size){
                            mCloudUploadPresenter.upload(cloudList)
                            uploadType=1
                        }
                    }
                }
            }
            else{
                //没有内容不上传
                nullItems.add(diaryBean)
            }
        }
    }

    /**
     * 每年上传随笔
     */
    fun uploadFreeNote(token:String){
        cloudList.clear()
        val beans=FreeNoteDaoManager.getInstance().queryList()
        val nullItems= mutableListOf<FreeNoteBean>()
        for (item in beans){
            val fileName=DateUtils.longToString(item.date)
            val path=FileAddress().getPathFreeNote(fileName)
            if (FileUtils.isExistContent(path)){
                FileUploadManager(token).apply {
                    startUpload(path,fileName)
                    setCallBack{
                        cloudList.add(CloudListBean().apply {
                            type=5
                            subType=-1
                            subTypeStr="随笔"
                            year=DateUtils.getYear()
                            date=System.currentTimeMillis()
                            listJson= Gson().toJson(item)
                            downloadUrl=it
                        })
                        //当加入上传的内容等于全部需要上传时候，则上传
                        if (cloudList.size== beans.size-nullItems.size){
                            mCloudUploadPresenter.upload(cloudList)
                            uploadType=2
                        }
                    }
                }
            }
            else{
                //没有内容不上传
                nullItems.add(item)
            }
        }
    }

    /**
     * 每年上传截图
     */
    fun uploadScreenShot(token:String){
        cloudList.clear()
        val screenTypes= ItemTypeDaoManager.getInstance().queryAll(3)
        val nullItems= mutableListOf<ItemTypeBean>()
        val itemTypeBean=ItemTypeBean()
        itemTypeBean.title="未分类"
        itemTypeBean.date=System.currentTimeMillis()
        itemTypeBean.path=FileAddress().getPathScreen("未分类")
        screenTypes.add(itemTypeBean)
        for (item in screenTypes){
            val fileName=DateUtils.longToString(item.date)
            val path=item.path
            if (FileUtils.isExistContent(path)){
                FileUploadManager(token).apply {
                    startUpload(path,fileName)
                    setCallBack{
                        cloudList.add(CloudListBean().apply {
                            type=6
                            subType=-1
                            subTypeStr=item.title
                            year=DateUtils.getYear()
                            date=System.currentTimeMillis()
                            listJson= Gson().toJson(item)
                            downloadUrl=it
                        })
                        //当加入上传的内容等于全部需要上传时候，则上传
                        if (cloudList.size== screenTypes.size-nullItems.size){
                            mCloudUploadPresenter.upload(cloudList)
                            uploadType=3
                        }
                    }
                }
            }
            else{
                //没有内容不上传
                nullItems.add(item)
            }
        }
    }

    override fun uploadSuccess(cloudIds: MutableList<Int>?) {
        super.uploadSuccess(cloudIds)
        when(uploadType){
            1->{
                val path=FileAddress().getPathDiary(DateUtils.longToString(System.currentTimeMillis()))
                FileUtils.deleteFile(File(path).parentFile)
                DiaryDaoManager.getInstance().clear()
            }
            2->{
                val path=FileAddress().getPathFreeNote(DateUtils.longToString(System.currentTimeMillis()))
                FileUtils.deleteFile(File(path).parentFile)
                FreeNoteDaoManager.getInstance().clear()
            }
            3->{
                val path=FileAddress().getPathScreen("未分类")
                FileUtils.deleteFile(File(path).parentFile)
                ItemTypeDaoManager.getInstance().clear(3)
            }
        }
    }

}