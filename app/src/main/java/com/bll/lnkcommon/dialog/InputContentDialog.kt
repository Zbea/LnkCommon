package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.KeyboardUtils

class InputContentDialog(val context: Context,val string: String) {


    fun builder(): InputContentDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_input_content)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val btn_ok = dialog.findViewById<TextView>(R.id.tv_ok)
        val btn_cancel = dialog.findViewById<TextView>(R.id.tv_cancel)
        val name = dialog.findViewById<EditText>(R.id.ed_name)
        name.hint=string
        dialog.show()

        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
        btn_ok.setOnClickListener {
            val content = name.text.toString()
            if (content.isNotEmpty()) {
                dialog.dismiss()
                listener?.onClick(content)
            }
        }
        dialog.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }

        return this
    }

    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onClick(string: String)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

}