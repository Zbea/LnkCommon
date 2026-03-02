package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.SToast

/**
 * 几何尺寸设置弹窗：继承BaseDialog
 * @param context 上下文
 * @param currentGeometry 几何类型（1=线、2=矩形、3=圆等）
 * @param type 子类型（圆：0=半径、1=直径）
 */
class GeometryScaleDialog(context: Context, private val currentGeometry: Int, private val type: Int) : BaseDialog(context) {

    private var onDialogClickListener: OnDialogClickListener? = null

    override fun getLayoutResId(): Int = R.layout.dialog_geometry_scale

    override fun initView(contentView: View) {

        val btnOk = contentView.findViewById<TextView>(R.id.tv_ok)
        val btnCancel = contentView.findViewById<TextView>(R.id.tv_cancel)
        val etWidth = contentView.findViewById<EditText>(R.id.et_width)
        val etHeight = contentView.findViewById<EditText>(R.id.et_height)

        //根据几何类型初始化输入框提示/显隐
        when (currentGeometry) {
            1 -> {
                etWidth.hint = context.getString(R.string.geometry_hint_lint_distance)
                etHeight.visibility = View.GONE
            }
            2 -> {
                etWidth.hint = context.getString(R.string.geometry_hint_rectangle_width)
                etHeight.hint = context.getString(R.string.geometry_hint_rectangle_height)
            }
            3 -> {
                etWidth.hint = if (type == 0) {
                    context.getString(R.string.geometry_hint_circle_radius)
                } else {
                    context.getString(R.string.geometry_hint_circle_diameter)
                }
                etHeight.visibility = View.GONE
            }
            5 -> {
                etWidth.hint = context.getString(R.string.geometry_hint_oval_half_width)
                etHeight.hint = context.getString(R.string.geometry_hint_oval_half_height)
            }
            7 -> {
                etWidth.hint = "输入抛物线大小"
                etHeight.visibility = View.GONE
            }
            8 -> {
                etWidth.hint = context.getString(R.string.geometry_hint_angle)
                etHeight.visibility = View.GONE
            }
            9 -> {
                etWidth.hint = context.getString(R.string.geometry_hint_scale)
                etHeight.visibility = View.GONE
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnOk.setOnClickListener {
            val widthStr = etWidth.text.toString().trim()
            val heightStr = etHeight.text.toString().trim()

            if (widthStr.isNotEmpty()) {
                val width = widthStr.toFloat()
                // 不同几何类型的校验逻辑
                when (currentGeometry) {
                    2, 5, 9 -> {
                        if (heightStr.isNotEmpty()) {
                            val height = heightStr.toFloat()
                            dismiss()
                            onDialogClickListener?.onClick(width, height)
                        }
                    }
                    8 -> {
                        if (width > 360) {
                            SToast.showText("角度需要小于360°")
                        } else {
                            dismiss()
                            onDialogClickListener?.onClick(width, 0f)
                        }
                    }
                    else -> {
                        dismiss()
                        onDialogClickListener?.onClick(width, 0f)
                    }
                }
            }
        }

        dialog?.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }
    }

    fun interface OnDialogClickListener {
        fun onClick(width: Float, height: Float)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): GeometryScaleDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): GeometryScaleDialog {
        super.builder()
        return this
    }

}