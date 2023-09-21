package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.CheckPassword
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.SPUtil
import com.bll.lnkcommon.utils.SToast
import com.google.gson.Gson


class CheckPasswordEditDialog(private val context: Context) {

    fun builder(): CheckPasswordEditDialog? {
        val dialog= Dialog(context)
        dialog.setContentView(R.layout.dialog_check_password_edit)
        val window = dialog.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val user= SPUtil.getObj("user", User::class.java)
        val checkPassword=SPUtil.getObj("${user?.accountId}CheckPassword",
            CheckPassword::class.java)

        val btn_ok = dialog.findViewById<Button>(R.id.btn_ok)
        val btn_cancel = dialog.findViewById<Button>(R.id.btn_cancel)

        val etPassword=dialog.findViewById<EditText>(R.id.et_password)
        val etPasswordAgain=dialog.findViewById<EditText>(R.id.et_password_again)
        val etPasswordOld=dialog.findViewById<EditText>(R.id.et_password_old)


        btn_cancel?.setOnClickListener { dialog.dismiss() }
        btn_ok?.setOnClickListener {
            val passwordStr=etPassword?.text.toString()
            val passwordAgainStr=etPasswordAgain?.text.toString()
            val passwordOldStr=etPasswordOld?.text.toString()

            if (MD5Utils.digest(passwordOldStr)!=checkPassword?.password){
                SToast.showText("原密码输入错误")
                return@setOnClickListener
            }
            if (passwordStr.isEmpty()||passwordAgainStr.isEmpty()){
                SToast.showText("请输入密码")
                return@setOnClickListener
            }
            if (passwordStr!=passwordAgainStr){
                SToast.showText("密码输入不一致")
                return@setOnClickListener
            }
            checkPassword?.password= MD5Utils.digest(checkPassword?.password)
            SPUtil.putObj("${user?.accountId}CheckPassword",checkPassword!!)
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