package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.SToast

/**
 * 作业创建弹窗：继承BaseDialog
 * @param context 上下文
 */
class HomeworkCreateDialog(context: Context) : BaseDialog(context) {

    private var courseId = 0

    private var onDialogClickListener: OnDialogClickListener? = null

    override fun getLayoutResId(): Int = R.layout.dialog_homework_create

    override fun initView(contentView: View) {

        val tvSend = contentView.findViewById<TextView>(R.id.tv_send)
        val tvCourse = contentView.findViewById<TextView>(R.id.tv_course)
        val etContent = contentView.findViewById<EditText>(R.id.et_content)

        val pops = DataBeanManager.popupCourses
        tvCourse.setOnClickListener {
            PopupRadioList(context, pops, tvCourse, tvCourse.width, 5)
                .builder()
                .setOnSelectListener {
                    tvCourse.text = it.name
                    courseId = it.id
                }
        }

        tvSend.setOnClickListener {
            val contentStr = etContent.text.toString().trim()
            when {
                contentStr.isEmpty() -> SToast.showText(R.string.toast_input_content)
                courseId == 0 -> SToast.showText(R.string.selector_subject)
                else -> {
                    onDialogClickListener?.onCreate(contentStr, courseId)
                    dismiss()
                }
            }
        }

        dialog?.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }
    }

    fun interface OnDialogClickListener {
        fun onCreate(contentStr: String, courseId: Int)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): HomeworkCreateDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): HomeworkCreateDialog {
        super.builder()
        return this
    }
}