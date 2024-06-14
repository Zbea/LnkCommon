package com.bll.lnkcommon.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.mvp.presenter.LoginPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.utils.*
import kotlinx.android.synthetic.main.ac_account_login_user.*
import org.greenrobot.eventbus.EventBus

class AccountLoginActivity: BaseActivity(), IContractView.ILoginView {

    private val presenter= LoginPresenter(this)
    private var token=""
    private var statusBarValue=0

    override fun getLogin(user: User?) {
        token= user?.token.toString()
        SPUtil.putString("token",token)
        presenter.accounts()
    }

    override fun getAccount(user: User?) {
        user?.token=token
        SPUtil.putObj("user",user!!)
        EventBus.getDefault().post(Constants.USER_EVENT)

        MethodManager.setStatusBarValue(statusBarValue)

        val intent = Intent()
        intent.putExtra("token", token)
        intent.putExtra("userId", user.accountId)
        intent.action = Constants.LOGIN_BROADCAST_EVENT
        sendBroadcast(intent)

        finish()
    }

    override fun layoutId(): Int {
        return R.layout.ac_account_login_user
    }

    override fun initData() {
    }

    @SuppressLint("WrongConstant")
    override fun initView() {
        statusBarValue= MethodManager.getStatusBarValue()
        MethodManager.setStatusBarValue(Constants.STATUS_BAR_SHOW)

        val account=SPUtil.getString("account")
        val password=SPUtil.getString("password")

        ed_user.setText(account)
        ed_psw.setText(password)

        tv_register.setOnClickListener {
            startActivityForResult(Intent(this, AccountRegisterActivity::class.java).setFlags(0), 0)
        }

        tv_find_psd.setOnClickListener {
            startActivityForResult(Intent(this, AccountRegisterActivity::class.java).setFlags(1), 0)
        }

        btn_login.setOnClickListener {

            val account = ed_user.text.toString()
            val psdStr=ed_psw.text.toString()
            val password = MD5Utils.digest(psdStr)

            SPUtil.putString("account",account)
            SPUtil.putString("password",psdStr)

            val map=HashMap<String,Any>()
            map ["account"]=account
            map ["password"]=password
            map ["role"]= 3
            presenter.login(map)
        }
    }

    override fun onResume() {
        super.onResume()
        if (NetworkUtil.isNetworkAvailable(this)) {
            disMissView(ll_tips)
        } else {
            showView(ll_tips)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            ed_user.setText(data?.getStringExtra("user"))
            ed_psw.setText(data?.getStringExtra("psw"))
        }
    }

}