package com.bll.lnkcommon.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.manager.DiaryDaoManager
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.SToast
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView

/**
 * 日历日记弹窗
 * @param context 上下文
 * @param uploadId 上传ID（用于筛选日记日期）
 */
class CalendarDiaryDialog(context: Context, private val uploadId: Int) : BaseDialog(context) {

    private val diaryTimes = mutableListOf<Long>()
    private val currentDay = DateUtils.getStartOfDayInMillis()

    private var calendarView: CalendarView? = null

    private var dateListener: OnDateListener? = null

    override val defaultGravity: Int = Gravity.TOP or Gravity.END // 右上位置
    override val defaultXOffset: Float = 50f // X偏移50dp
    override val defaultYOffset: Float = 150f // Y偏移150dp

    override fun getLayoutResId(): Int = R.layout.dialog_calendar_single

    @SuppressLint("SetTextI18n")
    override fun initView(contentView: View) {

        val tvYear = contentView.findViewById<TextView>(R.id.tv_year)
        val ivLeft = contentView.findViewById<ImageView>(R.id.iv_left)
        val ivRight = contentView.findViewById<ImageView>(R.id.iv_right)
        calendarView = contentView.findViewById(R.id.dp_date)

        tvYear?.text = "${calendarView?.curYear} 年  ${calendarView?.curMonth} 月"

        initDiaryTimes(calendarView?.curYear ?: 0, calendarView?.curMonth ?: 0)

        ivLeft?.setOnClickListener {
            calendarView?.scrollToPre()
        }

        ivRight?.setOnClickListener {
            calendarView?.scrollToNext()
        }

        calendarView?.setOnMonthChangeListener { year, month ->
            tvYear?.text = "$year 年  $month 月"
            // 重新查询当月日记日期
            initDiaryTimes(year, month)
        }

        calendarView?.setOnCalendarSelectListener(object : CalendarView.OnCalendarSelectListener {
            override fun onCalendarOutOfRange(calendar: Calendar?) {}
            override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
                if (isClick) {
                    val year = calendar?.year ?: return
                    val month = calendar.month ?: return
                    val day = calendar.day ?: return
                    // 日期转时间戳
                    val dateToStamp = "${year}-${month}-${day}"
                    val time = DateUtils.dateToStamp(dateToStamp)
                    // 触发日期选择回调
                    dateListener?.getDate(time)
                    dismiss()
                }
            }
        })

        // 日历拦截监听（不可选日期提示）
        calendarView?.setOnCalendarInterceptListener(object : CalendarView.OnCalendarInterceptListener {
            override fun onCalendarIntercept(calendar: Calendar?): Boolean {
                val year = calendar?.year ?: return true
                val month = calendar?.month ?: return true
                val day = calendar?.day ?: return true
                val time = DateUtils.dateToStamp(year, month, day)
                // 不可选条件：不在日记日期列表中
                return !diaryTimes.contains(time)
            }
            override fun onCalendarInterceptClick(calendar: Calendar?, isClick: Boolean) {
                SToast.showText("当前日期无法选择")
            }
        })
    }

    private fun initDiaryTimes(year: Int, month: Int) {
        // 查询指定年月+uploadId的日记日期
        diaryTimes.clear()
        diaryTimes.addAll(DiaryDaoManager.getInstance().queryLongList(year, month, uploadId))
        // 特殊逻辑：uploadId=0时，添加当天日期
        if (uploadId == 0) {
            if (!diaryTimes.contains(currentDay)) {
                diaryTimes.add(currentDay)
            }
        }
    }

    fun interface OnDateListener {
        fun getDate(dateTim: Long)
    }

    fun setOnDateListener(listener: OnDateListener?): CalendarDiaryDialog {
        this.dateListener = listener
        return this
    }

    override fun builder(): CalendarDiaryDialog {
        super.builder()
        return this
    }

    override fun release() {
        super.release()
        dateListener = null
        calendarView = null
        diaryTimes.clear()
    }
}