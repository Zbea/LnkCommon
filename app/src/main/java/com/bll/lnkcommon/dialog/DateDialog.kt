package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.DatePicker
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.DateUtils


class DateDialog(private val context: Context){

    private var dialog:Dialog?=null

    fun builder(): DateDialog {
        dialog= Dialog(context)
        dialog?.setContentView(R.layout.dialog_date)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()

        val cancleTv = dialog?.findViewById<TextView>(R.id.tv_cancel)
        val okTv = dialog?.findViewById<TextView>(R.id.tv_ok)
        val mDatePicker = dialog?.findViewById<DatePicker>(R.id.dp_date)

        cancleTv?.setOnClickListener { dismiss() }
        okTv?.setOnClickListener {
            dismiss()
            val year = mDatePicker?.year
            val month = mDatePicker?.month?.plus(1)
            val dayOfMonth = mDatePicker?.dayOfMonth
            val time = "${year}-${month}-${dayOfMonth}"
            val dateToStamp = DateUtils.dateToStamp(year!!,month!!,dayOfMonth!!)
            dateListener?.getDate(time, dateToStamp)
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
        fun getDate(dateStr: String?, dateTim: Long)
    }

    fun setOnDateListener(dateListener: OnDateListener?) {
        this.dateListener = dateListener
    }


}