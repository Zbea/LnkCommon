package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.TextView
import com.bll.lnkcommon.R


class CommonDialog(private val context: Context) {

    private var dialog: Dialog? = null
    private var titleStr = ""
    private var isTitle = false
    private var contentStr = "" //提示文案
    private var contentStrId = 0
    private var cancelStr = "取消" //取消文案
    private var okStr = "确认" //确认文案

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
        cancelStr = cancel
        return this
    }

    fun setOk(ok: String): CommonDialog {
        okStr = ok
        return this
    }

    fun setTitleView(boolean: Boolean): CommonDialog {
        isTitle = boolean
        return this
    }

    fun builder(): CommonDialog {
        dialog = Dialog(context)
        dialog?.setContentView(R.layout.dialog_com)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()

        val titleTv = dialog!!.findViewById<TextView>(R.id.tv_dialog_title)
        val contentTv = dialog!!.findViewById<TextView>(R.id.tv_dialog_content)
        val cancelTv = dialog!!.findViewById<TextView>(R.id.tv_cancel)
        val tvOk = dialog!!.findViewById<TextView>(R.id.tv_ok)

        if (titleStr.isNotEmpty()) titleTv.text = titleStr
        if (!isTitle) {
            titleTv.visibility = View.GONE
            contentTv.minHeight = 220
        }
        if (contentStr.isNotEmpty()) contentTv.text = contentStr
        if (contentStrId!=0) contentTv.setText(contentStrId)
        if (cancelStr.isNotEmpty()) cancelTv.text = cancelStr
        if (okStr.isNotEmpty()) tvOk.text = okStr

        cancelTv.setOnClickListener {
            cancel()
            onDialogClickListener?.cancel()
        }
        tvOk.setOnClickListener {
            cancel()
            onDialogClickListener?.ok()
        }

        return this
    }

    fun show() {
        dialog!!.show()
    }

    fun cancel() {
        dialog!!.dismiss()
    }

    var onDialogClickListener: OnDialogClickListener? = null

    interface OnDialogClickListener {
        fun cancel()
        fun ok()
    }

    fun setDialogClickListener(onDialogClickListener: OnDialogClickListener?) {
        this.onDialogClickListener = onDialogClickListener
    }
}