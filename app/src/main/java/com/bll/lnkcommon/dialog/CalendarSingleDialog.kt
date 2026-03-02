package com.bll.lnkcommon.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.utils.DateUtils
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView

/**
 * 单日历选择弹窗
 * @param context 上下文
 * @param x X轴偏移（dp），0则使用基类默认值
 * @param y Y轴偏移（dp），0则使用基类默认值
 */
class CalendarSingleDialog(context: Context, private val x: Float = 0f, private val y: Float = 0f) : BaseDialog(context) {

    private var calendarView: CalendarView? = null

    private var dateListener: OnDateListener? = null

    override val defaultGravity: Int
        get() = if (x != 0f && y != 0f) Gravity.TOP or Gravity.END else super.defaultGravity

    override val defaultXOffset: Float
        get() = if (x != 0f && y != 0f) x else super.defaultXOffset

    override val defaultYOffset: Float
        get() = if (x != 0f && y != 0f) y else super.defaultYOffset


    override fun getLayoutResId(): Int = R.layout.dialog_calendar_single

    @SuppressLint("SetTextI18n")
    override fun initView(contentView: View) {

        val tvYear = contentView.findViewById<TextView>(R.id.tv_year)
        val ivLeft = contentView.findViewById<ImageView>(R.id.iv_left)
        val ivRight = contentView.findViewById<ImageView>(R.id.iv_right)
        calendarView = contentView.findViewById(R.id.dp_date)

        tvYear?.text = "${calendarView?.curYear} 年  ${calendarView?.curMonth} 月"

        ivLeft?.setOnClickListener {
            calendarView?.scrollToPre()
        }

        ivRight?.setOnClickListener {
            calendarView?.scrollToNext()
        }

        calendarView?.setOnMonthChangeListener { year, month ->
            tvYear?.text = "$year 年  $month 月"
        }

        calendarView?.setOnCalendarSelectListener(object : CalendarView.OnCalendarSelectListener {
            override fun onCalendarOutOfRange(calendar: Calendar?) {}
            override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
                if (isClick) {
                    val year = calendar?.year ?: return
                    val month = calendar?.month ?: return
                    val day = calendar?.day ?: return
                    // 日期转时间戳
                    val dateToStamp = "${year}-${month}-${day}"
                    val time = DateUtils.dateToStamp(dateToStamp)
                    // 触发回调
                    dateListener?.getDate(time)
                    dismiss()
                }
            }
        })
    }

    fun interface OnDateListener {
        fun getDate(dateTim: Long)
    }

    fun setOnDateListener(listener: OnDateListener?): CalendarSingleDialog {
        this.dateListener = listener
        return this
    }

    //保持链式调用连贯性
    override fun builder(): CalendarSingleDialog {
        super.builder()
        return this
    }
}