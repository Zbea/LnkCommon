package com.bll.lnkcommon.ui.fragment.resource

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MyApplication
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.mvp.model.AppList
import com.bll.lnkcommon.mvp.model.CommonData
import com.bll.lnkcommon.mvp.presenter.AppCenterPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.AppCenterListAdapter
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileDownManager
import com.bll.lnkcommon.utils.NetworkUtil
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.fragment_list_content.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class AppDownloadFragment : BaseFragment(), IContractView.IAPPView{

    private var index=0
    private var presenter= AppCenterPresenter(this)
    private var mAdapter: AppCenterListAdapter?=null
    private var apps= mutableListOf<AppList.ListBean>()
    private var currentDownLoadTask: BaseDownloadTask?=null
    private var position=0
    private var supply=1

    override fun onType(commonData: CommonData) {
    }

    override fun onAppList(appBean: AppList) {
        setPageNumber(appBean.total)
        apps=appBean.list
        mAdapter?.setNewData(apps)
    }

    override fun buySuccess() {
        apps[position].buyStatus=1
        mAdapter?.notifyItemChanged(position)

        if (currentDownLoadTask == null || !currentDownLoadTask!!.isRunning) {
            currentDownLoadTask = downLoadStart(apps[position])
        } else {
            showToast("正在下载安装")
        }
    }


    /**
     * 实例 传送数据
     */
    fun newInstance(index:Int): AppDownloadFragment {
        val fragment= AppDownloadFragment()
        val bundle= Bundle()
        bundle.putInt("index",index)
        fragment.arguments=bundle
        return fragment
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_content
    }

    override fun initView() {
        index= arguments?.getInt("index")!!
        pageSize=8
        initRecyclerView()
    }

    override fun lazyLoad() {
        if (NetworkUtil.isNetworkConnected()) {
            fetchData()
        }
    }

    private fun initRecyclerView(){

        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(requireActivity(),50f),
            DP2PX.dip2px(requireActivity(),30f),
            DP2PX.dip2px(requireActivity(),50f),0)
        layoutParams.weight=1f

        rv_list.layoutParams= layoutParams
        rv_list.layoutManager = LinearLayoutManager(requireActivity())//创建布局管理
        mAdapter = AppCenterListAdapter(R.layout.item_app_center_list, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            setOnItemClickListener { adapter, view, position ->
                this@AppDownloadFragment.position=position
                val app=apps[position]
                if (app.buyStatus==0){
                    val map = HashMap<String, Any>()
                    map["type"] = 4
                    map["bookId"] = app.applicationId
                    presenter.buyApk(map)
                }
                else{
                    val idName=app.applicationId.toString()
                    if (!isInstalled(idName)) {
                        if (currentDownLoadTask == null || !currentDownLoadTask!!.isRunning) {
                            currentDownLoadTask = downLoadStart(app)
                        } else {
                            showToast("正在下载安装")
                        }
                    }
                }
            }
        }
    }

    //下载应用
    private fun downLoadStart(bean: AppList.ListBean): BaseDownloadTask? {
        val targetFileStr= FileAddress().getPathApk(bean.applicationId.toString())
        showLoading()
        val download = FileDownManager.with(requireActivity()).create(bean.contentUrl).setPath(targetFileStr).startSingleTaskDownLoad(object :
            FileDownManager.SingleTaskCallBack {

            override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
            }
            override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
            }
            override fun completed(task: BaseDownloadTask?) {
                hideLoading()
                installApk(targetFileStr)
                currentDownLoadTask = null//完成了废弃线程
            }
            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                hideLoading()
                currentDownLoadTask = null//完成了废弃线程
                showToast("下载失败")
            }
        })
        return download
    }

    //安装apk
    private fun installApk(apkPath: String) {
        AppUtils.installApp(requireActivity(), apkPath)
    }

    //是否已经下载安装
    private fun isInstalled(idName:String): Boolean {
        if (File(FileAddress().getPathApk(idName)).exists()){
            val packageName = AppUtils.getApkInfo(requireActivity(), FileAddress().getPathApk(idName))
            if (AppUtils.isAvailable(requireActivity(),packageName)){
                AppUtils.startAPP(requireActivity(), packageName)
            }
            else{
                //已经下载 直接去解析apk 去安装
                installApk(FileAddress().getPathApk(idName))
            }
            return true
        }
        return false
    }

    /**
     * 改变供应商
     */
    fun changeSupply(supply:Int){
        this.supply=supply
        pageIndex=1
        fetchData()
    }

    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        map["type"] = supply
        map["subType"]=index
        map["mainType"]=2
        presenter.getAppList(map)
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag== Constants.APP_INSTALL_EVENT){
            if (index==2){
                val bean=apps[position]
                val item=AppBean()
                item.appName=bean.nickname
                item.packageName=bean.packageName
                item.imageByte= AppUtils.scanLocalInstallAppDrawable(requireActivity(),bean.packageName)
                item.subType=1
                AppDaoManager.getInstance().insertOrReplace(item)
                EventBus.getDefault().post(Constants.APP_INSTALL_INSERT_EVENT)
            }
        }
    }

}