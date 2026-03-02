package com.bll.lnkcommon.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BasePopupWindow
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.widget.WheelView

/**
 * 时间选择器
 */
class PopupDateSelector(
    context: Context,
    anchorView: View,
    private val nums: List<Int>,
    private val type: Int // 0=年份选择，其他=月份选择
) : BasePopupWindow(context = context, anchorView = anchorView, layoutWidth = anchorView.width, xOffset = 0, yOffset = 5) {

    override fun getLayoutResId(): Int = R.layout.popup_date_number_selector

    override fun initView() {

        val defaultPos = calculateDefaultPosition()

        val wvView = contentView.findViewById<WheelView>(R.id.wv_view)
        wvView.setOffset(2) // 保留原有偏移设置
        wvView.setItems(nums) // 设置滚轮数据
        wvView.setSelection(defaultPos) // 设置默认选中位置

        wvView.setOnWheelViewListener(object : WheelView.OnWheelViewListener {
            override fun onSelector(selectedIndex: Int, item: String?) {
                item?.let {
                    onDateSelectorListener?.onSelect(it)
                }
            }

            override fun onClick(item: String?) {
                item?.let {
                    onDateSelectorListener?.onSelect(it)
                    dismiss()
                }
            }
        })
    }

    /**
     * 辅助方法：计算年份/月份的默认选中位置
     */
    private fun calculateDefaultPosition(): Int {
        var pos = 0
        if (type == 0) {
            // 年份选择：匹配当前年份
            for (i in nums.indices) {
                if (nums[i] == DateUtils.getYear()) {
                    pos = i
                    break
                }
            }
        } else {
            // 月份选择：匹配当前月份
            for (i in nums.indices) {
                if (nums[i] == DateUtils.getMonth()) {
                    pos = i
                    break
                }
            }
        }
        return pos
    }

    private var onDateSelectorListener: OnDateSelectorListener? = null

    fun setOnDateSelectorListener(listener: OnDateSelectorListener) {
        this.onDateSelectorListener = listener
    }

    fun interface OnDateSelectorListener {
        fun onSelect(date: String)
    }

}