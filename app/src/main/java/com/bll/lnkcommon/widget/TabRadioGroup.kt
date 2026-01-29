package com.bll.lnkcommon.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.RadioButton
import android.widget.RadioGroup
import com.bll.lnkcommon.R

/**
 * 自定义RadioGroup选项卡组件，支持选中文字变大变粗，可复用
 */
class TabRadioGroup @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RadioGroup(context, attrs) {

    // 默认配置参数
    private val DEFAULT_UNSELECTED_TEXT_SIZE = 14f // sp
    private val DEFAULT_SELECTED_TEXT_SIZE = 16f // sp
    private val DEFAULT_UNSELECTED_TEXT_COLOR = 0xFF666666.toInt() // 灰色
    private val DEFAULT_SELECTED_TEXT_COLOR = 0xFF2196F3.toInt() // 蓝色
    private val DEFAULT_TAB_HEIGHT = 50 // dp

    // 配置参数（可通过XML/代码修改）
    private var unselectedTextSize: Float = DEFAULT_UNSELECTED_TEXT_SIZE
    private var selectedTextSize: Float = DEFAULT_SELECTED_TEXT_SIZE
    private var unselectedTextColor: Int = DEFAULT_UNSELECTED_TEXT_COLOR
    private var selectedTextColor: Int = DEFAULT_SELECTED_TEXT_COLOR
    private var tabHeight: Int = dp2px(DEFAULT_TAB_HEIGHT)
    private var tabTitles: List<String> = emptyList()

    // 缓存所有RadioButton选项卡
    private val tabRadioButtons = mutableListOf<RadioButton>()

    // 选中回调接口
    interface OnTabSelectedListener {
        /**
         * 选项卡选中回调
         * @param position 选中项的索引（从0开始）
         * @param title 选中项的标题
         */
        fun onTabSelected(position: Int, title: String)
    }

    // 对外暴露的回调实例
    private var onTabSelectedListener: OnTabSelectedListener? = null

    init {
        // 1. 设置默认方向为横向
        orientation = HORIZONTAL

        // 2. 解析自定义属性
        parseCustomAttrs(attrs)

        // 3. 初始化选项卡
        initTabs()

        // 4. 设置选中状态监听
        setOnCheckedChangeListenerInternal()
    }

    /**
     * 解析XML中的自定义属性
     */
    private fun parseCustomAttrs(attrs: AttributeSet?, defStyleAttr: Int=0) {
        val typedArray: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.TabRadioGroup,
            defStyleAttr,
            0
        )

        // 解析文字大小（优先取XML配置，无则用默认值）
        unselectedTextSize = typedArray.getDimension(
            R.styleable.TabRadioGroup_unselectedTextSize,
            sp2px(DEFAULT_UNSELECTED_TEXT_SIZE)
        )
        selectedTextSize = typedArray.getDimension(
            R.styleable.TabRadioGroup_selectedTextSize,
            sp2px(DEFAULT_SELECTED_TEXT_SIZE)
        )

        // 解析文字颜色
        unselectedTextColor = typedArray.getColor(
            R.styleable.TabRadioGroup_unselectedTextColor,
            DEFAULT_UNSELECTED_TEXT_COLOR
        )
        selectedTextColor = typedArray.getColor(
            R.styleable.TabRadioGroup_selectedTextColor,
            DEFAULT_SELECTED_TEXT_COLOR
        )

        // 解析选项卡高度
        tabHeight = typedArray.getDimensionPixelSize(
            R.styleable.TabRadioGroup_tabHeight,
            dp2px(DEFAULT_TAB_HEIGHT)
        )

        // 解析选项卡标题（逗号分隔）
        val tabTitlesStr = typedArray.getString(R.styleable.TabRadioGroup_tabTitles)
        tabTitles = tabTitlesStr?.split(",")?.map { it.trim() } ?: emptyList()

        // 回收TypedArray，避免内存泄漏
        typedArray.recycle()
    }

    /**
     * 初始化所有选项卡
     */
    private fun initTabs() {
        tabRadioButtons.clear()
        removeAllViews() // 清空原有视图，避免重复添加

        tabTitles.forEachIndexed { index, title ->
            // 创建RadioButton
            val radioButton = createTabRadioButton(title, index)
            // 添加到RadioGroup
            addView(radioButton)
            // 缓存到列表
            tabRadioButtons.add(radioButton)
        }

        // 默认选中第一个选项卡（若有）
        if (tabRadioButtons.isNotEmpty()) {
            tabRadioButtons[0].isChecked = true
            setTabStyle(tabRadioButtons[0], isSelected = true)
        }
    }

    /**
     * 创建单个选项卡RadioButton
     */
    private fun createTabRadioButton(title: String, index: Int): RadioButton {
        return RadioButton(context).apply {
            // 1. 基础配置
            id = index + 1000 // 设置唯一ID（避免冲突）
            text = title
            gravity = Gravity.CENTER // 文字居中

            // 2. 隐藏默认单选按钮和背景
            buttonDrawable = null // 替代android:button="@null"（兼容高版本）
            setBackgroundResource(0)

            // 3. 设置布局参数（平分宽度，固定高度）
            layoutParams = LayoutParams(
                0,
                tabHeight,
                1.0f // 权重1，实现平分宽度
            )

            // 4. 设置默认样式（未选中状态）
            setTextSize(TypedValue.COMPLEX_UNIT_PX, unselectedTextSize)
            setTextColor(unselectedTextColor)
            typeface = Typeface.DEFAULT
        }
    }

    /**
     * 内部选中状态监听，处理样式切换和对外回调
     */
    private fun setOnCheckedChangeListenerInternal() {
        this.setOnCheckedChangeListener { _, checkedId ->
            tabRadioButtons.forEachIndexed { index, radioButton ->
                val isSelected = radioButton.id == checkedId
                // 切换样式
                setTabStyle(radioButton, isSelected)
                // 对外回调（仅选中时触发）
                if (isSelected) {
                    onTabSelectedListener?.onTabSelected(index, radioButton.text.toString())
                }
            }
        }
    }

    /**
     * 统一设置选项卡样式（核心：变大+变粗）
     */
    private fun setTabStyle(radioButton: RadioButton, isSelected: Boolean) {
        radioButton.apply {
            // 文字大小切换
            setTextSize(TypedValue.COMPLEX_UNIT_PX, if (isSelected) selectedTextSize else unselectedTextSize)
            // 文字颜色切换
            setTextColor(if (isSelected) selectedTextColor else unselectedTextColor)
            // 文字粗细切换
            typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    // ---------------------- 对外暴露的公共API ----------------------
    /**
     * 设置选项卡选中回调
     */
    fun setOnTabSelectedListener(listener: OnTabSelectedListener) {
        this.onTabSelectedListener = listener
    }

    /**
     * 代码动态设置选项卡标题
     */
    fun setTabTitles(titles: MutableList<String>) {
        this.tabTitles = titles
        initTabs()
    }

    /**
     * 代码动态选中指定位置的选项卡
     */
    fun selectTab(position: Int) {
        if (position in tabRadioButtons.indices) {
            tabRadioButtons[position].isChecked = true
        }
    }

    // ---------------------- 工具方法 ----------------------
    /**
     * dp转px
     */
    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    /**
     * sp转px
     */
    private fun sp2px(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }
}