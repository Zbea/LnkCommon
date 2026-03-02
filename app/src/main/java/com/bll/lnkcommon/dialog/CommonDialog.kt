package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog

class CommonDialog(context: Context) : BaseDialog(context) {

    private var onDialogClickListener: OnDialogClickListener? = null

    private var titleStr = ""
    private var contentStr = "" // 提示文案
    private var contentStrId = 0
    private var cancelStr = "" // 取消文案
    private var okStr = "" // 确认文案

    override fun getLayoutResId(): Int {
        return R.layout.dialog_com
    }

    override fun initView(contentView: View) {
        val titleTv = contentView.findViewById<TextView>(R.id.tv_dialog_title)
        val contentTv = contentView.findViewById<TextView>(R.id.tv_dialog_content)

        if (titleStr.isNotEmpty()) {
            titleTv.text = titleStr
            titleTv.visibility = View.VISIBLE
        } else {
            titleTv.visibility = View.GONE
        }

        when {
            contentStrId != 0 -> contentTv.setText(contentStrId)
            contentStr.isNotEmpty() -> contentTv.text = contentStr
        }

        btnCancel?.text = cancelStr.ifEmpty { "取消" }
        btnOk?.text = okStr.ifEmpty { "确认" }

        btnCancel?.setOnClickListener {
            dismiss()
            onDialogClickListener?.cancel()
        }

        btnOk?.setOnClickListener {
            dismiss()
            onDialogClickListener?.ok()
        }
    }

    fun setTitle(title: String): CommonDialog {
        this.titleStr = title
        return this
    }

    fun setContent(content: String): CommonDialog {
        this.contentStr = content
        return this
    }

    fun setContent(strId: Int): CommonDialog {
        contentStrId = strId
        return this
    }

    fun setCancel(cancel: String): CommonDialog {
        this.cancelStr = cancel
        return this
    }

    fun setOk(ok: String): CommonDialog {
        this.okStr = ok
        return this
    }

    //保持链式结构
    override fun builder(): CommonDialog {
        super.builder()
        return this
    }

    interface OnDialogClickListener {
        fun cancel() {}
        fun ok()
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): CommonDialog {
        this.onDialogClickListener = listener
        return this
    }

}