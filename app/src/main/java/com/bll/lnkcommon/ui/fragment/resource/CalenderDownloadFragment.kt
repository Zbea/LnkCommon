package com.bll.lnkcommon.ui.fragment.resource

import android.os.Handler
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.DownloadCalenderDialog
import com.bll.lnkcommon.dialog.ImageDialog
import com.bll.lnkcommon.manager.CalenderDaoManager
import com.bll.lnkcommon.mvp.model.CalenderItemBean
import com.bll.lnkcommon.mvp.model.CalenderList
import com.bll.lnkcommon.mvp.presenter.CalenderPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.CalenderListAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DownloadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.NetworkUtil
import com.bll.lnkcommon.utils.ToolUtils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.ac_list.rv_list
import org.greenrobot.eventbus.EventBus
import java.io.File

class CalenderDownloadFragment: BaseFragment(), IContractView.ICalenderView {

    private var presenter= CalenderPresenter(this)
    private var items= mutableListOf<CalenderItemBean>()
    private var mAdapter: CalenderListAdapter?=null
    private var detailsDialog: DownloadCalenderDialog?=null
    private var position=0
    private var supply=1

    override fun onList(list: CalenderList) {
        setPageNumber(list.total)
        for (item in list.list){
            item.pid=item.id.toInt()
            item.id=null
        }
        items=list.list
        mAdapter?.setNewData(items)
    }

    override fun buySuccess() {
        items[position].buyStatus=1
        mAdapter?.notifyItemChanged(position)
        detailsDialog?.setChangeStatus()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_content
    }

    override fun initView() {
        pageSize=12
        initRecycleView()
    }

    override fun lazyLoad() {
        if (NetworkUtil.isNetworkConnected()) {
            fetchData()
        }
    }

    private fun initRecycleView(){
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(requireActivity(),30f), DP2PX.dip2px(requireActivity(),50f),
            DP2PX.dip2px(requireActivity(),30f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(requireActivity(), 4)//创建布局管理
        mAdapter = CalenderListAdapter(R.layout.item_calendar,null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            setOnItemClickListener { adapter, view, position ->
                val item=items[position]
                val urls=item.previewUrl.split(",")
                ImageDialog(requireActivity(),urls).builder()

            }
            setOnItemChildClickListener { adapter, view, position ->
                this@CalenderDownloadFragment.position=position
                val item=items[position]
                if (view.id==R.id.tv_buy){
                    showDetails(item)
                }
            }
        }
        rv_list?.addItemDecoration(SpaceGridItemDeco(4, DP2PX.dip2px(requireActivity(), 50f)))
    }


    private fun showDetails(item: CalenderItemBean) {
        detailsDialog = DownloadCalenderDialog(requireActivity(), item)
        detailsDialog?.builder()
        detailsDialog?.setOnClickListener {
            if (item.buyStatus==1){
                if (!CalenderDaoManager.getInstance().isExist(item.pid)) {
                    downLoadStart(item.downloadUrl,item)
                } else {
                    item.loadSate =2
                    showToast("已下载")
                    mAdapter?.notifyItemChanged(position)
                    detailsDialog?.setDissBtn()
                }
            }
            else{
                val map = HashMap<String, Any>()
                map["type"] = 7
                map["bookId"] = item.pid
                presenter.buyApk(map)
            }
        }
    }


    private fun downLoadStart(url: String, item: CalenderItemBean){
        showLoading()
        val fileName = MD5Utils.digest(item.pid.toString())//文件名
        val zipPath = FileAddress().getPathZip(fileName)
        mDownloadManager?.startSingle(url,zipPath, object : DownloadManager.SingleCallback {
            override fun onProgress(task: BaseDownloadTask, soFar: Long, total: Long) {
                if (task.isRunning) {
                    requireActivity().runOnUiThread {
                        val s = ToolUtils.getFormatNum(soFar.toDouble() / (1024 * 1024),"0.0M") + "/" +
                                ToolUtils.getFormatNum(total.toDouble() / (1024 * 1024),"0.0M")
                        detailsDialog?.setUnClickBtn(s)
                    }
                }
            }
            override fun onCompleted(task: BaseDownloadTask) {
                val fileTargetPath = FileAddress().getPathCalender(fileName)
                unzip(item, zipPath, fileTargetPath)
            }
            override fun onFailed(task: BaseDownloadTask?, error: String) {
                hideLoading()
                showToast("${item.title}下载失败")
                detailsDialog?.setChangeStatus()
            }
        })
    }

    private fun unzip(item: CalenderItemBean, zipPath: String, fileTargetPath: String) {
        ZipUtils.unzip(zipPath, fileTargetPath, object : IZipCallback {
            override fun onFinish() {
                item.apply {
                    loadSate = 2
                    date = System.currentTimeMillis()//下载时间用于排序
                    path = fileTargetPath
                }
                CalenderDaoManager.getInstance().insertOrReplace(item)
                EventBus.getDefault().post(Constants.CALENDER_EVENT)
                detailsDialog?.dismiss()
                FileUtils.deleteFile(File(zipPath))
                Handler().postDelayed({
                    hideLoading()
                    showToast(item.title+"下载成功")
                },500)
            }
            override fun onProgress(percentDone: Int) {
            }
            override fun onError(msg: String?) {
                hideLoading()
                showToast(item.title+msg!!)
                detailsDialog?.setChangeStatus()
            }
            override fun onStart() {
            }
        })
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
        map["mainType"]=3
        presenter.getList(map)
    }

    override fun onDestroy() {
        super.onDestroy()
        FileDownloader.getImpl().pauseAll()
    }

}