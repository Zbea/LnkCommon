package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.widget.CalendarView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DateUtils


class DateCalendarDialog(private val context: Context,private val x:Float,private val y:Float){

    private var dialog:Dialog?=null

    constructor(context: Context):this(context,50f,150f)

    fun builder(): DateCalendarDialog {
        dialog= Dialog(context)
        dialog?.setContentView(R.layout.dialog_calendar)
        val window=dialog?.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        val layoutParams = window.attributes
        layoutParams.gravity = Gravity.TOP or  Gravity.END
        layoutParams.y= DP2PX.dip2px(context,y)
        layoutParams.x=DP2PX.dip2px(context,x)
        dialog?.show()

        val calendarView = dialog?.findViewById<CalendarView>(R.id.dp_date)
        calendarView?.firstDayOfWeek=2
        calendarView?.setOnDateChangeListener { calendarView, i, i2, i3 ->
            dismiss()
            val dateToStamp = "${i}-${i2+1}-${i3}"
            val time = DateUtils.dateToStamp(dateToStamp)
            dateListener?.getDate(time)
        }
        return this
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