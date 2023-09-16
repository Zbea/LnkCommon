package com.bll.lnkcommon.ui.activity

import android.os.Handler
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.BookDetailsDialog
import com.bll.lnkcommon.dialog.CalenderDetailsDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.manager.CalenderDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.mvp.model.CalenderItemBean
import com.bll.lnkcommon.mvp.model.CalenderList
import com.bll.lnkcommon.mvp.presenter.CalenderPresenter
import com.bll.lnkcommon.mvp.view.IContractView.ICalenderView
import com.bll.lnkcommon.ui.adapter.CalenderListAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileDownManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.android.synthetic.main.ac_list.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.DecimalFormat

class CalenderListActivity:BaseActivity(),ICalenderView {

    private val presenter=CalenderPresenter(this)
    private var items= mutableListOf<CalenderItemBean>()
    private var mAdapter:CalenderListAdapter?=null
    private var detailsDialog:CalenderDetailsDialog?=null
    private var position=0

    override fun onList(list: CalenderList) {
        setPageNumber(list.total)
        items=list.list
        mAdapter?.setNewData(items)
    }

    override fun buySuccess() {
    }

    override fun layoutId(): Int {
        return R.layout.ac_list
    }

    override fun initData() {
        pageSize=12
    }
    override fun initView() {
        setPageTitle("新年台历")

        initRecycleView()
    }

    private fun initRecycleView(){

        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(this,28f), DP2PX.dip2px(this,60f),
            DP2PX.dip2px(this,28f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = CalenderListAdapter(R.layout.item_calendar, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            rv_list?.addItemDecoration(SpaceGridItemDeco1(4, DP2PX.dip2px(this@CalenderListActivity, 20f)
                , DP2PX.dip2px(this@CalenderListActivity, 60f)))
            setOnItemClickListener { adapter, view, position ->
                this@CalenderListActivity.position=position
                val item=items[position]
                showDetails(item)
            }
        }

    }


    private fun showDetails(item: CalenderItemBean) {
        detailsDialog = CalenderDetailsDialog(this, item)
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
                map["type"] = 3
                map["bookId"] = item.pid
                presenter.buyApk(map)
            }
        }
    }


    private fun downLoadStart(url: String, item: CalenderItemBean): BaseDownloadTask? {

        val fileName = MD5Utils.digest(item.pid.toString())//文件名
        val zipPath = FileAddress().getPathZip(fileName)
        val download = FileDownManager.with(this).create(url).setPath(zipPath)
            .startSingleTaskDownLoad(object : FileDownManager.SingleTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                    if (task != null && task.isRunning) {
                        runOnUiThread {
                            val s = getFormatNum(soFarBytes.toDouble() / (1024 * 1024),) + "/" +
                                    getFormatNum(totalBytes.toDouble() / (1024 * 1024),)
                            detailsDialog?.setUnClickBtn(s)
                        }
                    }
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    val fileTargetPath = FileAddress().getPathCalender(fileName)
                    unzip(item, zipPath, fileTargetPath)
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    //删除缓存 poolmap
                    hideLoading()
                    showToast("${item.title}下载失败")
                    detailsDialog?.setChangeStatus()
                }
            })
        return download
    }

    /**
     * 下载完成书籍解压
     */
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
                //更新列表
                mAdapter?.notifyDataSetChanged()
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


    fun getFormatNum(pi: Double): String? {
        val df = DecimalFormat("0.0M")
        return df.format(pi)
    }


    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        presenter.getAppList(map)
    }

    override fun onDestroy() {
        super.onDestroy()
        FileDownloader.getImpl().pauseAll()
    }

}