package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.SToast

import com.bll.lnkcommon.base.BaseDialog

/**
 * 作业发布弹窗：继承BaseDialog
 * @param context 上下文
 */
class HomeworkPublishDialog(context: Context) : BaseDialog(context) {

    private var date = 0L

    private var onDialogClickListener: OnDialogClickListener? = null

    override fun getLayoutResId(): Int = R.layout.dialog_homework_publish

    override fun initView(contentView: View) {

        val tvSend = contentView.findViewById<TextView>(R.id.tv_send)
        val tvDate = contentView.findViewById<TextView>(R.id.tv_date)
        val etContent = contentView.findViewById<EditText>(R.id.et_content)

        date = System.currentTimeMillis() + Constants.dayLong
        tvDate.text = DateUtils.longToStringWeek(date)

        tvDate.setOnClickListener {
            CalendarSingleDialog(context).builder().setOnDateListener { dateTim ->
                    tvDate.text = DateUtils.longToStringWeek(dateTim)
                    date = dateTim
                }
        }

        tvSend.setOnClickListener {
            val contentStr = etContent.text.toString().trim()
            when {
                contentStr.isEmpty() -> SToast.showText(R.string.toast_input_content)
                date <= System.currentTimeMillis() -> SToast.showText(R.string.toast_commit_time_error)
                else -> {
                    onDialogClickListener?.onSend(contentStr, date)
                    dismiss()
                }
            }
        }

        dialog?.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }
    }

    fun interface OnDialogClickListener {
        fun onSend(contentStr: String, date: Long)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): HomeworkPublishDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): HomeworkPublishDialog {
        super.builder()
        return this
    }

}