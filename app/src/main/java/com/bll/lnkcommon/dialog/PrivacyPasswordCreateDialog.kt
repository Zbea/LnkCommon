package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.PrivacyPassword
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.SPUtil
import com.bll.lnkcommon.utils.SToast


class PrivacyPasswordCreateDialog(private val context: Context) {

    private val popWindowBeans= mutableListOf<PopupBean>()

    fun builder(): PrivacyPasswordCreateDialog {
        val dialog= Dialog(context)
        dialog.setContentView(R.layout.dialog_check_password_create)
        val window = dialog.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        popWindowBeans.add(
            PopupBean(
                0,
                "您的姓名是？",
                false
            )
        )
        popWindowBeans.add(
            PopupBean(
                1,
                "父亲姓名是？",
                false
            )
        )
        popWindowBeans.add(
            PopupBean(
                2,
                "您的生日是？",
                false
            )
        )
        popWindowBeans.add(
            PopupBean(
                3,
                "最喜欢电影？",
                false
            )
        )

        val btn_ok = dialog.findViewById<Button>(R.id.btn_ok)
        val btn_cancel = dialog.findViewById<Button>(R.id.btn_cancel)

        val etPassword=dialog.findViewById<EditText>(R.id.et_password)
        val etPasswordAgain=dialog.findViewById<EditText>(R.id.et_password_again)
        val etPasswordQuestion=dialog.findViewById<EditText>(R.id.et_question_password)
        val tvQuestion=dialog.findViewById<TextView>(R.id.tv_question_password)
        tvQuestion.setOnClickListener {
            PopupRadioList(context, popWindowBeans, tvQuestion, 5).builder()
            .setOnSelectListener { item ->
                tvQuestion.text = item.name
            }
        }

        btn_cancel?.setOnClickListener { dialog.dismiss() }
        btn_ok?.setOnClickListener {
            val passwordStr=etPassword?.text.toString()
            val passwordAgainStr=etPasswordAgain?.text.toString()
            val answerStr=etPasswordQuestion?.text.toString()
            val questionStr=tvQuestion?.text.toString()
            if (questionStr=="选择问题"){
                return@setOnClickListener
            }
            if (answerStr.isEmpty()){
                SToast.showText("请输入密保问题")
                return@setOnClickListener
            }

            if (passwordStr.isEmpty()){
                SToast.showText("请输入密码")
                return@setOnClickListener
            }
            if (passwordAgainStr.isEmpty()){
                SToast.showText("请再次输入密码")
                return@setOnClickListener
            }

            if (passwordStr!=passwordAgainStr){
                SToast.showText("密码输入不一致")
                return@setOnClickListener
            }
            val privacyPassword= PrivacyPassword()
            privacyPassword.question=tvQuestion.text.toString()
            privacyPassword.answer=answerStr
            privacyPassword.password= MD5Utils.digest(passwordStr)
            val user= SPUtil.getObj("user", User::class.java)
            SPUtil.putObj("${user?.accountId}PrivacyPassword",privacyPassword)

            dialog.dismiss()
            listener?.onClick(privacyPassword)

        }

        dialog.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }

        return this
    }


    private var listener: OnDialogClickListener? = null

    fun interface OnDialogClickListener {
        fun onClick(privacyPassword: PrivacyPassword)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

}