package com.bll.lnkcommon.dialog


import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.TimePicker
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.mvp.model.DateWeek
import com.bll.lnkcommon.mvp.model.PermissionTimeBean
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PermissionTimeSelectorDialog(
    context: Context,
    private val weekSelects: List<Int>,
    private val item: PermissionTimeBean? = null
) : BaseDialog(context) {

    private var weeks = DataBeanManager.weeks
    private var checkWeeks = mutableListOf<Int>()

    constructor(context: Context, weekSelects: List<Int>) : this(context, weekSelects, null)

    override fun getLayoutResId(): Int = R.layout.dialog_date_time_selector

    override fun initView(contentView: View) {
        // 时间选择器初始化
        val tpStartTime = contentView.findViewById<TimePicker>(R.id.tp_start_time)
        tpStartTime?.setIs24HourView(true)
        val tpEndTime = contentView.findViewById<TimePicker>(R.id.tp_end_time)
        tpEndTime?.setIs24HourView(true)

        // 初始化已有时间
        item?.let {
            val startStr = DateUtils.longToHour2(it.startTime)
            tpStartTime?.hour = startStr.split(":")[0].toInt()
            tpStartTime?.minute = startStr.split(":")[1].toInt()

            val endStr = DateUtils.longToHour2(it.endTime)
            tpEndTime?.hour = endStr.split(":")[0].toInt()
            tpEndTime?.minute = endStr.split(":")[1].toInt()

            val week = it.weeks.split(",")
            week.forEach { num -> checkWeeks.add(num.toInt()) }
        }

        // 初始化星期
        initWeeks()

        // 星期列表
        val rvWeek = contentView.findViewById<RecyclerView>(R.id.rv_week)
        rvWeek?.layoutManager = GridLayoutManager(context, 7)
        val mWeekAdapter = WeekAdapter(R.layout.item_week, weeks)
        rvWeek?.adapter = mWeekAdapter
        mWeekAdapter.bindToRecyclerView(rvWeek)

        // 星期选择事件
        mWeekAdapter.setOnItemChildClickListener { _, view, position ->
            if (view.id == R.id.cb_week) {
                val item = weeks[position]
                item.isCheck = !item.isCheck
                mWeekAdapter.notifyItemChanged(position)
            }
        }


        btnCancel?.setOnClickListener { dismiss() }
        btnOk?.setOnClickListener {
            val startHour = tpStartTime?.hour ?: 0
            val startMinute = tpStartTime?.minute ?: 0
            val startLong = (startHour * 60 + startMinute) * 60 * 1000L

            val endHour = tpEndTime?.hour ?: 0
            val endMinute = tpEndTime?.minute ?: 0
            val endLong = (endHour * 60 + endMinute) * 60 * 1000L

            if (endLong > startLong && getSelectWeeks().size > 0) {
                dateListener?.getDate(startLong, endLong, getSelectWeeks())
                dismiss()
            }
        }
    }

    /**
     * 设置已选星期不可点击
     */
    private fun initWeeks(): MutableList<DateWeek> {
        weekSelects.forEach { select ->
            weeks.forEach { item ->
                if (item.week == select) {
                    if (checkWeeks.contains(select)) {
                        item.isCheck = true
                    } else {
                        item.isSelected = true
                    }
                }
            }
        }
        return weeks
    }

    /**
     * 获取选中的星期
     */
    private fun getSelectWeeks(): MutableList<DateWeek> {
        val selectWeeks = mutableListOf<DateWeek>()
        weeks.forEach { if (it.isCheck) selectWeeks.add(it) }
        return selectWeeks
    }

    // 时间选择监听
    private var dateListener: OnDateListener? = null
    fun interface OnDateListener {
        fun getDate(startLon: Long, endLon: Long, weeks: List<DateWeek>)
    }
    fun setOnDateListener(dateListener: OnDateListener) {
        this.dateListener = dateListener
    }

    override fun builder(): PermissionTimeSelectorDialog {
        super.builder()
        return this
    }

    // 星期适配器
    class WeekAdapter(layoutResId: Int, data: List<DateWeek>?) : BaseQuickAdapter<DateWeek, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: DateWeek) {
            helper.apply {
                setText(R.id.tv_name, item.name)
                if (item.isSelected) {
                    setEnabled(R.id.cb_week, false)
                    setImageResource(R.id.cb_week, R.mipmap.icon_check_focuse)
                } else {
                    setEnabled(R.id.cb_week, true)
                    setImageResource(
                        R.id.cb_week,
                        if (item.isCheck) R.mipmap.icon_check_select else R.mipmap.icon_check_nor
                    )
                    addOnClickListener(R.id.cb_week)
                }
            }
        }
    }
}