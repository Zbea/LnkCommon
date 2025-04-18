package com.bll.lnkcommon.ui.activity.drawing

import android.os.Handler
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.Constants.dayLong
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.CalendarSingleDialog
import com.bll.lnkcommon.mvp.model.Date
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.ToolUtils
import kotlinx.android.synthetic.main.ac_date_event.*
import kotlinx.android.synthetic.main.ac_plan_overview.v_content
import kotlinx.android.synthetic.main.common_date_arrow.iv_down
import kotlinx.android.synthetic.main.common_date_arrow.iv_up
import kotlinx.android.synthetic.main.common_date_arrow.tv_date
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class DateEventActivity:BaseDrawingActivity() {
    private var nowLong=0L

    override fun layoutId(): Int {
        return R.layout.ac_date_event
    }

    override fun initData() {
        nowLong=intent.getLongExtra("date",0)
    }

    override fun initView() {
        setPageTitle("日程")
        MethodManager.setImageResource(this, R.mipmap.icon_date_event_bg,v_content)


        setContentView()
        elik=v_content.pwInterFace

        iv_up.setOnClickListener {
            nowLong-=dayLong
            setContentView()
        }

        iv_down.setOnClickListener {
            nowLong+=dayLong
            setContentView()
        }

        tv_date.setOnClickListener {
            CalendarSingleDialog(this,45f,190f).builder().setOnDateListener { dateTim ->
                nowLong=dateTim
                setContentView()
            }
        }

    }

    private fun setContentView(){
        tv_date.text= SimpleDateFormat("MM月dd日 E", Locale.CHINA).format(Date(nowLong))
        val path=FileAddress().getPathDate(DateUtils.longToStringCalender(nowLong))+"/draw.png"
        elik?.setLoadFilePath(path, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopErasure()
        Handler().postDelayed({
            EventBus.getDefault().post(Constants.DATE_DRAWING_EVENT)
        },100)
    }

}