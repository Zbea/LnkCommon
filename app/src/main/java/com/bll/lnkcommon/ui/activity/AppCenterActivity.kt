package com.bll.lnkcommon.ui.activity

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.presenter.AppCenterPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.AppCenterListAdapter
import com.bll.lnkcommon.utils.*
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.ac_list_type.rv_list
import org.greenrobot.eventbus.EventBus
import java.io.File

class AppCenterActivity:BaseActivity(), IContractView.IAPPView{

    private var presenter= AppCenterPresenter(this)
    private var type=1
    private var mAdapter:AppCenterListAdapter?=null
    private var apps= mutableListOf<AppList.ListBean>()
    private var position=0
    private var currentDownLoadTask:BaseDownloadTask?=null
    private var types= mutableListOf<ItemList>()

    override fun onType(commonData: CommonData) {
        types=commonData.subType
        initTab()
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

    override fun layoutId(): Int {
        return R.layout.ac_list_type
    }

    override fun initData() {
        pageSize=8
        presenter.getTypeList()
    }

    override fun initView() {
        setPageTitle("应用")
        initRecyclerView()
    }

    private fun initTab(){
        for (i in types.indices) {
            itemTabTypes.add(ItemTypeBean().apply {
                title=types[i].desc
                isCheck=i==0
            })
        }
        mTabTypeAdapter?.setNewData(itemTabTypes)
        fetchData()
    }

    override fun onTabClickListener(view: View, position: Int) {
        type=types[position].type
        pageIndex=1
        fetchData()
    }

    private fun initRecyclerView(){
        val layoutParams=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(DP2PX.dip2px(this,52f),DP2PX.dip2px(this,50f),DP2PX.dip2px(this,52f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams
        rv_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mAdapter = AppCenterListAdapter(R.layout.item_app_center_list, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            setOnItemClickListener { adapter, view, position ->
                this@AppCenterActivity.position=position
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
        val download = FileDownManager.with(this).create(bean.contentUrl).setPath(targetFileStr).startSingleTaskDownLoad(object :
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
                showToast(e!!.message!!)
            }
        })
        return download
    }

    //安装apk
    private fun installApk(apkPath: String) {
        AppUtils.installApp(this, apkPath)
    }

    //是否已经下载安装
    private fun isInstalled(idName:String): Boolean {
        if (File(FileAddress().getPathApk(idName)).exists()){
            val packageName = AppUtils.getApkInfo(this, FileAddress().getPathApk(idName))
            if (AppUtils.isAvailable(this,packageName)){
                AppUtils.startAPP(this, packageName)
            }
            else{
                //已经下载 直接去解析apk 去安装
                installApk(FileAddress().getPathApk(idName))
            }
            return true
        }
        return false
    }

    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        map["subType"] = type
        map["mainType"]=1
        presenter.getAppList(map)
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag==Constants.APP_INSTALL_EVENT){
            if (type==6){
                val bean=apps[position]
                val item=AppBean()
                item.appName=bean.nickname
                item.packageName=bean.packageName
                item.imageByte= AppUtils.scanLocalInstallAppDrawable(this,bean.packageName)
                item.subType=1
                AppDaoManager.getInstance().insertOrReplace(item)
                EventBus.getDefault().post(Constants.APP_INSTALL_INSERT_EVENT)
            }
        }
    }

}