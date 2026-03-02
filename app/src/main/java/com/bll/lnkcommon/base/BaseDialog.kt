package com.bll.lnkcommon.base

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.bll.lnkcommon.R


abstract class BaseDialog(protected val context: Context) {
    protected var dialog: Dialog? = null
    // 默认弹窗宽度
    protected open val defaultWidth: Int = WindowManager.LayoutParams.WRAP_CONTENT
    // 默认弹窗位置（居中）
    protected open val defaultGravity: Int = Gravity.CENTER
    // 默认X/Y偏移（dp）
    protected open val defaultXOffset: Float = 0f
    protected open val defaultYOffset: Float = 0f

    protected var btnOk:TextView?=null
    protected var btnCancel:TextView?=null

    /**
     * 子类必须实现：返回弹窗布局ID
     */
    protected abstract fun getLayoutResId(): Int

    /**
     * 子类必须实现：初始化布局控件和业务逻辑
     */
    protected abstract fun initView(contentView: View)

    /**
     * 构建弹窗
     */
    open fun builder(): BaseDialog {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(getLayoutResId())
            window?.apply {
                // 透明背景
                setBackgroundDrawableResource(android.R.color.transparent)
                // 宽度/位置/偏移配置
                attributes = attributes.apply {
                    width = defaultWidth
                    gravity = defaultGravity
                    x = dp2px(defaultXOffset)
                    y = dp2px(defaultYOffset)
                }
            }
            // 点击外部是否关闭（默认true）
            setCanceledOnTouchOutside(true)
            // 点击返回键是否关闭（默认true）
            setCancelable(true)
        }

        dialog?.findViewById<View>(android.R.id.content)?.let {
            btnOk = it.findViewById(R.id.tv_ok)
            btnCancel = it.findViewById(R.id.tv_cancel)
            initView(it)
        }

        show()
        return this
    }


    /**
     * 通用显示方法
     */
    open fun show() {
        dialog?.show()
    }

    /**
     * 通用隐藏方法
     */
    open fun dismiss() {
        dialog?.dismiss()
    }

    /**
     * 通用资源释放方法（避免内存泄漏）
     */
    open fun release() {
        dialog?.apply {
            if (isShowing) dismiss()
        }
        dialog = null
    }

    /**
     * 通用设置：点击外部是否关闭
     */
    fun setCanceledOnTouchOutside(cancel: Boolean): BaseDialog {
        dialog?.setCanceledOnTouchOutside(cancel)
        return this
    }

    /**
     * 通用设置：点击返回键是否关闭
     */
    fun setCancelable(cancel: Boolean): BaseDialog {
        dialog?.setCancelable(cancel)
        return this
    }

    // DP转PX工具方法
    protected fun dp2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}