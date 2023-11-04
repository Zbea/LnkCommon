package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import android.widget.TextView
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

        tv_date.setOnClickListener {
            DateDialog(context).builder().setOnDateListener { dateStr, dateTim ->
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
            if (date>0&&date<=System.currentTimeMillis()){
                SToast.showText(R.string.toast_commit_time_error)
                return@setOnClickListener
            }
            listener?.onSend(contentStr,date)
            dialog.dismiss()
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