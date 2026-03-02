package com.bll.lnkcommon.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.ToolUtils

/**
 * 编辑手机号弹窗：继承BaseDialog
 * @param context 上下文
 * @param phone 初始手机号（为空则传空字符串）
 */
class EditPhoneDialog(context: Context, private val phone: String = "") : BaseDialog(context) {

    var btnCode: TextView? = null

    private var onDialogClickListener: OnDialogClickListener? = null

    private var countDownTimer: CountDownTimer? = null

    override fun getLayoutResId(): Int = R.layout.dialog_account_edit_phone

    override fun initView(contentView: View) {

        val edPhone = contentView.findViewById<EditText>(R.id.ed_phone)
        val edCode = contentView.findViewById<EditText>(R.id.ed_code)
        btnCode = contentView.findViewById(R.id.btn_code)

        if (phone.isNotEmpty()) {
            edPhone?.setText(phone)
            edCode?.requestFocus()
        }

        btnCancel?.setOnClickListener {
            dismiss()
        }

        btnOk?.setOnClickListener {
            val phoneStr = edPhone?.text.toString().trim()
            val codeStr = edCode?.text.toString().trim()
            // 手机号+验证码校验
            if (ToolUtils.isPhoneNum(phoneStr) && codeStr.isNotEmpty()) {
                dismiss()
                onDialogClickListener?.onClick(codeStr, phoneStr)
            }
        }

        btnCode?.setOnClickListener {
            val phoneStr = edPhone?.text.toString().trim()
            if (ToolUtils.isPhoneNum(phoneStr)) {
                onDialogClickListener?.onPhone(phoneStr)
                setCountDownTimer()
            }
        }

        dialog?.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }
    }

    private fun setCountDownTimer() {
        // 先取消原有倒计时（避免重复）
        countDownTimer?.cancel()
        // 初始化新倒计时
        countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                btnCode?.isEnabled = true
                btnCode?.isClickable = true
                btnCode?.text = "获取验证码"
            }

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                btnCode?.text = "${millisUntilFinished / 1000}s"
            }
        }.start()

        // 禁用按钮
        btnCode?.isEnabled = false
        btnCode?.isClickable = false
    }

    interface OnDialogClickListener {
        fun onClick(code: String, phone: String)
        fun onPhone(phone: String)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener): EditPhoneDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): EditPhoneDialog {
        super.builder()
        return this
    }
}