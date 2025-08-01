package com.bll.lnkcommon.ui.activity.account

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.*
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.presenter.AccountInfoPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.AccountStudentAdapter
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.mvp.presenter.SmsPresenter
import com.bll.lnkcommon.mvp.view.IContractView.ISmsView
import com.bll.lnkcommon.utils.SPUtil
import com.bll.lnkcommon.utils.ToolUtils
import kotlinx.android.synthetic.main.ac_account_info.*
import kotlinx.android.synthetic.main.ac_account_info.rv_list
import org.greenrobot.eventbus.EventBus

class AccountInfoActivity:BaseActivity(), IContractView.IAccountInfoView,ISmsView {
    private val smsPresenter= SmsPresenter(this)
    private val presenter=AccountInfoPresenter(this)
    private var nickname=""
    private var students= mutableListOf<StudentBean>()
    private var mAdapter: AccountStudentAdapter?=null
    private var position=0
    private var phone=""
    private var type=0

    override fun onSms() {
        showToast("短信发送成功")
        if (type==0){
            InputContentDialog(this,"请输入验证码",1).builder().setOnDialogClickListener{
                smsPresenter.checkPhone(it)
            }
        }
    }
    override fun onCheckSuccess() {
        editPhone()
    }

    override fun onEditPhone() {
        showToast("修改手机号码成功")
        mUser?.telNumber=phone
        tv_phone.text=getPhoneStr(phone)
    }

    override fun onEditNameSuccess() {
        showToast("修改姓名成功")
        mUser?.nickname=nickname
        tv_name.text = nickname
    }
    override fun onBind() {
        presenter.getStudents()
    }
    override fun onUnbind() {
        mAdapter?.remove(position)
        DataBeanManager.students=students
        EventBus.getDefault().post(Constants.STUDENT_EVENT)
    }
    override fun onListStudent(bens: MutableList<StudentBean>) {
        students=bens
        mAdapter?.setNewData(students)
        if (DataBeanManager.students!=bens){
            DataBeanManager.students=bens
            EventBus.getDefault().post(Constants.STUDENT_EVENT)
        }
    }


    override fun layoutId(): Int {
        return R.layout.ac_account_info
    }

    override fun initData() {
        mUser=MethodManager.getUser()
        presenter.getStudents()
    }

    @SuppressLint("WrongConstant")
    override fun initView() {
        setPageTitle("我的账户")

        initRecyclerView()

        mUser?.apply {
            tv_user.text = account
            tv_name.text = nickname
            tv_phone.text =getPhoneStr(telNumber)
        }

        btn_edit_name.setOnClickListener {
            editName()
        }

        btn_add.setOnClickListener {
            add()
        }

        btn_edit_phone.setOnClickListener {
            type=0
            smsPresenter.sms(mUser?.telNumber!!)
        }

        btn_edit_password.setOnClickListener {
            customStartActivity(Intent(this, AccountRegisterActivity::class.java).setFlags(1))
        }

        btn_logout.setOnClickListener {
            CommonDialog(this).setContent("退出登录？").builder().setDialogClickListener(object :
                CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    mUser=null
                    MethodManager.logout(this@AccountInfoActivity)
                }
            })
        }

    }

    private fun initRecyclerView(){
        rv_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mAdapter = AccountStudentAdapter(R.layout.item_account_student,null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            this.position=position
            when(view.id){
                R.id.tv_student_cancel->{
                    cancel()
                }
                R.id.tv_set->{
                    val intent = Intent(this, PermissionSettingActivity::class.java)
                    val bundle = Bundle()
                    bundle.putSerializable("studentInfo", students[position])
                    intent.putExtra("bundle", bundle)
                    customStartActivity(intent)
                }
            }
        }
    }

    private fun getPhoneStr(phone:String):String{
        return if (ToolUtils.isPhoneNum(phone)) phone.substring(0, 3) + "****" + phone.substring(7, 11) else ""
    }

    private fun editPhone(){
        EditPhoneDialog(this).builder().setOnDialogClickListener(object : EditPhoneDialog.OnDialogClickListener {
            override fun onClick(code: String, phone: String) {
                this@AccountInfoActivity.phone=phone
                presenter.editPhone(code, phone)
            }
            override fun onPhone(phone: String) {
                type=1
                smsPresenter.sms(phone)
            }
        })
    }

    /**
     * 修改名称
     */
    private fun editName(){
        InputContentDialog(this,tv_name.text.toString()).builder()
            .setOnDialogClickListener { string ->
                nickname = string
                presenter.editName(nickname)
            }
    }

    /**
     * 关联
     */
    private fun add(){
        InputContentDialog(this,"输入学生账号").builder()
            .setOnDialogClickListener { string ->
                presenter.onBindStudent(string)
            }
    }

    /**
     * 取消关联
     */
    private fun cancel(){
        CommonDialog(this).setContent( "取消学生关联?").builder().setDialogClickListener(object :
            CommonDialog.OnDialogClickListener {
            override fun cancel() {
            }
            override fun ok() {
                presenter.unbindStudent(students[position].accountId)
            }
        })
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (Constants.REFRESH_STUDENT_PERMISSION_EVENT==msgFlag){
            presenter.getStudents()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mUser?.let { SPUtil.putObj("user", it) }
    }

    override fun onRefreshData() {
        presenter.getStudents()
    }

}