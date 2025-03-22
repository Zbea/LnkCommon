package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.SToast

class HomeworkPublishDialog(val context: Context) {
    private var date=0L

    fun builder(): HomeworkPublishDialog {

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_homework_publish)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val tv_send = dialog.findViewById<TextView>(R.id.tv_send)
        val tv_date = dialog.findViewById<TextView>(R.id.tv_date)
        val etContent = dialog.findViewById<EditText>(R.id.et_content)

        date=System.currentTimeMillis()+ Constants.dayLong
        tv_date.text=DateUtils.longToStringWeek(date)
        tv_date.setOnClickListener {
            CalendarSingleDialog(context).builder().setOnDateListener {  dateTim ->
                tv_date.text=DateUtils.longToStringWeek(dateTim)
                date=dateTim
            }
        }

        tv_send.setOnClickListener {
            val contentStr = etContent.text.toString()
            if (contentStr.isEmpty()){
                SToast.showText(R.string.toast_input_content)
                return@setOnClickListener
            }
            if (date>System.currentTimeMillis()){
                listener?.onSend(contentStr,date)
                dialog.dismiss()
            }
            else{
                SToast.showText(R.string.toast_commit_time_error)
            }
        }

        dialog.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }

        return this
    }


    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onSend(contentStr:String,date:Long)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

}