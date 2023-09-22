package com.bll.lnkcommon.ui.activity

import android.annotation.SuppressLint
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.*
import com.bll.lnkcommon.mvp.model.CheckPassword
import com.bll.lnkcommon.mvp.model.FriendList
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.presenter.AccountInfoPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.AccountFriendAdapter
import com.bll.lnkcommon.ui.adapter.AccountStudentAdapter
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.utils.SPUtil
import kotlinx.android.synthetic.main.ac_account_info.*
import kotlinx.android.synthetic.main.ac_account_info.rv_list
import kotlinx.android.synthetic.main.common_title.*
import kotlinx.android.synthetic.main.fragment_app.*
import org.greenrobot.eventbus.EventBus

class AccountInfoActivity:BaseActivity(), IContractView.IAccountInfoView {

    private val presenter=AccountInfoPresenter(this)
    private var nickname=""
    private var students= mutableListOf<StudentBean>()
    private var mAdapter: AccountStudentAdapter?=null
    private var friends= mutableListOf<FriendList.FriendBean>()
    private var requestfriends= mutableListOf<FriendList.FriendBean>()
    private var mAdapterFriend: AccountFriendAdapter?=null
    private var position=0
    private var type=0//0学生1好友
    private var requestPosition=0
    private var checkPassword: CheckPassword?=null

    override fun onEditNameSuccess() {
        showToast("修改姓名成功")
        mUser?.nickname=nickname
        tv_name.text = nickname
    }
    override fun onBind() {
        if (type==0){
            presenter.getStudents()
        }
    }
    override fun onUnbind() {
        if (type==0){
            mAdapter?.remove(position)
            DataBeanManager.students=students
            if (DataBeanManager.students.size==0)
                EventBus.getDefault().post(Constants.STUDENT_EVENT)
        }
        else{
            mAdapterFriend?.remove(position)
            DataBeanManager.friends=friends
        }
    }
    override fun onListStudent(bens: MutableList<StudentBean>) {
        DataBeanManager.students=bens
        students=bens
        mAdapter?.setNewData(students)
        EventBus.getDefault().post(Constants.STUDENT_EVENT)
    }
    override fun onListFriend(list: FriendList) {
        friends=list.list
        DataBeanManager.friends=friends
        mAdapterFriend?.setNewData(friends)
    }
    override fun onAgree() {
        requestfriends.removeAt(requestPosition)
        presenter.getFriends()
    }
    override fun onDisagree() {
        requestfriends.removeAt(requestPosition)
    }
    override fun onListRequestFriend(list: FriendList) {
        requestfriends=list.list
    }

    override fun layoutId(): Int {
        return R.layout.ac_account_info
    }

    override fun initData() {
        mUser=getUser()
        presenter.getStudents()
        presenter.getFriends()
        presenter.getRequestFriends()
        checkPassword=SPUtil.getObj("${mUser?.accountId}CheckPassword", CheckPassword::class.java)
    }

    @SuppressLint("WrongConstant")
    override fun initView() {
        setPageTitle("我的账户")
        showView(iv_manager)
        iv_manager.setImageResource(R.mipmap.icon_friend_add)

        initRecyclerView()
        initRecyclerViewFriend()

        mUser?.apply {
            tv_user.text = account
            tv_name.text = nickname
            tv_phone.text =  telNumber.substring(0,3)+"****"+telNumber.substring(7,11)
        }

        if (checkPassword!=null){
            showView(tv_check_pad)
            if (checkPassword?.isSet == true){
                btn_psd_check.text="取消密码"
            }
            else{
                btn_psd_check.text="设置密码"
            }
        }


        iv_manager?.setOnClickListener {
            PopupFriendRequestList(this,iv_manager,requestfriends).builder()
                .setOnSelectListener{ position,type->
                    requestPosition=position
                    if (type==1) {
                        presenter.disagreeFriend(requestfriends[position].id)
                    }
                    else{
                        presenter.onAgreeFriend(requestfriends[position].id)
                    }
            }
        }


        btn_edit_name.setOnClickListener {
            editName()
        }

        btn_add.setOnClickListener {
            type=0
            add()
        }

        btn_add_friend.setOnClickListener {
            type=1
            add()
        }

        btn_psd_check.setOnClickListener {
            setPassword()
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
            if (view.id==R.id.tv_student_cancel){
                type=0
                cancel()
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
                type=1
                cancel()
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
     * 关联
     */
    private fun add(){
        val str=if (type==0) "输入学生账号" else "输入好友账号"
        InputContentDialog(this,str).builder()
            .setOnDialogClickListener { string ->
                if (type==0){
                    presenter.onBindStudent(string)
                }
                else{
                    presenter.onBindFriend(string)
                }
            }
    }

    /**
     * 取消关联
     */
    private fun cancel(){
        val str=if (type==0) "取消学生关联?" else "取消好友关联?"
        CommonDialog(this).setContent(str).builder().setDialogClickListener(object :
            CommonDialog.OnDialogClickListener {
            override fun cancel() {
            }
            override fun ok() {
                if (type==0){
                    presenter.unbindStudent(students[position].childId)
                }
                else{
                    presenter.unbindFriend(friends[position].id)
                }
            }
        })
    }

    /**
     * 设置查看密码
     */
    private fun setPassword(){
        if (checkPassword==null){
            CheckPasswordCreateDialog(this).builder().setOnDialogClickListener{
                checkPassword=it
                showView(tv_check_pad)
                btn_psd_check.text="设置密码"
                SPUtil.putObj("${mUser?.accountId}CheckPassword",checkPassword!!)
                EventBus.getDefault().post(Constants.CHECK_PASSWORD_EVENT)
            }
        }
        else{
            CheckPasswordDialog(this).builder()?.setOnDialogClickListener{
                checkPassword?.isSet=!checkPassword?.isSet!!
                btn_psd_check.text=if (checkPassword?.isSet==true) "取消密码" else "设置密码"
                SPUtil.putObj("${mUser?.accountId}CheckPassword",checkPassword!!)
                EventBus.getDefault().post(Constants.CHECK_PASSWORD_EVENT)
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        mUser?.let { SPUtil.putObj("user", it) }
    }

}