package com.bll.lnkcommon.dialog


import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog

class MessageSendDialog(context: Context) : BaseDialog(context) {

    override fun getLayoutResId(): Int = R.layout.dialog_message_send

    override fun initView(contentView: View) {
        val tvOK = contentView.findViewById<TextView>(R.id.tv_ok)
        val tvCancel = contentView.findViewById<TextView>(R.id.tv_cancel)
        val etContent = contentView.findViewById<EditText>(R.id.et_content)

        tvCancel?.setOnClickListener { dismiss() }

        tvOK?.setOnClickListener {
            val contentStr = etContent?.text.toString()
            if (contentStr.isNotEmpty()) {
                dismiss()
                listener?.onSend(contentStr)
            }
        }
    }

    private var listener: OnDialogClickListener? = null
    fun interface OnDialogClickListener {
        fun onSend(contentStr: String)
    }
    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    override fun builder(): MessageSendDialog {
        super.builder()
        return this
    }
}