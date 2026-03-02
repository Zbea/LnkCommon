package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.widget.NumberPasswordView

class NumberPasswordDialog(context: Context) : BaseDialog(context) {

    private var tvTitle: TextView? = null
    private var nvView: NumberPasswordView? = null

    override fun getLayoutResId(): Int = R.layout.dialog_number_password

    override fun initView(contentView: View) {
        // 关闭按钮
        val ivClose = contentView.findViewById<ImageView>(R.id.iv_close)
        ivClose?.setOnClickListener { dismiss() }

        // 标题和密码输入控件
        tvTitle = contentView.findViewById(R.id.tv_title)
        nvView = contentView.findViewById(R.id.nv_view)

        // 密码完成监听
        nvView?.onPwdComplete = { psd ->
            onDialogClickListener?.onNumber(psd)
        }

        // 弹窗关闭监听
        dialog?.setOnDismissListener {
            onDialogClickListener?.onDismiss()
        }
    }

    // 自定义方法
    fun reset() {
        nvView?.reset()
    }

    fun setTitle(str: String) {
        tvTitle?.text = str
    }

    // 点击监听
    private var onDialogClickListener: OnDialogClickListener? = null
    interface OnDialogClickListener {
        fun onNumber(psw: String)
        fun onDismiss() {}
    }
    fun setOnDialogClickListener(onDialogClickListener: OnDialogClickListener) {
        this.onDialogClickListener = onDialogClickListener
    }

    override fun builder(): NumberPasswordDialog {
        super.builder()
        return this
    }

}