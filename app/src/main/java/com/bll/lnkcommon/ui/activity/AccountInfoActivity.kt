package com.bll.lnkcommon.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.presenter.AccountInfoPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.AccountStudentAdapter
import com.bll.lnkcommon.utils.ActivityManager
import com.bll.lnkcommon.utils.SPUtil
import kotlinx.android.synthetic.main.ac_account_info.*
import kotlinx.android.synthetic.main.ac_account_info.rv_list
import kotlinx.android.synthetic.main.fragment_app.*

class AccountInfoActivity:BaseActivity(), IContractView.IAccountInfoView {

    private val presenter=AccountInfoPresenter(this)
    private var nickname=""
    private var students= mutableListOf<StudentBean>()
    private var mAdapter: AccountStudentAdapter?=null

    override fun onEditNameSuccess() {
        showToast("修改姓名成功")
        mUser?.nickname=nickname
        tv_name.text = nickname
    }

    override fun layoutId(): Int {
        return R.layout.ac_account_info
    }

    override fun initData() {
    }

    @SuppressLint("WrongConstant")
    override fun initView() {

        setPageTitle("我的账户")

        initRecyclerView()

        mUser?.apply {
            tv_user.text = account
            tv_name.text = nickname
            tv_phone.text =  telNumber.substring(0,3)+"****"+telNumber.substring(7,11)
        }

        btn_edit_psd.setOnClickListener {
            startActivity(Intent(this,AccountRegisterActivity::class.java).setFlags(2))
        }

        btn_edit_name.setOnClickListener {
            editName()
        }

        btn_add.setOnClickListener {
            addStudent()
        }

        btn_logout.setOnClickListener {
            CommonDialog(this).setContent("确认退出登录？").builder().setDialogClickListener(object :
                CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    SPUtil.putString("token", "")
                    SPUtil.removeObj("user")
                    startActivity(Intent(this@AccountInfoActivity, AccountLoginActivity::class.java))
                    ActivityManager.getInstance().finishOthers(AccountLoginActivity::class.java)
                }
            })
        }

        students.add(StudentBean().apply{
            id=123
            name="朱"
            grade=3
        })

        mAdapter?.setNewData(students)

    }

    private fun initRecyclerView(){
        rv_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mAdapter = AccountStudentAdapter(R.layout.item_account_student,null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setOnItemClickListener { adapter, view, position ->

        }
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            if (view.id==R.id.tv_student_cancel){
                CommonDialog(this).setContent("确定取消学生关联？").builder().setDialogClickListener(object :
                    CommonDialog.OnDialogClickListener {
                    override fun cancel() {
                    }
                    override fun ok() {

                    }
                })
            }
        }
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
     * 关联学生
     */
    private fun addStudent(){
        InputContentDialog(this,"请输入学生号").builder()
            .setOnDialogClickListener { string ->

            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mUser?.let { SPUtil.putObj("user", it) }
    }

}