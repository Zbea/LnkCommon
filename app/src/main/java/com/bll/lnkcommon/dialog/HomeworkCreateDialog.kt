package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.SToast

class HomeworkCreateDialog(val context: Context) {

    private var courseId=0

    fun builder(): HomeworkCreateDialog {

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_homework_create)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val tv_send = dialog.findViewById<TextView>(R.id.tv_send)
        val tv_course = dialog.findViewById<TextView>(R.id.tv_course)
        val etContent = dialog.findViewById<EditText>(R.id.et_content)

        val pops=DataBeanManager.popupCourses
        tv_course.setOnClickListener {
            PopupRadioList(context,pops,tv_course,tv_course.width,5).builder()
                .setOnSelectListener{
                    tv_course.text=it.name
                    courseId=it.id
            }
        }

        tv_send.setOnClickListener {
            val contentStr = etContent.text.toString()
            if (contentStr.isEmpty()){
                SToast.showText(R.string.toast_input_content)
                return@setOnClickListener
            }
            if (courseId==0){
                SToast.showText(R.string.selector_subject)
                return@setOnClickListener
            }
            listener?.onCreate(contentStr,courseId)
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }

        return this
    }




    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onCreate(contentStr:String,courseId:Int)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

}