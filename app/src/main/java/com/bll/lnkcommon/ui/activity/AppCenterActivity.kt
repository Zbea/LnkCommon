package com.bll.lnkcommon.ui.activity

import android.content.Intent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.mvp.model.AppList
import com.bll.lnkcommon.mvp.presenter.AppCenterPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.AppCenterListAdapter
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileDownManager
import com.bll.lnkcommon.utils.FileUtils
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.ac_app_center.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class AppCenterActivity:BaseActivity(), IContractView.IAPPView{

    private var presenter= AppCenterPresenter(this)
    private var type=1
    private var mAdapter:AppCenterListAdapter?=null
    private var apps= mutableListOf<AppList.ListBean>()
    private var position=0
    private var currentDownLoadTask:BaseDownloadTask?=null

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
        return R.layout.ac_app_center
    }

    override fun initData() {
        pageSize=8
        fetchData()
    }

    override fun initView() {
        setPageTitle("应用中心")

        rg_group.setOnCheckedChangeListener { radioGroup, id ->
            type = if (id==R.id.rb_official){
                1
            }else{
                2
            }
            pageIndex=1
            fetchData()
        }
        initRecyclerView()
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
                if (!isLoginState()){
                    customStartActivity(Intent(this@AppCenterActivity,AccountLoginActivity::class.java))
                    return@setOnItemClickListener
                }
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
        map["type"] = type
        if (isLoginState()){
            presenter.getAppList(map)
        }
        else{
            presenter.getUnLoginAppList(map)
        }
    }

}