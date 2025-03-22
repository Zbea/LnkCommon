package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import android.graphics.BitmapFactory
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.Constants.AUTO_REFRESH_EVENT
import com.bll.lnkcommon.Constants.CALENDER_SET_EVENT
import com.bll.lnkcommon.Constants.DATE_DRAWING_EVENT
import com.bll.lnkcommon.Constants.STUDENT_EVENT
import com.bll.lnkcommon.Constants.USER_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.MyApplication
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.DiaryManageDialog
import com.bll.lnkcommon.dialog.DiaryUploadListDialog
import com.bll.lnkcommon.dialog.PopupUpClick
import com.bll.lnkcommon.dialog.PrivacyPasswordCreateDialog
import com.bll.lnkcommon.dialog.PrivacyPasswordDialog
import com.bll.lnkcommon.manager.CalenderDaoManager
import com.bll.lnkcommon.manager.DiaryDaoManager
import com.bll.lnkcommon.manager.FreeNoteDaoManager
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.mvp.model.FreeNoteBean
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.presenter.RelationPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IRelationView
import com.bll.lnkcommon.ui.activity.AccountLoginActivity
import com.bll.lnkcommon.ui.activity.DateActivity
import com.bll.lnkcommon.ui.activity.MessageListActivity
import com.bll.lnkcommon.ui.activity.ScreenshotListActivity
import com.bll.lnkcommon.ui.activity.drawing.DateEventActivity
import com.bll.lnkcommon.ui.activity.drawing.DiaryActivity
import com.bll.lnkcommon.ui.activity.drawing.FreeNoteActivity
import com.bll.lnkcommon.ui.activity.drawing.PlanOverviewActivity
import com.bll.lnkcommon.utils.CalenderUtils
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileUploadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.utils.NetworkUtil
import com.bll.lnkcommon.utils.SPUtil
import com.bll.lnkcommon.utils.date.LunarSolarConverter
import com.bll.lnkcommon.utils.date.Solar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.iv_bg
import kotlinx.android.synthetic.main.fragment_home.iv_calender
import kotlinx.android.synthetic.main.fragment_home.iv_change
import kotlinx.android.synthetic.main.fragment_home.iv_date
import kotlinx.android.synthetic.main.fragment_home.iv_message_tips
import kotlinx.android.synthetic.main.fragment_home.ll_date
import kotlinx.android.synthetic.main.fragment_home.ll_diary
import kotlinx.android.synthetic.main.fragment_home.ll_freenote
import kotlinx.android.synthetic.main.fragment_home.ll_message
import kotlinx.android.synthetic.main.fragment_home.ll_plan
import kotlinx.android.synthetic.main.fragment_home.ll_screenshot
import kotlinx.android.synthetic.main.fragment_home.tv_date_day
import kotlinx.android.synthetic.main.fragment_home.tv_date_festival
import kotlinx.android.synthetic.main.fragment_home.tv_date_luna
import kotlinx.android.synthetic.main.fragment_home.tv_date_month
import kotlinx.android.synthetic.main.fragment_home.tv_date_week
import kotlinx.android.synthetic.main.fragment_home.v_down
import kotlinx.android.synthetic.main.fragment_home.v_up
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

class MainFragment:BaseFragment(),IRelationView{

    private val presenter=RelationPresenter(this)
    private var nowDayPos=1
    private var nowDay=0L
    private var calenderPath=""
    private var uploadType=0//上传类型
    private var isChange=false
    private var isShow=false//是否存在台历
    private var privacyPassword=MethodManager.getPrivacyPassword(0)
    private var diaryStartLong=0L
    private var diaryEndLong=0L
    private var diaryUploadTitleStr=""

    override fun onListStudents(list: MutableList<StudentBean>) {
        if (list.size>0){
            showView(ll_message)
        }
        else{
            disMissView(ll_message)
        }
        if (DataBeanManager.students != list){
            DataBeanManager.students=list
            EventBus.getDefault().post(STUDENT_EVENT)
        }
    }

    override fun onMessageTotal(total: Int) {
        if (total>SPUtil.getInt("messageTotal")){
            showView(iv_message_tips)
        }
        SPUtil.putInt("messageTotal",total)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }
    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[0])
        MethodManager.setImageResource(requireActivity(), R.mipmap.icon_date_event_bg,iv_bg)

        ll_message.setOnClickListener {
            if (MethodManager.isLogin()){
                disMissView(iv_message_tips)
                customStartActivity(Intent(requireActivity(),MessageListActivity::class.java))
            }
            else{
                customStartActivity(Intent(requireActivity(),AccountLoginActivity::class.java))
            }
        }

        ll_date.setOnClickListener {
            customStartActivity(Intent(activity,DateActivity::class.java))
        }

        ll_diary.setOnClickListener {
            if (!MethodManager.isLogin()){
                customStartActivity(Intent(requireActivity(),AccountLoginActivity::class.java))
                return@setOnClickListener
            }
            startDiaryActivity(0)
        }

        ll_diary.setOnLongClickListener {
            if (!MethodManager.isLogin()){
                customStartActivity(Intent(requireActivity(),AccountLoginActivity::class.java))
               return@setOnLongClickListener true
            }
            onLongDiary()
            return@setOnLongClickListener true
        }

        ll_freenote.setOnClickListener {
            customStartActivity(Intent(requireActivity(),FreeNoteActivity::class.java))
        }

        ll_plan.setOnClickListener {
            customStartActivity(Intent(requireActivity(),PlanOverviewActivity::class.java))
        }

        ll_screenshot.setOnClickListener {
            if (MethodManager.isLogin()){
                customStartActivity(Intent(requireActivity(),ScreenshotListActivity::class.java))
            }
            else{
                customStartActivity(Intent(requireActivity(),AccountLoginActivity::class.java))
            }
        }

        iv_date.setOnClickListener {
            val intent = Intent(requireActivity(), DateEventActivity::class.java)
            intent.putExtra("date",nowDay)
            customStartActivity(intent)
        }

        v_up.setOnClickListener{
            nowDay-=Constants.dayLong
            setDateView()
            if (isShow&&nowDayPos>1){
                nowDayPos-=1
                setCalenderBg()
            }
        }

        v_down.setOnClickListener {
            nowDay+=Constants.dayLong
            setDateView()
            val allDay=if (DateUtils().isYear(DateUtils.getYear())) 366 else 365
            if (isShow&&nowDayPos<=allDay){
                nowDayPos+=1
                setCalenderBg()
            }
        }

        iv_change.setOnClickListener {
            if (!MethodManager.isLogin())
                return@setOnClickListener
            isChange=!isChange
            if (isChange){
                showView(iv_calender)
            }
            else{
                disMissView(iv_calender)
            }
        }

        iv_change.setOnLongClickListener {
            val boolean=SPUtil.getBoolean("isShowCalender")
            val titleStr=if (boolean) "默认显示日程？" else "默认显示台历？"
            CommonDialog(requireActivity()).setContent(titleStr).builder().onDialogClickListener= object : CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    if (boolean){
                        SPUtil.putBoolean("isShowCalender",false)
                        disMissView(iv_calender)
                    }
                    else{
                        SPUtil.putBoolean("isShowCalender",true)
                        showView(iv_calender)
                    }
                }
            }
            return@setOnLongClickListener true
        }

    }
    override fun lazyLoad() {
        onCheckUpdate()
        if (NetworkUtil.isNetworkConnected()) {
            if (MethodManager.isLogin()){
                presenter.getStudents()
                presenter.getMessageTotal()
            }
        }
        nowDay=DateUtils.getStartOfDayInMillis()
        setDateView()
        showCalenderView()
        setMessageView()
    }

    private fun setMessageView(){
        if (MethodManager.isLogin()&&DataBeanManager.students.size>0){
            showView(ll_message)
        }
        else{
            disMissView(ll_message)
        }
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
//            GlideUtils.setImageNoCacheUrl(activity,path,iv_date)
            val myBitmap= BitmapFactory.decodeFile(path)
            iv_date.setImageBitmap(myBitmap)
        }
        else{
            iv_date.setImageResource(0)
        }
    }

    /**
     * 是否显示台历
     */
    private fun showCalenderView(){
        val item=CalenderDaoManager.getInstance().queryCalenderBean()
        isShow=item!=null
        if (isShow){
            calenderPath=item.path
            showView(iv_change,iv_calender)
            setCalenderView()
        }
        else{
            isChange=false
            disMissView(iv_change,iv_calender)
        }
    }

    /**
     * 设置台历内容
     */
    private fun setCalenderView(){
        if (isShow){
            val calenderUtils=CalenderUtils(DateUtils.longToStringDataNoHour(nowDay))
            nowDayPos=calenderUtils.elapsedTime()
            setCalenderBg()
            if (SPUtil.getBoolean("isShowCalender"))
            {
                isChange=true
                showView(iv_calender)
            }
            else{
                isChange=false
                disMissView(iv_calender)
            }
        }
    }

    /**
     * 设置台历图片
     */
    private fun setCalenderBg(){
        val listFiles= FileUtils.getFiles(calenderPath)
        if (listFiles.size>0){
            val file=if (listFiles.size>nowDayPos-1){
                listFiles[nowDayPos-1]
            }
            else{
//                listFiles[listFiles.size-1]
                listFiles[Random().nextInt(listFiles.size)]
            }
            GlideUtils.setImageRoundUrl(requireActivity(),file.path,iv_calender,15)
        }
    }

    /**
     * 日记跳转
     */
    private fun startDiaryActivity(typeId:Int){
        if (privacyPassword!=null&&privacyPassword?.isSet==true){
            PrivacyPasswordDialog(requireActivity()).builder().setOnDialogClickListener{
                customStartActivity(Intent(activity,DiaryActivity::class.java).setFlags(typeId))
            }
        }
        else{
            customStartActivity(Intent(activity,DiaryActivity::class.java).setFlags(typeId))
        }
    }

    /**
     * 日记长按管理
     */
    private fun onLongDiary(){
        val pops= mutableListOf<PopupBean>()
        if (privacyPassword==null){
            pops.add(PopupBean(1,"设置密码"))
        }
        else{
            if (privacyPassword?.isSet==true){
                pops.add(PopupBean(1,"取消密码"))
            }
            else{
                pops.add(PopupBean(1,"设置密码"))
            }
        }
        pops.add(PopupBean(2,"结集保存"))
        pops.add(PopupBean(3,"云库日记"))
        PopupUpClick(requireActivity(),pops,ll_diary,160,(ll_diary.width-160)/2,-ll_diary.height).builder().setOnSelectListener{
            when(it.id){
                1->{
                    if (privacyPassword==null){
                        PrivacyPasswordCreateDialog(requireActivity()).builder().setOnDialogClickListener{
                            privacyPassword=it
                            showToast("日记密码设置成功")
                        }
                    }
                    else{
                        val titleStr=if (privacyPassword?.isSet==true) "确定取消密码？" else "确定设置密码？"
                        CommonDialog(requireActivity()).setContent(titleStr).builder().setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                            override fun cancel() {
                            }
                            override fun ok() {
                                PrivacyPasswordDialog(requireActivity()).builder().setOnDialogClickListener{
                                    privacyPassword!!.isSet=!privacyPassword!!.isSet
                                    MethodManager.savePrivacyPassword(0,privacyPassword)
                                }
                            }
                        })
                    }
                }
                2->{
                    DiaryManageDialog(requireActivity(),1).builder().setOnDialogClickListener{
                            titleStr,startLong,endLong->
                        diaryStartLong=startLong
                        diaryEndLong=endLong
                        diaryUploadTitleStr=titleStr
                        if (NetworkUtil.isNetworkConnected()){
                            mQiniuPresenter.getToken()
                        }
                        else{
                            showToast("网络连接失败")
                        }
                    }
                }
                3->{
                    DiaryUploadListDialog(requireActivity()).builder().setOnDialogClickListener{ typeId->
                        startDiaryActivity(typeId)
                    }
                }
            }
        }
    }

    override fun onEventBusMessage(msgFlag: String) {
        when (msgFlag) {
            USER_EVENT->{
                privacyPassword=MethodManager.getPrivacyPassword(0)
                lazyLoad()
            }
            STUDENT_EVENT->{
                setMessageView()
            }
            DATE_DRAWING_EVENT -> {
                setDateView()
            }
            CALENDER_SET_EVENT->{
                showCalenderView()
            }
            AUTO_REFRESH_EVENT->{
                nowDay=DateUtils.getStartOfDayInMillis()
                setDateView()
                showCalenderView()
            }
        }
    }

    override fun onRefreshData() {
        lazyLoad()
    }

    override fun onUpload(token: String) {
        cloudList.clear()
        val diarys=DiaryDaoManager.getInstance().queryList(diaryStartLong,diaryEndLong)
        if (diarys.isNotEmpty()){
            val paths= mutableListOf<String>()
            for (item in diarys){
                paths.add(FileAddress().getPathDiary(DateUtils.longToStringCalender(item.date)))
            }
            val time=System.currentTimeMillis()
            FileUploadManager(token).apply {
                startUpload(paths,DateUtils.longToString(time))
                setCallBack{
                    cloudList.add(CloudListBean().apply {
                        type=4
                        title=diaryUploadTitleStr
                        subTypeStr="我的日记"
                        year=DateUtils.getYear()
                        date=time
                        listJson= Gson().toJson(diarys)
                        downloadUrl=it
                    })
                    mCloudUploadPresenter.upload(cloudList)
                    uploadType=1
                }
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
        val currentTime=System.currentTimeMillis()
        val itemTypeBean=ItemTypeBean()
        itemTypeBean.title="未分类"+DateUtils.longToStringDataNoYear(currentTime)
        itemTypeBean.date=currentTime
        itemTypeBean.path=FileAddress().getPathScreen("未分类")
        screenTypes.add(0,itemTypeBean)
        for (item in screenTypes){
            val fileName=DateUtils.longToString(item.date)
            val path=item.path
            if (FileUtils.isExistContent(path)){
                FileUploadManager(token).apply {
                    startUpload(path,fileName)
                    setCallBack{
                        cloudList.add(CloudListBean().apply {
                            type=6
                            subTypeStr="截图"
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
                val diarys=DiaryDaoManager.getInstance().queryList(diaryStartLong,diaryEndLong)
                for (item in diarys){
                    val path=FileAddress().getPathDiary(DateUtils.longToStringCalender(item.date))
                    FileUtils.deleteFile(File(path))
                    DiaryDaoManager.getInstance().delete(item)
                }
                showToast("日记上传成功")
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