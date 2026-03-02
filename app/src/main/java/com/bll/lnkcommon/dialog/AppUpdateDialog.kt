package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.mvp.model.AppUpdateBean
import com.bll.lnkcommon.mvp.model.SystemUpdateInfo
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.SPUtil

/**
 * 应用/系统更新弹窗
 * @param context 上下文
 * @param type 类型（1=应用更新，其他=系统更新）
 * @param item 数据（AppUpdateBean/SystemUpdateInfo）
 */
class AppUpdateDialog(context: Context, private val type: Int, private val item: Any) : BaseDialog(context) {

    private var tvUpdate: TextView? = null
    private var tvInfo: TextView? = null
    private var onDialogClickListener: OnDialogClickListener? = null

    override fun getLayoutResId(): Int = R.layout.dialog_update

    override fun initView(contentView: View) {
        tvUpdate = contentView.findViewById(R.id.tv_update)
        val tvCancel = contentView.findViewById<TextView>(R.id.tv_cancel)
        val tvTitle = contentView.findViewById<TextView>(R.id.tv_title)
        tvInfo = contentView.findViewById(R.id.tv_info)

        tvCancel?.setOnClickListener {
            dismiss()
            SPUtil.putString(Constants.SP_UPDATE_SYSTEM_STATUS, "waiting")
            onDialogClickListener?.onDelay()
        }

        when (type) {
            1 -> {
                // 应用更新
                val appUpdateBean = item as AppUpdateBean
                tvTitle?.text = "应用更新：${appUpdateBean.versionName}"
                tvInfo?.text = appUpdateBean.versionInfo
                tvCancel?.visibility = View.GONE // 隐藏取消按钮
            }
            else -> {
                // 系统更新
                val systemUpdateInfo = item as SystemUpdateInfo
                tvTitle?.text = "系统更新：${systemUpdateInfo.version}"
                tvInfo?.text = systemUpdateInfo.description
                // 确认更新按钮逻辑
                tvUpdate?.setOnClickListener {
                    dismiss()
                    AppUtils.startAPP(context, Constants.PACKAGE_SYSTEM_UPDATE)
                }
            }
        }
    }

    fun setUpdateBtn(text: String): AppUpdateDialog {
        tvUpdate?.text = text
        return this
    }

    fun isShow(): Boolean = dialog?.isShowing ?: false

    fun interface OnDialogClickListener {
        fun onDelay()
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): AppUpdateDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): AppUpdateDialog {
        super.builder()
        return this
    }

}