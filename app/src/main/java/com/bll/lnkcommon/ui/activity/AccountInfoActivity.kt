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
import com.bll.lnkcommon.ui.adapter.AccountFriendAdapter
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
    private var friends= mutableListOf<StudentBean>()
    private var mAdapterFriend: AccountFriendAdapter?=null
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
        initRecyclerViewFriend()

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
            add(0)
        }

        btn_add_friend.setOnClickListener {
            add(1)
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
                cancel(0)
            }
        }
    }

    private fun initRecyclerViewFriend(){
        rv_list_friend.layoutManager = LinearLayoutManager(this)//创建布局管理
        mAdapterFriend = AccountFriendAdapter(R.layout.item_account_friend,null)
        rv_list_friend.adapter = mAdapterFriend
        mAdapterFriend?.bindToRecyclerView(rv_list_friend)
        mAdapterFriend?.setOnItemChildClickListener { adapter, view, position ->
            this.position=position
            if (view.id==R.id.tv_friend_cancel){
                cancel(1)
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
     * 管理
     */
    private fun add(type:Int){
        val str=if (type==0) "输入学生账号" else "输入好友账号"
        InputContentDialog(this,str).builder()
            .setOnDialogClickListener { string ->
                if (type==0){
                    presenter.onBindStudent(string)
                }
            }
    }

    private fun cancel(type: Int){
        val str=if (type==0) "取消学生关联?" else "取消好友关联?"
        CommonDialog(this).setContent(str).builder().setDialogClickListener(object :
            CommonDialog.OnDialogClickListener {
            override fun cancel() {
            }
            override fun ok() {
                if (type==0){
                    presenter.unbindStudent(students[position].childId)
                }
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        mUser?.let { SPUtil.putObj("user", it) }
    }

}