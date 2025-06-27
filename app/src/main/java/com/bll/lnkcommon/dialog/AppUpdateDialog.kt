package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.AppUpdateBean
import com.bll.lnkcommon.mvp.model.SystemUpdateInfo
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.DP2PX


class AppUpdateDialog(private val context: Context,private val type:Int,private val item:Any){

    private var dialog:Dialog?=null
    private var btn_ok:TextView?=null
    private var tv_info:TextView?=null

    fun builder(): AppUpdateDialog {
        dialog= Dialog(context)
        dialog?.setContentView(R.layout.dialog_update)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
//        dialog!!.setCanceledOnTouchOutside(false)
        dialog?.show()

        btn_ok = dialog?.findViewById(R.id.tv_update)
        val tvCancel = dialog?.findViewById<TextView>(R.id.tv_cancel)
        val tv_name = dialog?.findViewById<TextView>(R.id.tv_title)
        tv_info = dialog?.findViewById(R.id.tv_info)

        tvCancel?.setOnClickListener {
            dismiss()
        }

        if(type==1){
            val item=item as AppUpdateBean
            tv_name?.text="应用更新："+item.versionName
            tv_info?.text=item.versionInfo
            tvCancel?.visibility= View.GONE
        }
        else{
            val item=item as SystemUpdateInfo
            tv_name?.text="系统更新："+item.version
            tv_info?.text=item.description
            btn_ok?.setOnClickListener {
                dialog?.dismiss()
                AppUtils.startAPP(context,Constants.PACKAGE_SYSTEM_UPDATE)
            }
        }
        return this
    }

    fun show() {
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun isShow():Boolean?{
        return dialog?.isShowing
    }

    fun setUpdateBtn(string: String){
        if (btn_ok!=null){
            btn_ok?.text = string
        }
    }
}