package com.bll.lnkcommon.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.CountDownTimer
import android.view.View
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.mvp.presenter.RegisterPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.SPUtil
import com.bll.lnkcommon.utils.ToolUtils
import kotlinx.android.synthetic.main.ac_account_register.*


class AccountRegisterActivity : BaseActivity(), IContractView.IRegisterView {

    private val presenter= RegisterPresenter(this)
    private var countDownTimer: CountDownTimer? = null
    private var flags = 0

    override fun onSms() {
        showToast("发送验证码成功")
        showCountDownView()
    }

    override fun onRegister() {
        showToast("注册成功")
        setIntent()
    }
    override fun onFindPsd() {
        showToast("设置密码成功")
        setIntent()
    }

    override fun onEditPsd() {
        showToast("修改密码成功")
        setIntent()
    }

    override fun layoutId(): Int {
        return R.layout.ac_account_register
    }

    override fun initData() {
        flags=intent.flags
    }

    override fun initView() {

        when (flags) {
            2 -> {
                setPageTitle("修改密码")
                disMissView(ll_name,ll_user)
                btn_register.text="提交"
            }
            1 -> {
                setPageTitle("找回密码")
                disMissView(ll_name)
                ed_user.setText(SPUtil.getString("account"))
                btn_register.text="提交"
            }
            else -> {
                setPageTitle("注册账号")
            }
        }

        btn_code.setOnClickListener {
            val phone=ed_phone.text.toString().trim()
            if (!ToolUtils.isPhoneNum(phone)) {
                showToast(getString(R.string.phone_tip))
                return@setOnClickListener
            }
            presenter.sms(phone)
        }

        btn_register.setOnClickListener {

            val account=ed_user.text.toString().trim()
            val psd=ed_password.text.toString().trim()
            val name=ed_name.text.toString().trim()
            val phone=ed_phone.text.toString().trim()
            val code=ed_code.text.toString().trim()
            val role=3

            if (psd.isEmpty()) {
                showToast("请输入密码")
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                showToast("请输入电话号码")
                return@setOnClickListener
            }

            if (code.isEmpty()) {
                showToast("请输入验证码")
                return@setOnClickListener
            }

            if (!ToolUtils.isLetterOrDigit(psd, 6, 20)) {
                showToast(getString(R.string.psw_tip))
                return@setOnClickListener
            }

            if (!ToolUtils.isPhoneNum(phone)) {
                showToast(getString(R.string.phone_tip))
                return@setOnClickListener
            }
            when (flags) {
                0 -> {
                    if (account.isEmpty()) {
                        showToast("请输入用户名")
                        return@setOnClickListener
                    }
                    if (name.isEmpty()) {
                        showToast("请输入姓名")
                        return@setOnClickListener
                    }
                    if (!ToolUtils.isLetterOrDigit(account, 4, 12)) {
                        showToast(getString(R.string.user_tip))
                        return@setOnClickListener
                    }
                    val map=HashMap<String,Any>()
                    map["account"]=account
                    map["password"]=MD5Utils.digest(psd)
                    map["nickname"]=name
                    map["code"]=code
                    map["telNumber"]=phone
                    map["role"]=role
                    presenter.register(map)
                }
                1 -> {
                    if (account.isEmpty()) {
                        showToast("请输入用户名")
                        return@setOnClickListener
                    }
                    presenter.findPsd(role.toString(),account,MD5Utils.digest(psd),phone, code)
                }
                else -> {
                    presenter.editPsd(MD5Utils.digest(psd),code)
                }
            }

        }

    }

    //验证码倒计时刷新ui
    private fun showCountDownView() {
        btn_code.isEnabled = false
        btn_code.isClickable = false
        countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                runOnUiThread {
                    btn_code.isEnabled = true
                    btn_code.isClickable = true
                    btn_code.text = "获取验证码"
                }

            }
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    btn_code.text = "${millisUntilFinished / 1000}s"
                }
            }
        }.start()

    }

    private fun setIntent(){
        val intent = Intent()
        intent.putExtra("user", ed_user.text.toString())
        intent.putExtra("psw", ed_password.text.toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
