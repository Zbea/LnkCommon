package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import android.widget.TimePicker
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.DateWeek
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.text.SimpleDateFormat
import java.util.*


class PermissionTimeSelectorDialog(private val context: Context,private val weekSelects:List<Int>) {
    private var dialog:Dialog?=null
    private var weeks= DataBeanManager.weeks

    fun builder(): PermissionTimeSelectorDialog {
        dialog =Dialog(context)
        dialog?.setContentView(R.layout.dialog_date_time_selector)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val yearMonth=simpleDateFormat.format(Date())

        val tp_start_time = dialog?.findViewById<TimePicker>(R.id.tp_start_time)
        tp_start_time?.setIs24HourView(true)
//        if (!item.startTimeStr.isNullOrEmpty()){
//            tp_start_time?.hour=item.startTimeStr.split(":")[0].toInt()
//            tp_start_time?.minute=item.startTimeStr.split(":")[1].toInt()
//        }

        val tp_end_time = dialog?.findViewById<TimePicker>(R.id.tp_end_time)
        tp_end_time?.setIs24HourView(true)
//        if (!item.endTimeStr.isNullOrEmpty()){
//            tp_end_time?.hour=item.endTimeStr.split(":")[0].toInt()
//            tp_end_time?.minute=item.endTimeStr.split(":")[1].toInt()
//        }

        initWeeks()

        val rv_week = dialog?.findViewById<RecyclerView>(R.id.rv_week)
        rv_week?.layoutManager = GridLayoutManager(context,7) //创建布局管理
        val mWeekAdapter = WeekAdapter(R.layout.item_week, weeks)
        rv_week?.adapter = mWeekAdapter
        mWeekAdapter.bindToRecyclerView(rv_week)
        mWeekAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item=weeks[position]
            if (view.id==R.id.cb_week){
                item.isCheck=!item.isCheck
                mWeekAdapter.notifyItemChanged(position)
            }
        }

        val cancleTv = dialog?.findViewById<TextView>(R.id.tv_cancel)
        val okTv = dialog?.findViewById<TextView>(R.id.tv_ok)

        cancleTv?.setOnClickListener { dismiss() }
        okTv?.setOnClickListener {

            val startHour=tp_start_time?.hour
            val startMinute=tp_start_time?.minute
            val startStr="${getFormat(startHour!!)}:${getFormat(startMinute!!)}"
            val startLong= DateUtils.date3Stamp("$yearMonth $startStr")-DateUtils.getStartOfDayInMillis()

            val endHour=tp_end_time?.hour
            val endMinute=tp_end_time?.minute
            val endStr="${getFormat(endHour!!)}:${getFormat(endMinute!!)}"
            val endLong=DateUtils.date3Stamp("$yearMonth $endStr")-DateUtils.getStartOfDayInMillis()

            if (endLong>startLong&&getSelectWeeks().size>0){
                dateListener?.getDate(startStr,startLong,endStr,endLong,getSelectWeeks())
                dismiss()
            }
        }
        return this
    }

    /**
     * 设置已选星期不可以点击
     */
    private fun initWeeks():MutableList<DateWeek>{
        for (i in weekSelects){
            for (item in weeks){
                if (item.week==i){
                    item.isSelected=true
                }
            }
        }
        return weeks
    }

    /**
     * 获取选中的星期
     */
    private fun getSelectWeeks():MutableList<DateWeek>{
        val selectWeeks= mutableListOf<DateWeek>()
        for (item in weeks){
            if (item.isCheck)
                selectWeeks.add(item)
        }
        return selectWeeks
    }

    /**
     * 格式化时间
     */
    private fun getFormat(num:Int):String{
        return if (num<10) "0$num" else "$num"
    }

    fun show() {
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    class WeekAdapter(layoutResId: Int, data: List<DateWeek>?) :
        BaseQuickAdapter<DateWeek, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: DateWeek) {
            helper.apply {
                setText(R.id.tv_name,item.name)
                if (item.isSelected){
                    setEnabled(R.id.cb_week,false)
                    setImageResource(R.id.cb_week,R.mipmap.icon_check_focuse)
                }
                else{
                    setEnabled(R.id.cb_week,true)
                    setImageResource(R.id.cb_week,if (item.isCheck) R.mipmap.icon_check_select else R.mipmap.icon_check_nor)
                    addOnClickListener(R.id.cb_week)
                }
            }
        }
    }


    private var dateListener: OnDateListener? = null

    fun interface OnDateListener {
        fun getDate(startStr: String,startLon: Long,endStr: String,endLon:Long,weeks:List<DateWeek>)
    }

    fun setOnDateListener(dateListener:OnDateListener) {
        this.dateListener = dateListener
    }

}