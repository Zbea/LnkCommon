package com.bll.lnkcommon.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.mvp.presenter.LoginPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.utils.*
import kotlinx.android.synthetic.main.ac_account_login_user.*
import pub.devrel.easypermissions.EasyPermissions

class AccountLoginActivity: BaseActivity(), IContractView.ILoginView {

    private val presenter= LoginPresenter(this)
    private var token=""

    override fun getLogin(user: User?) {
        token= user?.token.toString()
        SPUtil.putString("token",token)
        presenter.getStudents()
    }

    override fun getAccount(user: User?) {
        user?.token=token
        user?.isBind=DataBeanManager.students.size!=0
        SPUtil.putObj("user",user!!)
        startActivity(Intent(this,MainActivity::class.java))
        ActivityManager.getInstance().finishOthers(MainActivity::class.java)
    }

    override fun onStudentList(studentBeans: MutableList<StudentBean>) {
        DataBeanManager.students=studentBeans
        presenter.accounts()
    }

    override fun layoutId(): Int {
        return R.layout.ac_account_login_user
    }

    override fun initData() {
    }

    @SuppressLint("WrongConstant")
    override fun initView() {

        ed_user.setText("zhufeng4")
        ed_psw.setText("123456")


        tv_register.setOnClickListener {
            startActivityForResult(Intent(this, AccountRegisterActivity::class.java).setFlags(0), 0)
        }

        tv_find_psd.setOnClickListener {
            startActivityForResult(Intent(this, AccountRegisterActivity::class.java).setFlags(1), 0)
        }

        btn_login.setOnClickListener {

            val account = ed_user.text.toString()
            val password = MD5Utils.digest(ed_psw.text.toString())

            val map=HashMap<String,Any>()
            map ["account"]=account
            map ["password"]=password
            map ["role"]= 3
            presenter.login(map)
        }

        val tokenStr=SPUtil.getString("token")

        if (tokenStr.isNotEmpty() && mUser!=null)
        {
            startActivity(Intent(this,MainActivity::class.java))
            ActivityManager.getInstance().finishOthers(MainActivity::class.java)
        }

    }

    override fun onResume() {
        super.onResume()
        if (NetworkUtil.isNetworkAvailable(this)) {
            disMissView(tv_tips)
        } else {
            showView(tv_tips)
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