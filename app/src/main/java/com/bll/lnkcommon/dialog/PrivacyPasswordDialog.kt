package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.PrivacyPassword
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.SPUtil
import com.bll.lnkcommon.utils.SToast


class PrivacyPasswordDialog(private val context: Context) {

    fun builder(): PrivacyPasswordDialog? {
        val dialog= Dialog(context)
        dialog.setContentView(R.layout.dialog_check_password)
        val window = dialog.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val btn_ok = dialog.findViewById<Button>(R.id.btn_ok)
        val btn_cancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val etPassword=dialog.findViewById<EditText>(R.id.et_password)
        val tvFind = dialog.findViewById<TextView>(R.id.tv_find_password)

        tvFind.setOnClickListener {
            dialog.dismiss()
            PrivacyPasswordFindDialog(context).builder()
        }

        val tvEdit = dialog.findViewById<TextView>(R.id.tv_edit_password)
        tvEdit.setOnClickListener {
            dialog.dismiss()
            PrivacyPasswordEditDialog(context).builder()
        }

        btn_cancel?.setOnClickListener { dialog.dismiss() }
        btn_ok?.setOnClickListener {
            val passwordStr=etPassword?.text.toString()
            if (passwordStr.isEmpty()){
                return@setOnClickListener
            }
            val user= SPUtil.getObj("user", User::class.java)
            val privacyPassword=SPUtil.getObj("${user?.accountId}PrivacyPassword",
                PrivacyPassword::class.java)
            if (MD5Utils.digest(passwordStr) != privacyPassword?.password){
                SToast.showText(R.string.password_error)
                return@setOnClickListener
            }
            listener?.onClick()
            dialog.dismiss()

        }

        dialog.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }

        return this
    }


    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onClick()
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

}