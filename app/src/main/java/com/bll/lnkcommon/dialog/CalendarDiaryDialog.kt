package com.bll.lnkcommon.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.manager.DiaryDaoManager
import com.bll.lnkcommon.mvp.model.DiaryBean
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.SToast
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView


class CalendarDiaryDialog(private val context: Context) {

    private var dialog: Dialog? = null
    private var diaryTimes= mutableListOf<Long>()
    private var calendarView:CalendarView?=null
    private val currentDay=DateUtils.getStartOfDayInMillis()

    @SuppressLint("SetTextI18n")
    fun builder(): CalendarDiaryDialog {
        dialog = Dialog(context)
        dialog?.setContentView(R.layout.dialog_calendar_single)
        val window = dialog?.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        val layoutParams = window.attributes
        layoutParams.gravity = Gravity.TOP or Gravity.END
        layoutParams.x = DP2PX.dip2px(context, 50f)
        layoutParams.y = DP2PX.dip2px(context, 150f)
        dialog?.show()

        val tv_year = dialog?.findViewById<TextView>(R.id.tv_year)
        val iv_left = dialog?.findViewById<ImageView>(R.id.iv_left)
        val iv_right = dialog?.findViewById<ImageView>(R.id.iv_right)
        calendarView = dialog!!.findViewById(R.id.dp_date)

        tv_year?.text="${calendarView?.curYear} 年  ${calendarView?.curMonth} 月"
        diaryTimes=DiaryDaoManager.getInstance().queryLongList(calendarView!!.curYear, calendarView!!.curMonth)
        if (!diaryTimes.contains(currentDay)){
            diaryTimes.add(currentDay)
        }
//        initCalendar()

        iv_left?.setOnClickListener {
            calendarView?.scrollToPre()
        }

        iv_right?.setOnClickListener {
            calendarView?.scrollToNext()
        }

        calendarView?.setOnMonthChangeListener { year, month ->
            tv_year?.text="$year 年  $month 月"
            diaryTimes=DiaryDaoManager.getInstance().queryLongList(calendarView!!.curYear, calendarView!!.curMonth)
            if (month==DateUtils.getMonth()){
                if (!diaryTimes.contains(currentDay)){
                    diaryTimes.add(currentDay)
                }
            }
//            initCalendar()
        }

        calendarView?.setOnCalendarSelectListener(object : CalendarView.OnCalendarSelectListener {
            override fun onCalendarOutOfRange(calendar: Calendar?) {
            }

            override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
                if (isClick){
                    val years = calendar?.year
                    val months = calendar?.month
                    val day = calendar?.day
                    val dateToStamp = "${years}-${months}-${day}"
                    val time = DateUtils.dateToStamp(dateToStamp)
                    dateListener?.getDate(time)
                    dismiss()
                }
            }
        })

        calendarView?.setOnCalendarInterceptListener(object : CalendarView.OnCalendarInterceptListener {
            override fun onCalendarIntercept(calendar: Calendar?): Boolean {
                val year = calendar?.year!!
                val month = calendar.month
                val day = calendar.day
                val time=DateUtils.dateToStamp(year,month,day)
                return !diaryTimes.contains(time)
            }

            override fun onCalendarInterceptClick(calendar: Calendar?, isClick: Boolean) {
                SToast.showText("当前日期无法选择")
            }
        })

        return this
    }

    private fun initCalendar(){
        for (date in diaryTimes){
            val years=DateUtils.longToStringDataNoHour(date).split("-")
            val calendar=Calendar()
            calendar.year=years[0].toInt()
            calendar.month=years[1].toInt()
            calendar.day=years[2].toInt()
            calendarView!!.putMultiSelect(calendar)
        }
    }



    fun show() {
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    private var dateListener: OnDateListener? = null

    fun interface OnDateListener {
        fun getDate(dateTim: Long)
    }

    fun setOnDateListener(dateListener: OnDateListener?) {
        this.dateListener = dateListener
    }


}