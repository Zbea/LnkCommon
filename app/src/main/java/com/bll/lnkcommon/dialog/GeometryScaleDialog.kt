package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.SToast

class GeometryScaleDialog(val context: Context, val currentGeometry: Int,val type:Int) {


    fun builder(): GeometryScaleDialog? {

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_geometry_scale)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val btn_ok = dialog.findViewById<Button>(R.id.btn_ok)
        val btn_cancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val et_width = dialog.findViewById<EditText>(R.id.et_width)
        val et_height = dialog.findViewById<EditText>(R.id.et_height)
        when(currentGeometry){
            1->{
                et_width.hint = context.getString(R.string.geometry_hint_lint_distance)
                et_height.visibility= View.GONE
            }
            2->{
                et_width.hint =context.getString(R.string.geometry_hint_rectangle_width)
                et_height.hint = context.getString(R.string.geometry_hint_rectangle_height)
            }
            3->{
                if (type==0){
                    et_width.hint =context.getString(R.string.geometry_hint_circle_radius)
                }
                else{
                    et_width.hint =context.getString(R.string.geometry_hint_circle_diameter)
                }
                et_height.visibility= View.GONE
            }
            5->{
                et_width.hint = context.getString(R.string.geometry_hint_oval_half_width)
                et_height.hint = context.getString(R.string.geometry_hint_oval_half_height)
            }
            7->{
                et_width.hint = "输入抛物线大小"
                et_height.visibility= View.GONE
            }
            8->{
                et_width.hint = context.getString(R.string.geometry_hint_angle)
                et_height.visibility= View.GONE
            }
            9->{
                et_width.hint = context.getString(R.string.geometry_hint_scale)
                et_height.visibility= View.GONE
            }
        }


        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
        btn_ok.setOnClickListener {
            val width = et_width.text.toString()
            val height=et_height.text.toString()
            if (width.isNotEmpty()) {
                dialog.dismiss()
                if (currentGeometry==2||currentGeometry==5||currentGeometry==9){
                    if (height.isNotEmpty()){
                        dialog.dismiss()
                        listener?.onClick(width.toFloat(),height.toFloat())
                    }
                }
                else{
                    val num=width.toFloat()
                    if (currentGeometry==8&&num>360){
                        SToast.showText("角度需要小于360°")
                    }
                    else{
                        dialog.dismiss()
                        listener?.onClick(num,0f)
                    }
                }
            }
        }
        dialog.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }

        return this
    }

    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onClick(width: Float,height:Float)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

}