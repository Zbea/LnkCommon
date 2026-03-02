package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.KeyboardUtils

/**
 * 日记管理弹窗（上传/删除）
 * @param context 上下文
 * @param type 类型（1=上传日记，其他=删除日记）
 */
class DiaryManageDialog(context: Context, private val type: Int) : BaseDialog(context) {

    var startLong = 0L
    var endLong = 0L

    private var onDialogClickListener: OnDialogClickListener? = null

    override fun getLayoutResId(): Int = R.layout.dialog_diary_upload

    override fun initView(contentView: View) {

        val tvTitle = contentView.findViewById<TextView>(R.id.tv_title)
        val etName = contentView.findViewById<EditText>(R.id.et_name)
        val tvStartDate = contentView.findViewById<TextView>(R.id.tv_start_date)
        val tvEndDate = contentView.findViewById<TextView>(R.id.tv_end_date)

        tvTitle.text = if (type == 1) "上传日记" else "删除日记"

        etName?.visibility = if (type == 1) View.VISIBLE else View.GONE

        tvStartDate?.setOnClickListener {
            CalendarSingleDialog(context, 310f, 480f).builder().setOnDateListener { time ->
                    startLong = time
                    tvStartDate.text = DateUtils.longToStringDataNoYear(startLong)
                }
        }

        tvEndDate?.setOnClickListener {
            CalendarSingleDialog(context, 310f, 480f).builder().setOnDateListener { time ->
                    endLong = time
                    tvEndDate.text = DateUtils.longToStringDataNoYear(endLong)
                }
        }

        btnCancel?.setOnClickListener {
            dismiss()
        }
        btnOk?.setOnClickListener {
            var titleStr = ""
            // 上传模式：校验标题非空
            if (type == 1) {
                titleStr = etName?.text.toString().trim()
                if (titleStr.isEmpty()) {
                    return@setOnClickListener
                }
            }
            // 校验日期范围（开始<结束）
            if (startLong > 0 && endLong > 0 && startLong < endLong) {
                dismiss()
                onDialogClickListener?.onClick(titleStr, startLong, endLong)
            }
        }

        dialog?.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }
    }

    fun interface OnDialogClickListener {
        fun onClick(name: String, startLong: Long, endLong: Long)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener): DiaryManageDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): DiaryManageDialog {
        super.builder() // 调用基类构建逻辑（创建Dialog、透明背景等）
        return this
    }
}