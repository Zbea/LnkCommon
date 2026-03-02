package com.bll.lnkcommon.dialog

import android.app.Activity
import android.content.Context
import android.view.View
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog

class ProgressDialog(context: Context) : BaseDialog(context) {

    override fun getLayoutResId(): Int = R.layout.dialog_progress

    override fun initView(contentView: View) {
    }

    // 重写show方法，增加Activity状态检查
    override fun show() {
        val activity = context as? Activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            super.show()
        }
    }

    // 重写dismiss方法，增加Activity状态检查
    override fun dismiss() {
        val activity = context as? Activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            super.dismiss()
        }
    }

    override fun builder(): ProgressDialog {
        super.builder()
        return this
    }

}
