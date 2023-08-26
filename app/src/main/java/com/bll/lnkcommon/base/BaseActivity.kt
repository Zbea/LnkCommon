package com.bll.lnkcommon.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.dialog.ProgressDialog
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.net.ExceptionHandle
import com.bll.lnkcommon.net.IBaseView
import com.bll.lnkcommon.ui.activity.AccountLoginActivity
import com.bll.lnkcommon.ui.activity.MainActivity
import com.bll.lnkcommon.utils.*
import com.google.gson.Gson
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.common_page_number.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
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

    open fun navigationToFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.frame_layout, fragment, (fragment as Any?)!!.javaClass.simpleName)
                .addToBackStack(null).commitAllowingStateLoss()
        }
    }

    open fun popToStack(fragment: Fragment?) {
        val fragmentManager: FragmentManager = supportFragmentManager
        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
        fragmentManager.popBackStack()
    }

    override fun moveTaskToBack(nonRoot: Boolean): Boolean {
        return super.moveTaskToBack(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSaveState=savedInstanceState
        setContentView(layoutId())
        initCommonTitle()
        EventBus.getDefault().register(this)
        setStatusBarColor(ContextCompat.getColor(this, R.color.white))

        if (!EasyPermissions.hasPermissions(this,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)){
            EasyPermissions.requestPermissions(this,"请求权限",1,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
        }

        mDialog = ProgressDialog(this)
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


    fun showSearchView(isShow:Boolean) {
       if (isShow){
            showView(ll_search)
        }
        else{
            disMissView(ll_search)
        }
    }


    fun setPageTitle(pageTitle: String) {
        tv_title?.text = pageTitle
    }

    fun setPageTitle(titleId: Int) {
        tv_title?.setText(titleId)
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

    /**
     * 是否登录
     */
    fun isLoginState():Boolean{
        val mUser= SPUtil.getObj("user",User::class.java)
        val token=SPUtil.getString("token")
        return token.isNotEmpty() && mUser!=null
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

    fun getRadioButton(i:Int,str:String,max:Int): RadioButton {
        val radioButton =
            layoutInflater.inflate(R.layout.common_radiobutton, null) as RadioButton
        radioButton.text = str
        radioButton.id = i
        radioButton.isChecked = i == 0
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            DP2PX.dip2px(this, 45f))

        layoutParams.marginEnd = if (i == max) 0 else DP2PX.dip2px(this, 44f)
        radioButton.layoutParams = layoutParams

        return radioButton
    }

    fun getRadioButton(i:Int,str:String,isCheck:Boolean): RadioButton {
        val radioButton =
            layoutInflater.inflate(R.layout.common_radiobutton, null) as RadioButton
        radioButton.text = str
        radioButton.id = i
        radioButton.isChecked = isCheck
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            DP2PX.dip2px(this, 45f))

        layoutParams.marginEnd = DP2PX.dip2px(this, 44f)
        radioButton.layoutParams = layoutParams

        return radioButton
    }

    /**
     * 跳转阅读器
     */
    fun gotoBookDetails(bookBean: Book){
        bookBean.isLook=true
        bookBean.time=System.currentTimeMillis()
        BookDaoManager.getInstance().insertOrReplaceBook(bookBean)
        EventBus.getDefault().post(Constants.BOOK_EVENT)

        val toolApps= AppDaoManager.getInstance().queryTool()
        val result = JSONArray()
        for (item in toolApps){
            val jsonObject = JSONObject()
            try {
                jsonObject.put("appName", item.appName)
                jsonObject.put("packageName", item.packageName)
            } catch (_: JSONException) {
            }
            result.put(jsonObject)
        }

        val intent = Intent()
        intent.action = "com.geniatech.reader.action.VIEW_BOOK_PATH"
        intent.setPackage("com.geniatech.knote.reader")
        intent.putExtra("path", bookBean.bookPath)
        intent.putExtra("key_book_id",bookBean.bookId.toString())
        intent.putExtra("bookName", bookBean.bookName)
        intent.putExtra("tool",result.toString())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("android.intent.extra.LAUNCH_SCREEN", 1)
        startActivity(intent)
    }

    /**
     * 删除书本
     */
    fun deleteBook(book: Book){
        BookDaoManager.getInstance().deleteBook(book) //删除本地数据库
        FileUtils.deleteFile(File(book.bookPath))//删除下载的书籍资源
        if (File(book.bookDrawPath).exists())
            FileUtils.deleteFile(File(book.bookDrawPath))
        EventBus.getDefault().post(Constants.BOOK_EVENT)
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
        Log.i("EasyPermissions", "获取成功的权限$perms")
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
            Toast.makeText(this, "已拒绝权限" + sb + "并不再询问", Toast.LENGTH_SHORT).show()
            AppSettingsDialog.Builder(this)
                    .setRationale("此功能需要" + sb + "权限，否则无法正常使用，是否打开设置")
                    .setPositiveButton("好")
                    .setNegativeButton("不行")
                    .build()
                    .show()
        }
    }

    override fun addSubscription(d: Disposable) {
    }
    override fun login() {
        SToast.showText(R.string.login_timeout)
        SPUtil.putString("token", "")
        SPUtil.removeObj("user")
        EventBus.getDefault().post(Constants.USER_EVENT)
        ActivityManager.getInstance().finishOthers(MainActivity::class.java)
        customStartActivity(Intent(this, AccountLoginActivity::class.java))
        DataBeanManager.students.clear()
        EventBus.getDefault().post(Constants.STUDENT_EVENT)
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
        onEventBusMessage(msgFlag)
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


