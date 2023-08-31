package com.bll.lnkcommon.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.dialog.ProgressDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.mvp.model.CommonData
import com.bll.lnkcommon.mvp.model.Note
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.mvp.presenter.CommonPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.ExceptionHandle
import com.bll.lnkcommon.net.IBaseView
import com.bll.lnkcommon.ui.activity.AccountLoginActivity
import com.bll.lnkcommon.ui.activity.MainActivity
import com.bll.lnkcommon.ui.activity.NoteDrawingActivity
import com.bll.lnkcommon.utils.*
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import kotlin.math.ceil
import kotlinx.android.synthetic.main.common_fragment_title.*
import kotlinx.android.synthetic.main.common_page_number.*
import java.io.File
import java.net.UnknownServiceException


abstract class BaseFragment : Fragment(), EasyPermissions.PermissionCallbacks, IBaseView,  IContractView.ICommonView{

    var mCommonPresenter= CommonPresenter(this)

    /**
     * 视图是否加载完毕
     */
    private var isViewPrepare = false
    /**
     * 数据是否加载过了
     */
    private var hasLoadData = false
    /**
     * 多种状态的 View 的切换
     */
    var mView:View?=null
    var mDialog: ProgressDialog? = null

    var pageIndex=1 //当前页码
    var pageCount=1 //全部数据
    var pageSize=0 //一页数据

    override fun onCommon(commonData: CommonData) {
        if (!commonData.grade.isNullOrEmpty())
            DataBeanManager.grades=commonData.grade
        if (!commonData.subject.isNullOrEmpty())
            DataBeanManager.courses=commonData.subject
        if (!commonData.typeGrade.isNullOrEmpty())
            DataBeanManager.typeGrades=commonData.typeGrade
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null != mView) {
            val parent: ViewGroup? = container
            parent?.removeView(parent)
        } else {
            mView = inflater.inflate(getLayoutId(), container,false)
        }

        return mView
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            lazyLoadDataIfPrepared()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        isViewPrepare = true
        initCommonTitle()
        initView()
        mDialog = ProgressDialog(activity)
        lazyLoadDataIfPrepared()
    }

    private fun lazyLoadDataIfPrepared() {
        if (userVisibleHint && isViewPrepare && !hasLoadData) {
            lazyLoad()
            hasLoadData = true
        }
    }

    /**
     * 关闭软键盘
     */
    fun hideKeyboard(){
        KeyboardUtils.hideSoftKeyboard(activity)
    }

    fun showToast(s:String){
        SToast.showText(s)
    }

    fun showToast(strId:Int){
        SToast.showText(strId)
    }

    fun showLog(s:String){
        Log.d("debug",s)
    }

    fun showLog(resId:Int){
        Log.d("debug",getString(resId))
    }

    /**
     * 加载布局
     */
    @LayoutRes
    abstract fun getLayoutId(): Int

    /**
     * 初始化 ViewI
     */
    abstract fun initView()

    /**
     * 懒加载
     */
    abstract fun lazyLoad()

    @SuppressLint("WrongViewCast")
    fun initCommonTitle() {

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


    fun setTitle(pageTitle: String) {
        tv_title?.text = pageTitle
    }

    fun setTitle(titleResId: Int) {
        tv_title?.text = getString(titleResId)
    }

    fun showSearch(isShow:Boolean) {
        if (isShow){
            showView(ll_search)
        }
        else{
            disMissView(ll_search)
        }
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

    fun getRadioButton(i:Int,str:String,max:Int): RadioButton {
        val radioButton =
            layoutInflater.inflate(R.layout.common_radiobutton, null) as RadioButton
        radioButton.text = str
        radioButton.id = i
        radioButton.isChecked = i == 0
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            DP2PX.dip2px(activity, 45f))

        layoutParams.marginEnd = if (i == max) 0 else DP2PX.dip2px(activity, 44f)
        radioButton.layoutParams = layoutParams

        return radioButton
    }

    /**
     * 跳转笔记写作
     */
    fun gotoNote(note: Note) {
        note.date=System.currentTimeMillis()
        NoteDaoManager.getInstance().insertOrReplace(note)
        EventBus.getDefault().post(Constants.NOTE_EVENT)

        val intent = Intent(activity, NoteDrawingActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("noteBundle",note)
        intent.putExtra("bundle",bundle)
        customStartActivity(intent)
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
     * 跳转活动(关闭已经打开的)
     */
    fun customStartActivity(intent: Intent){
        ActivityManager.getInstance().finishActivity(intent.component?.className)
        startActivity(intent)
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
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)) {
            Toast.makeText(activity, "已拒绝权限" + sb + "并不再询问", Toast.LENGTH_SHORT).show()
            AppSettingsDialog.Builder(requireActivity())
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
        if (mView==null||activity==null)return
        SToast.showText(R.string.login_timeout)
        SPUtil.putString("token", "")
        SPUtil.removeObj("user")
        EventBus.getDefault().post(Constants.USER_EVENT)
        ActivityManager.getInstance().finishOthers(MainActivity::class.java)
        customStartActivity(Intent(requireActivity(), AccountLoginActivity::class.java))
        DataBeanManager.students.clear()
        DataBeanManager.friends.clear()
        EventBus.getDefault().post(Constants.STUDENT_EVENT)
    }

    override fun hideLoading() {
        if (mView==null||activity==null)return
        mDialog?.dismiss()
    }
    override fun showLoading() {
        mDialog?.show()
    }
    override fun fail(msg: String) {
        if (mView==null||activity==null)return
        SToast.showText( msg)
    }
    override fun onFailer(responeThrowable: ExceptionHandle.ResponeThrowable?) {
        showLog(R.string.connect_server_timeout)
    }
    override fun onComplete() {
        showLog(R.string.request_success)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            onRefreshData()
        }
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

    /**
     * 每次翻页，刷新数据
     */
    open fun onRefreshData(){
    }

    /**
     * 网络请求数据
     */
    open fun fetchData(){
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}
