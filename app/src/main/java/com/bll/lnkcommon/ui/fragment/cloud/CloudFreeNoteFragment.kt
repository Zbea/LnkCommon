package com.bll.lnkcommon.ui.fragment.cloud

import android.os.Handler
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseCloudFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.FreeNoteDaoManager
import com.bll.lnkcommon.mvp.model.CloudList
import com.bll.lnkcommon.mvp.model.FreeNoteBean
import com.bll.lnkcommon.ui.adapter.CloudFreeNoteAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileDownManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceItemDeco
import com.google.gson.Gson
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.fragment_cloud_list_tab.*
import java.io.File

class CloudFreeNoteFragment: BaseCloudFragment() {
    private var mAdapter: CloudFreeNoteAdapter?=null
    private var items= mutableListOf<FreeNoteBean>()
    private var position=0

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_content
    }

    override fun initView() {
        pageSize=14
        initRecyclerView()
    }

    override fun lazyLoad() {
        fetchData()
    }

    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(DP2PX.dip2px(activity,30f), DP2PX.dip2px(activity,20f), DP2PX.dip2px(activity,30f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams
        mAdapter = CloudFreeNoteAdapter(R.layout.item_cloud_diary, null).apply {
            rv_list.layoutManager = LinearLayoutManager(activity)//创建布局管理
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setOnItemClickListener { adapter, view, position ->
                this@CloudFreeNoteFragment.position=position
                CommonDialog(requireActivity()).setContent("确定下载？").builder()
                    .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun cancel() {
                        }
                        override fun ok() {
                            downloadItem()
                        }
                    })
            }
            setOnItemChildClickListener { adapter, view, position ->
                this@CloudFreeNoteFragment.position=position
                if (view.id==R.id.iv_delete){
                    CommonDialog(requireActivity()).setContent("确定删除？").builder()
                        .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                            override fun cancel() {
                            }
                            override fun ok() {
                                deleteItem()
                            }
                        })
                }
            }
        }
        rv_list.addItemDecoration(SpaceItemDeco(30))
    }

    private fun downloadItem(){
        val item=items[position]
        if (!FreeNoteDaoManager.getInstance().isExist(item.date)){
            download(item)
        }
        else{
            showToast("已存在")
        }
    }

    private fun deleteItem(){
        val ids= mutableListOf<Int>()
        ids.add(items[position].cloudId)
        mCloudPresenter.deleteCloud(ids)
    }

    private fun download(item: FreeNoteBean){
        showLoading()
        val fileName=DateUtils.longToString(item.date)
        val zipPath = FileAddress().getPathZip(fileName)
        val fileTargetPath= FileAddress().getPathFreeNote(fileName)
        FileDownManager.with(activity).create(item.downloadUrl).setPath(zipPath)
            .startSingleTaskDownLoad(object :
                FileDownManager.SingleTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    ZipUtils.unzip(zipPath, fileTargetPath, object : IZipCallback {
                        override fun onFinish() {
                            item.id=null//设置数据库id为null用于重新加入
                            item.isSave=true
                            FreeNoteDaoManager.getInstance().insertOrReplace(item)
                            //删掉本地zip文件
                            FileUtils.deleteFile(File(zipPath))
                            Handler().postDelayed({
                                showToast("下载成功")
                                deleteItem()
                                hideLoading()
                            },500)
                        }
                        override fun onProgress(percentDone: Int) {
                        }
                        override fun onError(msg: String?) {
                            showToast(msg!!)
                            hideLoading()
                        }
                        override fun onStart() {
                        }
                    })
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    hideLoading()
                    showToast("下载失败")
                }
            })
    }

    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"]=pageIndex
        map["size"] = pageSize
        map["type"] = 5
        map["subTypeStr"] = "随笔"
        mCloudPresenter.getList(map)
    }

    override fun onCloudList(cloudList: CloudList) {
        setPageNumber(cloudList.total)
        items.clear()
        for (item in cloudList.list){
            if (item.listJson.isNotEmpty()){
                val freeNoteBean= Gson().fromJson(item.listJson, FreeNoteBean::class.java)
                freeNoteBean.cloudId=item.id
                freeNoteBean.downloadUrl=item.downloadUrl
                items.add(freeNoteBean)
            }
        }
        mAdapter?.setNewData(items)
    }

    override fun onCloudDelete() {
        mAdapter?.remove(position)
    }
}