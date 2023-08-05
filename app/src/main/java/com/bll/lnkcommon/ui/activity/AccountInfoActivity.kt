package com.bll.lnkcommon.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.mvp.presenter.AccountInfoPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.AccountStudentAdapter
import com.bll.lnkcommon.utils.ActivityManager
import com.bll.lnkcommon.utils.SPUtil
import com.google.gson.Gson
import kotlinx.android.synthetic.main.ac_account_info.*
import kotlinx.android.synthetic.main.ac_account_info.rv_list
import kotlinx.android.synthetic.main.fragment_app.*
import org.greenrobot.eventbus.EventBus

class AccountInfoActivity:BaseActivity(), IContractView.IAccountInfoView {

    private val presenter=AccountInfoPresenter(this)
    private var nickname=""
    private var students= mutableListOf<StudentBean>()
    private var mAdapter: AccountStudentAdapter?=null
    private var position=0

    override fun onEditNameSuccess() {
        showToast("修改姓名成功")
        mUser?.nickname=nickname
        tv_name.text = nickname
    }
    override fun onBindStudent() {
        presenter.getStudents()
    }
    override fun onUnbindStudent() {
        mAdapter?.remove(position)
        DataBeanManager.students=students
        if (DataBeanManager.students.size==0){
            mUser?.isBind=false
            EventBus.getDefault().post(Constants.STUDENT_EVENT)
        }
    }
    override fun onStudentList(studentBeans: MutableList<StudentBean>) {
        if (studentBeans.size>0)
            mUser?.isBind=true
        DataBeanManager.students=studentBeans
        students=studentBeans
        mAdapter?.setNewData(students)
        EventBus.getDefault().post(Constants.STUDENT_EVENT)
    }

    override fun layoutId(): Int {
        return R.layout.ac_account_info
    }

    override fun initData() {
        presenter.getStudents()
    }

    @SuppressLint("WrongConstant")
    override fun initView() {
        setPageTitle("我的账户")
        initRecyclerView()

        mUser=getUser()

        mUser?.apply {
            tv_user.text = account
            tv_name.text = nickname
            tv_phone.text =  telNumber.substring(0,3)+"****"+telNumber.substring(7,11)
        }

        btn_edit_psd.setOnClickListener {
            customStartActivity(Intent(this,AccountRegisterActivity::class.java).setFlags(2))
        }

        btn_edit_name.setOnClickListener {
            editName()
        }

        btn_add.setOnClickListener {
            addStudent()
        }

        btn_logout.setOnClickListener {
            CommonDialog(this).setContent("退出登录？").builder().setDialogClickListener(object :
                CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    mUser=null
                    SPUtil.putString("token", "")
                    SPUtil.removeObj("user")
                    ActivityManager.getInstance().finishOthers(MainActivity::class.java)
                    EventBus.getDefault().post(Constants.USER_EVENT)
                    DataBeanManager.students.clear()
                    EventBus.getDefault().post(Constants.STUDENT_EVENT)
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
            if (view.id==R.id.tv_student_cancel){
                CommonDialog(this).setContent("取消学生关联？").builder().setDialogClickListener(object :
                    CommonDialog.OnDialogClickListener {
                    override fun cancel() {
                    }
                    override fun ok() {
                        presenter.unbindStudent(students[position].childId)
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
        InputContentDialog(this,"输入学生账号").builder()
            .setOnDialogClickListener { string ->
                presenter.onBindStudent(string)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mUser?.let { SPUtil.putObj("user", it) }
    }

}