package com.bll.lnkcommon.ui.activity

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.PopupDateSelector
import com.bll.lnkcommon.mvp.model.DateBean
import com.bll.lnkcommon.ui.activity.drawing.DateEventActivity
import com.bll.lnkcommon.ui.adapter.DateAdapter
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.date.LunarSolarConverter
import com.bll.lnkcommon.utils.date.Solar
import kotlinx.android.synthetic.main.ac_date.*
import kotlinx.android.synthetic.main.common_title.ll_year
import kotlinx.android.synthetic.main.common_title.tv_month
import kotlinx.android.synthetic.main.common_title.tv_year
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class DateActivity: BaseActivity() {

    private var yearPop:PopupDateSelector?=null
    private var monthPop:PopupDateSelector?=null
    private var yearNow=DateUtils.getYear()
    private var monthNow=DateUtils.getMonth()
    private var mAdapter:DateAdapter?=null
    private var dateBeans= mutableListOf<DateBean>()
    private var position=0
    private val yearList = mutableListOf<Int>().apply {
        val nowYear = DateUtils.getYear()
        repeat(5) { i -> add(nowYear - 5 + i) }
        repeat(5) { i -> add(nowYear + 1 + i) }
    }
    private val monthList = (1..12).toMutableList()

    override fun layoutId(): Int {
        return R.layout.ac_date
    }

    override fun initData() {
    }

    override fun initView() {
        setPageTitle("日历")
        showView(ll_year)

        initRecycler()

        tv_year.text=yearNow.toString()
        tv_month.text=monthNow.toString()

        tv_year.setOnClickListener {
            if (yearPop==null){
                yearPop=PopupDateSelector(this,tv_year,yearList,0)
                yearPop?.builder()
                yearPop?.setOnDateSelectorListener {
                    tv_year.text=it
                    yearNow=it.toInt()
                    loadCalendarData()
                }
                yearPop?.show()
            }
            else{
                yearPop?.show()
            }
        }

        tv_month.setOnClickListener {
            if (monthPop==null){
                monthPop=PopupDateSelector(this,tv_month,monthList,1)
                monthPop?.builder()
                monthPop?.setOnDateSelectorListener {
                    tv_month.text=it
                    monthNow=it.toInt()
                    loadCalendarData()
                }
                monthPop?.show()
            }
            else{
                monthPop?.show()
            }
        }

        loadCalendarData()
    }

    private fun initRecycler(){
        mAdapter = DateAdapter(R.layout.item_date, null)
        rv_list.layoutManager = GridLayoutManager(this,7)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            this.position=position
            val dateBean=dateBeans[position]
            if (dateBean.year!=0){
                val intent = Intent(this, DateEventActivity::class.java)
                intent.putExtra("date",dateBean.time)
                customStartActivity(intent)
            }
        }
    }

    /**
     * 加载日历数据
     */
    private fun loadCalendarData() {
        lifecycleScope.launch {
            val newDateList = withContext(Dispatchers.IO) {
                generateCalendarDates(yearNow, monthNow)
            }
            dateBeans.clear()
            dateBeans.addAll(newDateList)
            mAdapter?.setNewData(dateBeans)
        }
    }

    /**
     * 生成日历数据：提取为纯函数，无副作用，便于测试
     */
    private fun generateCalendarDates(targetYear: Int, targetMonth: Int): MutableList<DateBean> {
        val dateBeans = mutableListOf<DateBean>()
        // 获取当月第一天是周几
        var firstDayWeek = DateUtils.getMonthOneDayWeek(targetYear, targetMonth - 1)
        firstDayWeek = if (firstDayWeek == 1) 8 else firstDayWeek

        // 补齐上月占位（空Date）
        repeat(firstDayWeek - 2) {
            dateBeans.add(DateBean())
        }
        // 添加当月日期
        val monthMaxDay = DateUtils.getMonthMaxDay(targetYear, targetMonth - 1)
        repeat(monthMaxDay) { day ->
            dateBeans.add(createDateBean(targetYear, targetMonth, day + 1))
        }
        // 补齐下月占位（空Date），保证总长度为 35 或 42（7的倍数）
        val targetSize = if (dateBeans.size > 35) 42 else 35
        repeat(targetSize - dateBeans.size) {
            dateBeans.add(DateBean())
        }
        return dateBeans
    }

    /**
     * 创建日期Bean
     */
    private fun createDateBean(year: Int, month: Int, day: Int): DateBean {
        val solar = Solar().apply {
            solarYear = year
            solarMonth = month
            solarDay = day
        }
        val dateTime = DateUtils.dateToStamp("$year-$month-$day")
        return DateBean().apply {
            this.year = year
            this.month = month
            this.day = day
            this.time = dateTime
            this.isNow = (day == DateUtils.getDay() && DateUtils.getMonth() == month)
            this.solar = solar
            this.week = DateUtils.getWeek(dateTime)
            this.lunar = LunarSolarConverter.SolarToLunar(solar)
        }
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag==Constants.DATE_DRAWING_EVENT){
            mAdapter?.notifyItemChanged(position)
        }
    }

}