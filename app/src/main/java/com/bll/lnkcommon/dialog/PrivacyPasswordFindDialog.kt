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


class PrivacyPasswordFindDialog(private val context: Context) {

    fun builder(): PrivacyPasswordFindDialog? {
        val dialog= Dialog(context)
        dialog.setContentView(R.layout.dialog_check_password_find)
        val window = dialog.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val user= SPUtil.getObj("user", User::class.java)
        val privacyPassword=SPUtil.getObj("${user?.accountId}PrivacyPassword",
            PrivacyPassword::class.java)

        val btn_ok = dialog.findViewById<Button>(R.id.btn_ok)
        val btn_cancel = dialog.findViewById<Button>(R.id.btn_cancel)

        val etPassword=dialog.findViewById<EditText>(R.id.et_password)
        val etPasswordAgain=dialog.findViewById<EditText>(R.id.et_password_again)
        val etPasswordFind=dialog.findViewById<EditText>(R.id.et_question_password)
        val tvFind=dialog.findViewById<TextView>(R.id.tv_question_password)
        tvFind.text=privacyPassword?.question


        btn_cancel?.setOnClickListener { dialog.dismiss() }
        btn_ok?.setOnClickListener {
            val passwordStr=etPassword?.text.toString()
            val passwordAgainStr=etPasswordAgain?.text.toString()
            val passwordFindStr=etPasswordFind?.text.toString()

            if (passwordFindStr.isEmpty()){
                SToast.showText(R.string.password_question_str)
                return@setOnClickListener
            }

            if (passwordFindStr!=privacyPassword?.answer){
                SToast.showText(R.string.password_question_error)
                return@setOnClickListener
            }
            if (passwordStr.isEmpty()||passwordAgainStr.isEmpty()){
                SToast.showText(R.string.password_error)
                return@setOnClickListener
            }
            if (passwordStr!=passwordAgainStr){
                SToast.showText(R.string.password_different)
                return@setOnClickListener
            }

            privacyPassword.password= MD5Utils.digest(passwordStr)
            SPUtil.putObj("${user?.accountId}PrivacyPassword",privacyPassword)
            dialog.dismiss()
            listener?.onClick()

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