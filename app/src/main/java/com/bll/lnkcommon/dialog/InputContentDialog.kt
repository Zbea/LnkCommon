package com.bll.lnkcommon.dialog

import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.utils.KeyboardUtils

/**
 * 输入内容弹窗：继承BaseDialog，复用通用逻辑
 * @param context 上下文
 * @param hintStr 输入框提示文案
 * @param type 输入类型（0=默认文本，1=数字）
 */
class InputContentDialog(context: Context, private val hintStr: String, private val type: Int = 0) : BaseDialog(context) {

    override fun getLayoutResId(): Int {
        return R.layout.dialog_input_content // 输入框弹窗布局
    }

    override fun initView(contentView: View) {
        val edName = contentView.findViewById<EditText>(R.id.ed_name)

        edName.hint = hintStr
        if (type == 1) {
            edName.inputType = InputType.TYPE_CLASS_NUMBER
        }

        btnCancel?.setOnClickListener {
            dismiss()
        }

        btnOk?.setOnClickListener {
            val inputContent = edName.text.toString().trim()
            if (inputContent.isNotEmpty()) {
                dismiss()
                onDialogClickListener?.onClick(inputContent)
            }
        }

        dialog?.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }
    }

    fun interface OnDialogClickListener {
        fun onClick(inputContent: String) // 带输入内容的回调
    }

    private var onDialogClickListener: OnDialogClickListener? = null

    fun setOnDialogClickListener(listener: OnDialogClickListener?): InputContentDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): InputContentDialog {
        super.builder()
        return this
    }
}