package com.bll.lnkcommon.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.bll.lnkcommon.*
import com.bll.lnkcommon.Constants.NETWORK_CONNECTION_COMPLETE_EVENT
import com.bll.lnkcommon.dialog.AppUpdateDialog
import com.bll.lnkcommon.dialog.ProgressDialog
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.presenter.CloudUploadPresenter
import com.bll.lnkcommon.mvp.presenter.CommonPresenter
import com.bll.lnkcommon.mvp.presenter.QiniuPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.mvp.view.IContractView.ICloudUploadView
import com.bll.lnkcommon.net.ExceptionHandle
import com.bll.lnkcommon.net.IBaseView
import com.bll.lnkcommon.ui.activity.drawing.NoteDrawingActivity
import com.bll.lnkcommon.ui.adapter.TabTypeAdapter
import com.bll.lnkcommon.utils.*
import com.bll.lnkcommon.widget.FlowLayoutManager
import com.google.gson.Gson
import com.htfy.params.ServerParams
import com.liulishuo.filedownloader.BaseDownloadTask
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_list_tab.*
import kotlinx.android.synthetic.main.common_fragment_title.*
import kotlinx.android.synthetic.main.common_page_number.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import kotlin.math.ceil


abstract class BaseFragment : Fragment(), IBaseView, IContractView.ICommonView,ICloudUploadView, IContractView.IQiniuView {

    var mCommonPresenter= CommonPresenter(this)
    var mCloudUploadPresenter=CloudUploadPresenter(this)
    var mQiniuPresenter= QiniuPresenter(this)
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
    var cloudList= mutableListOf<CloudListBean>()
    var mTabTypeAdapter:TabTypeAdapter?=null
    var itemTabTypes= mutableListOf<ItemTypeBean>()
    var appUpdateDialog:AppUpdateDialog?=null

    override fun onToken(token: String) {
        onUpload(token)
    }
    override fun onSuccess(cloudIds: MutableList<Int>?) {
        uploadSuccess(cloudIds)
    }

    override fun onCommon(commonData: CommonData) {
        if (!commonData.grade.isNullOrEmpty())
            DataBeanManager.grades=commonData.grade
        if (!commonData.subject.isNullOrEmpty())
            DataBeanManager.courses=commonData.subject
        if (!commonData.typeGrade.isNullOrEmpty())
            DataBeanManager.typeGrades=commonData.typeGrade
        if (!commonData.version.isNullOrEmpty())
            DataBeanManager.versions=commonData.version
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
        if (rv_tab!=null){
            initTabView()
        }

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

    private fun initTabView(){
        rv_tab.layoutManager = FlowLayoutManager()//创建布局管理
        mTabTypeAdapter = TabTypeAdapter(R.layout.item_tab_type, null).apply {
            rv_tab.adapter = this
            bindToRecyclerView(rv_tab)
            setOnItemClickListener { adapter, view, position ->
                for (item in data){
                    item.isCheck=false
                }
                if (position<data.size){
                    val item=data[position]
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
     * 跳转笔记写作
     */
    fun gotoNote(note: Note) {
        note.date=System.currentTimeMillis()
        NoteDaoManager.getInstance().insertOrReplace(note)
        EventBus.getDefault().post(Constants.NOTE_EVENT)

        val intent = Intent(activity, NoteDrawingActivity::class.java)
        intent.putExtra("noteId",note.id)
        customStartActivity(intent)
    }


    /**
     * 跳转活动(关闭已经打开的)
     */
    fun customStartActivity(intent: Intent){
        ActivityManager.getInstance().finishActivity(intent.component?.className)
        startActivity(intent)
    }

    fun onCheckUpdate() {
        if (NetworkUtil.isNetworkConnected()) {
            mCommonPresenter.getCommon()
            checkAppUpdate()
            checkSystemUpdate()
        }
    }
    /**
     * 检查系统更新
     */
    private fun checkSystemUpdate(){
        val url= Constants.RELEASE_BASE_URL+"Device/CheckUpdate"

        val  jsonBody = JSONObject()
        jsonBody.put(Constants.SN, DeviceUtil.getOtaSerialNumber())
        jsonBody.put(Constants.KEY, ServerParams.getInstance().GetHtMd5Key(DeviceUtil.getOtaSerialNumber()))
        jsonBody.put(Constants.VERSION_NO, DeviceUtil.getOtaProductVersion())

        val  jsonObjectRequest= JsonObjectRequest(Request.Method.POST,url,jsonBody, {
            showLog(it.toString())
            val code= it.optInt("Code")
            val jsonObject=it.optJSONObject("Data")
            if (code==200&&jsonObject!=null){
                val item= Gson().fromJson(jsonObject.toString(),SystemUpdateInfo::class.java)
                requireActivity().runOnUiThread {
                    AppUpdateDialog(requireActivity(),2,item).builder()
                }
            }
        },null)
        MyApplication.requestQueue?.add(jsonObjectRequest)
    }
    /**
     * 检查应用更新
     */
    private fun checkAppUpdate(){
        val url=Constants.URL_BASE+"app/info/one?type=3"

        val  jsonObjectRequest= StringRequest(Request.Method.GET,url, {
            val jsonObject= JSONObject(it)
            val code= jsonObject.optInt("code")
            val dataString=jsonObject.optString("data")
            val item= Gson().fromJson(dataString,AppUpdateBean::class.java)
            if (code==0){
                if (item.versionCode > AppUtils.getVersionCode(requireActivity())) {
                    requireActivity().runOnUiThread {
                        downLoadAPP(item)
                    }
                }
            }
        },null)
        MyApplication.requestQueue?.add(jsonObjectRequest)
    }
    /**
     * 下载应用
     */
    private fun downLoadAPP(bean: AppUpdateBean){
        val targetFileStr = FileAddress().getLauncherPath()
        if (FileUtils.isExist(targetFileStr)){
            AppUtils.installApp(requireActivity(), targetFileStr)
        }
        else{
            if (appUpdateDialog==null||appUpdateDialog?.isShow()==false) {
                appUpdateDialog = AppUpdateDialog(requireActivity(), 1, bean).builder()
                FileDownManager.with(requireActivity()).create(bean.downloadUrl).setPath(targetFileStr).startSingleTaskDownLoad(object :
                    FileDownManager.SingleTaskCallBack {
                    override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                        if (task != null && task.isRunning) {
                            requireActivity().runOnUiThread {
                                val s = ToolUtils.getFormatNum(soFarBytes.toDouble() / (1024 * 1024), "0.0M") + "/" +
                                        ToolUtils.getFormatNum(totalBytes.toDouble() / (1024 * 1024), "0.0M")
                                appUpdateDialog?.setUpdateBtn(s)
                            }
                        }
                    }

                    override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                    }

                    override fun completed(task: BaseDownloadTask?) {
                        appUpdateDialog?.dismiss()
                        AppUtils.installApp(requireActivity(), targetFileStr)
                    }

                    override fun error(task: BaseDownloadTask?, e: Throwable?) {
                        appUpdateDialog?.dismiss()
                    }
                })
            }
        }
    }


    override fun addSubscription(d: Disposable) {
    }
    override fun login() {
        if (mView==null||activity==null)return
        MethodManager.logoutFailure(requireActivity())
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
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onMessageEvent(msgFlag: String) {
        when(msgFlag){
            NETWORK_CONNECTION_COMPLETE_EVENT->{
                lazyLoad()
            }
            else->{
                onEventBusMessage(msgFlag)
            }
        }
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
     * 开始上传
     */
    open fun onUpload(token: String){

    }

    /**
     * 上传成功(书籍云id) 上传成功后删掉重复上传的数据
     */
    open fun uploadSuccess(cloudIds: MutableList<Int>?){
        if (!cloudIds.isNullOrEmpty())
        {
            mCloudUploadPresenter.deleteCloud(cloudIds)
        }
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
