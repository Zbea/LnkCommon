package com.bll.lnkcommon.ui.activity

import android.view.EinkPWInterface
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.Constants.dayLong
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.DateDialog
import com.bll.lnkcommon.mvp.model.Date
import com.bll.lnkcommon.utils.DateUtils
import kotlinx.android.synthetic.main.ac_date_event.*
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class DateEventActivity:BaseActivity() {
    private var mDate: Date?=null
    private var nowLong=0L
    private var elik: EinkPWInterface?=null

    override fun layoutId(): Int {
        return R.layout.ac_date_event
    }

    override fun initData() {
        mDate = intent.getBundleExtra("bundle")?.getSerializable("dateBean") as Date
        nowLong=mDate?.time!!
    }

    override fun initView() {
        setPageTitle("日程")
        elik = v_content.pwInterFace
        setContentView()

        iv_up.setOnClickListener {
            nowLong-=dayLong
            setContentView()
        }

        iv_down.setOnClickListener {
            nowLong+=dayLong
            setContentView()
        }

        tv_date.setOnClickListener {
            DateDialog(this).builder().setOnDateListener { dateStr, dateTim ->
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
        EventBus.getDefault().post(Constants.DATE_DRAWING_EVENT)
    }

}