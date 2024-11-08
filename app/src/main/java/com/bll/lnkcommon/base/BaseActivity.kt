package com.bll.lnkcommon.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.dialog.ProgressDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.book.Book
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.net.ExceptionHandle
import com.bll.lnkcommon.net.IBaseView
import com.bll.lnkcommon.ui.adapter.TabTypeAdapter
import com.bll.lnkcommon.utils.*
import com.bll.lnkcommon.widget.FlowLayoutManager
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.ac_list_tab.*
import kotlinx.android.synthetic.main.common_page_number.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import kotlin.math.ceil


abstract class BaseActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, IBaseView {

    var mDialog: ProgressDialog? = null
    var mSaveState:Bundle?=null
    var pageIndex=1 //当前页码
    var pageCount=1 //全部数据
    var pageSize=0 //一页数据
    var mUser:User?=null
    var mTabTypeAdapter:TabTypeAdapter?=null
    var itemTabTypes= mutableListOf<ItemTypeBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSaveState=savedInstanceState
        setContentView(layoutId())
        initCommonTitle()
        EventBus.getDefault().register(this)
        setStatusBarColor(ContextCompat.getColor(this, R.color.white))

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE
            )){
            EasyPermissions.requestPermissions(this,"请求权限",1,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE
            )
        }

        mDialog = ProgressDialog(this)
        if (rv_tab!=null){
            initTabView()
        }
        initCreate()
        initData()
        initView()
    }

    /**
     *  加载布局
     */
    abstract fun layoutId(): Int

    /**
     * 初始化数据
     */
    abstract fun initData()

    /**
     * 初始化 View
     */
    abstract fun initView()

    /**
     * 初始化onCreate
     */
    open fun initCreate(){
    }

    @SuppressLint("WrongViewCast")
    fun initCommonTitle() {

        iv_back?.setOnClickListener { finish() }

        btn_page_up?.setOnClickListener {
            if(pageIndex>1){
                pageIndex-=1
                fetchData()
            }
        }

        btn_page_down?.setOnClickListener {
            if(pageIndex<pageCount){
                pageIndex+=1
                fetchData()
            }
        }
    }


    fun showBackView(isShow:Boolean) {
        if (isShow){
            showView(iv_back)
        }
        else{
            disMissView(iv_back)
        }
    }

    fun setPageTitle(pageTitle: String) {
        tv_title?.text = pageTitle
    }

    fun setPageTitle(titleId: Int) {
        tv_title?.setText(titleId)
    }

    fun setSettingText(s: String){
        showView(tv_setting)
        tv_setting.text=s
    }

    fun setImageBtn(resId: Int){
        showView(iv_manager)
        if (resId!=0){
            iv_manager.setImageResource(resId)
        }
    }

    private fun initTabView(){
        rv_tab.layoutManager = FlowLayoutManager()//创建布局管理
        mTabTypeAdapter = TabTypeAdapter(R.layout.item_tab_type, null).apply {
            rv_tab.adapter = this
            bindToRecyclerView(rv_tab)
            setOnItemClickListener { adapter, view, position ->
                for (item in itemTabTypes){
                    item.isCheck=false
                }
                if (position<itemTabTypes.size){
                    val item=itemTabTypes[position]
                    item.isCheck=true
                    mTabTypeAdapter?.notifyDataSetChanged()

                    onTabClickListener(view,position)
                }
            }
        }
    }

    /**
     * tab点击监听
     */
    open fun onTabClickListener(view:View, position:Int){

    }

    /**
     * 显示view
     */
    fun showView(view: View?) {
        if (view != null && view.visibility != View.VISIBLE) {
            view.visibility = View.VISIBLE
        }
    }

    /**
     * 显示view
     */
    fun showView(vararg views: View?) {
        for (view in views) {
            if (view != null && view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
            }
        }
    }


    /**
     * 消失view
     */
    fun disMissView(view: View?) {
        if (view != null && view.visibility != View.GONE) {
            view.visibility = View.GONE
        }
    }

    /**
     * 消失view
     */
    fun disMissView(vararg views: View?) {
        for (view in views) {
            if (view != null && view.visibility != View.GONE) {
                view.visibility = View.GONE
            }
        }
    }

    fun getUser():User?{
        return SPUtil.getObj("user",User::class.java)
    }

    /**
     * 设置翻页
     */
    fun setPageNumber(total:Int){
        if (ll_page_number!=null){
            pageCount = ceil(total.toDouble() / pageSize).toInt()
            if (total == 0) {
                disMissView(ll_page_number)
            } else {
                tv_page_current.text = pageIndex.toString()
                tv_page_total.text = pageCount.toString()
                showView(ll_page_number)
            }
        }
    }

    /**
     * 跳转活动(关闭已经打开的)
     */
    fun customStartActivity(intent: Intent){
        ActivityManager.getInstance().finishActivity(intent.component?.className)
        startActivity(intent)
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected fun setStatusBarColor(statusColor: Int) {
        val window = window
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //设置状态栏颜色
        window.statusBarColor = statusColor
        //设置系统状态栏处于可见状态
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        //让view不根据系统窗口来调整自己的布局
        val mContentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false)
            ViewCompat.requestApplyInsets(mChildView)
        }
    }


    /**
     * 打卡软键盘
     */
    fun openKeyBord(mEditText: EditText, mContext: Context) {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    /**
     * 关闭软键盘
     */
    fun closeKeyBord(mEditText: EditText, mContext: Context) {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mEditText.windowToken, 0)
    }

    /**
     * 关闭软键盘
     */
    fun hideKeyboard(){
        KeyboardUtils.hideSoftKeyboard(this)
    }

    fun showToast(s:String){
        SToast.showText(s)
    }

    fun showToast(resId:Int){
        SToast.showText(resId)
    }

    fun showLog(s:String){
        Log.d("debug",s)
    }

    fun showLog(resId:Int){
        Log.d("debug",getString(resId))
    }

    /**
     * 重写要申请权限的Activity或者Fragment的onRequestPermissionsResult()方法，
     * 在里面调用EasyPermissions.onRequestPermissionsResult()，实现回调。
     *
     * @param requestCode  权限请求的识别码
     * @param permissions  申请的权限
     * @param grantResults 授权结果
     */
    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    /**
     * 当权限被成功申请的时候执行回调
     *
     * @param requestCode 权限请求的识别码
     * @param perms       申请的权限的名字
     */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.i("EasyPermissions", getString(R.string.permission_successfully)+perms)
    }
    /**
     * 当权限申请失败的时候执行的回调
     *
     * @param requestCode 权限请求的识别码
     * @param perms       申请的权限的名字
     */
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        //处理权限名字字符串
        val sb = StringBuffer()
        for (str in perms) {
            sb.append(str)
            sb.append("\n")
        }
        sb.replace(sb.length - 2, sb.length, "")
        //用户点击拒绝并不在询问时候调用
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Toast.makeText(this, getString(R.string.permission_denied) + sb + getString(R.string.permission_ask_no_more), Toast.LENGTH_SHORT).show()
            AppSettingsDialog.Builder(this)
                    .setRationale(getString(R.string.permission_this_function_requires) + sb + getString(R.string.permission_unusable))
                    .setPositiveButton(R.string.ok)
                    .setNegativeButton(R.string.cancel)
                    .build()
                    .show()
        }
    }

    override fun addSubscription(d: Disposable) {
    }
    override fun login() {
        MethodManager.logoutFailure(this)
    }

    override fun hideLoading() {
        mDialog?.dismiss()
    }

    override fun showLoading() {
        mDialog!!.show()
    }

    override fun fail(msg: String) {
        showToast(msg)
    }

    override fun onFailer(responeThrowable: ExceptionHandle.ResponeThrowable?) {
        showLog(R.string.connect_server_timeout)
    }
    override fun onComplete() {
        showLog(R.string.request_success)
    }

    override fun onPause() {
        super.onPause()
        mDialog!!.dismiss()
    }

    //更新数据
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(msgFlag: String) {
        when(msgFlag){
            Constants.NETWORK_CONNECTION_COMPLETE_EVENT->{
                onRefreshData()
            }
            else->{
                onEventBusMessage(msgFlag)
            }
        }
    }

    /**
     * 数据刷新
     */
    open fun onRefreshData(){

    }

    /**
     * 收到eventbus事件处理
     */
    open fun onEventBusMessage(msgFlag: String){
    }

    open fun fetchData(){

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}


