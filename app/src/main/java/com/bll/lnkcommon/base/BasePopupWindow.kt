package com.bll.lnkcommon.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.PopupBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

abstract class BasePopupWindow(
    protected val context: Context,
    protected val anchorView: View, // 锚点View（PopupWindow依附的View）
    protected val layoutWidth: Int = 0, // PopupWindow宽度（0则自适应）
    protected val xOffset: Int = 0, // X轴偏移量
    protected val yOffset: Int = 0 // Y轴偏移量
){
    protected var mPopupWindow: PopupWindow? = null
    protected lateinit var contentView: View

    /**
     * 子类必须实现：返回PopupWindow的布局ID
     */
    protected abstract fun getLayoutResId(): Int

    /**
     * 子类必须实现：初始化布局内的控件和业务逻辑（如RecyclerView、Adapter等）
     */
    protected abstract fun initView()

    /**
     * 构建PopupWindow
     */
    fun builder(): BasePopupWindow {
        contentView = LayoutInflater.from(context).inflate(getLayoutResId(), null, false)
        mPopupWindow = PopupWindow(context).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            this.contentView = this@BasePopupWindow.contentView
            isFocusable = true
            isTouchable = true
            isOutsideTouchable = true
            isClippingEnabled = false
            if (layoutWidth != 0) this.width = layoutWidth
        }
        initView()
        show()
        return this
    }

    /**
     * 通用显示方法
     */
    open fun show() {
        val calculatedXOffset=if (layoutWidth!=0){
            xOffset
        }
        else{
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            -contentView.measuredWidth+anchorView.width+xOffset
        }
        mPopupWindow?.showAsDropDown(anchorView,calculatedXOffset, yOffset, Gravity.START)
    }

    /**
     * 通用消失方法
     */
    fun dismiss() {
        mPopupWindow?.dismiss()
    }

    /**
     * 通用RecyclerView初始化方法
     */
    protected fun initRecyclerView(rvId: Int, data: List<PopupBean>?, layoutResId: Int = R.layout.item_popwindow_list, adapterBuilder: (Int, List<PopupBean>?) -> BaseQuickAdapter<PopupBean, BaseViewHolder>): BaseQuickAdapter<PopupBean, BaseViewHolder> {
        val rv = contentView.findViewById<RecyclerView>(rvId)
        rv.layoutManager = LinearLayoutManager(context)
        val adapter = adapterBuilder(layoutResId, data)
        adapter.bindToRecyclerView(rv)
        return adapter
    }

    private var onSelectListener: OnSelectListener? = null

    fun setOnSelectListener(listener: OnSelectListener) {
        this.onSelectListener = listener
    }

    fun interface OnSelectListener {
        fun onSelect(item: PopupBean)
    }

    /**
     * 触发选中回调
     */
    protected fun notifySelect(item: PopupBean) {
        onSelectListener?.onSelect(item)
        dismiss()
    }

    // ===================== 多选回调 =====================
    private var onMultiSelectListener: OnMultiSelectListener? = null

    fun setOnMultiSelectListener(listener: OnMultiSelectListener) {
        this.onMultiSelectListener = listener
    }

    fun interface OnMultiSelectListener {
        fun onSelect(items: List<PopupBean>)
    }

    protected fun notifyMultiSelect(items: List<PopupBean>) {
        onMultiSelectListener?.onSelect(items)
    }

    /**
     * 通用设置消失监听
     */
    protected fun setPopupDismissListener(listener: () -> Unit) {
        mPopupWindow?.setOnDismissListener { listener.invoke() }
    }

    // DP转PX工具方法
    fun dp2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

}