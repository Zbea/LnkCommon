package com.bll.lnkcommon.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class NumberPasswordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr){

    private val PWD_LENGTH = 6
    private val DOT_SIZE = dp2px(20f) // 密码圆点大小
    private val KEY_SIZE = dp2px(80f) // 键盘按钮大小
    private val COLOR_PRIMARY = Color.parseColor("#000000") // 主题色
    private val COLOR_GRAY = Color.parseColor("#CCCCCC") // 灰色
    private val COLOR_KEY_NORMAL = Color.parseColor("#000000") // 键盘常态色
    private val COLOR_KEY_PRESSED = Color.parseColor("#4D000000") // 键盘按下色

    // 密码存储
    private val pwdBuilder = StringBuilder()
    // 密码圆点列表
    private val dotViews = mutableListOf<TextView>()
    // 输入完成回调
    var onPwdComplete: ((String) -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp2px(20f), dp2px(50f), dp2px(20f), dp2px(20f))
        initPwdDots()
        initNumberKeyboard()
    }

    /**
     * 初始化密码圆点（6个）
     */
    private fun initPwdDots() {
        val dotContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setPadding(0, 0, 0, dp2px(50f))
        }

        repeat(PWD_LENGTH) {
            val dotView = TextView(context).apply {
                layoutParams = LayoutParams(DOT_SIZE, DOT_SIZE).apply {
                    marginStart = dp2px(10f)
                    marginEnd = dp2px(10f)
                }
                background = getDotBackground(false)
                gravity = Gravity.CENTER
            }
            dotViews.add(dotView)
            dotContainer.addView(dotView)
        }
        addView(dotContainer)
    }

    /**
     * 初始化数字键盘
     */
    private fun initNumberKeyboard() {
        val keyboardContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // 键盘布局：1-9 + 空-0-删除
        val keyValues = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "del")
        )

        keyValues.forEach { row ->
            val rowLayout = LinearLayout(context).apply {
                orientation = HORIZONTAL
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                gravity = Gravity.CENTER
                weightSum = row.size.toFloat()
            }
            row.forEach { key ->
                val btn = Button(context).apply {
                    layoutParams = LayoutParams(KEY_SIZE, KEY_SIZE).apply {
                        topMargin=dp2px(10f)
                        bottomMargin=dp2px(10f)
                        marginStart=dp2px(30f)
                        marginEnd=dp2px(30f)
                        gravity = Gravity.CENTER
                    }
                    background = createKeySelectorDrawable()
                    textSize = 30f
                    gravity = Gravity.CENTER

                    when (key) {
                        "" -> visibility = View.INVISIBLE
                        "del" -> {
                            text = "←"
                            setTextColor(Color.parseColor("#ffffff"))
                            setOnClickListener { deleteLastPwd() }
                        }
                        else -> {
                            text = key
                            setTextColor(Color.parseColor("#ffffff"))
                            setOnClickListener { addPwd(key) }
                        }
                    }
                }
                rowLayout.addView(btn)
            }
            keyboardContainer.addView(rowLayout)
        }
        addView(keyboardContainer)
    }

    /**
     * 添加密码字符
     */
    private fun addPwd(num: String) {
        if (pwdBuilder.length >= PWD_LENGTH) return
        pwdBuilder.append(num)
        updateDots()
        // 输入完成触发回调
        if (pwdBuilder.length == PWD_LENGTH) {
            postDelayed({
                if (pwdBuilder.length == PWD_LENGTH) {
                    onPwdComplete?.invoke(pwdBuilder.toString())
                }
            }, 100)
        }
    }

    /**
     * 删除最后一位密码
     */
    private fun deleteLastPwd() {
        if (pwdBuilder.isEmpty()) return
        pwdBuilder.deleteCharAt(pwdBuilder.length - 1)
        updateDots()
    }

    /**
     * 更新密码圆点显示
     */
    private fun updateDots() {
        dotViews.forEachIndexed { index, dotView ->
            dotView.background = if (index < pwdBuilder.length) {
                getDotBackground(true)
            } else {
                getDotBackground(false)
            }
        }
    }

    /**
     * 重置密码输入状态（外部可调用）
     */
    fun reset() {
        pwdBuilder.clear()
        updateDots()
    }

    /**
     * 代码创建键盘按钮的Selector Drawable（对应xml的selector）
     */
    private fun createKeySelectorDrawable(): StateListDrawable {
        return StateListDrawable().apply {
            val pressedDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(COLOR_KEY_PRESSED)
            }
            val normalDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(COLOR_KEY_NORMAL)
            }
            // 绑定状态：按下状态 -> pressedDrawable，默认 -> normalDrawable
            addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
            addState(intArrayOf(), normalDrawable)
        }
    }

    private fun getDotBackground(isFill: Boolean): Drawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.OVAL
        if (isFill) {
            shape.setColor(COLOR_PRIMARY)
        } else {
            shape.setColor(COLOR_GRAY)
        }
        shape.setSize(DOT_SIZE, DOT_SIZE)
        return shape
    }

    /**
     * dp转px（内联简化，无需单独工具类）
     */
    private fun dp2px(dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

}