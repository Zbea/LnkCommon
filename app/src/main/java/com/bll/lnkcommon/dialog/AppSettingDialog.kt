package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.Book

class AppSettingDialog(val context: Context, val nameStr:String){

    fun builder(): AppSettingDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_app_setting)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tv_name=dialog.findViewById<TextView>(R.id.tv_name)
        val iv_close=dialog.findViewById<ImageView>(R.id.iv_close)
        val ll_delete=dialog.findViewById<LinearLayout>(R.id.ll_delete)
        val ll_move=dialog.findViewById<LinearLayout>(R.id.ll_move)

        tv_name.text=nameStr

        iv_close.setOnClickListener {
            dialog.dismiss()
        }

        ll_delete.setOnClickListener {
            onClickListener?.onUninstall()
            dialog.dismiss()
        }

        ll_move.setOnClickListener {
            onClickListener?.onMove()
            dialog.dismiss()
        }

        return this
    }

    private var onClickListener: OnDialogClickListener? = null

    interface OnDialogClickListener {
        fun onUninstall()
        fun onMove()
    }

    fun setOnDialogClickListener(onClickListener: OnDialogClickListener?) {
        this.onClickListener = onClickListener
    }

}