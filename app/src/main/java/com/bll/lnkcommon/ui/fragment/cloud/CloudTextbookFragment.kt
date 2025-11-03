package com.bll.lnkcommon.ui.fragment.cloud

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants.TEXT_BOOK_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseCloudFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.TextbookGreenDaoManager
import com.bll.lnkcommon.mvp.model.CloudList
import com.bll.lnkcommon.mvp.model.book.TextbookBean
import com.bll.lnkcommon.ui.adapter.TextbookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DownloadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_cloud_list_tab.rv_list
import org.greenrobot.eventbus.EventBus
import java.io.File

class CloudTextbookFragment: BaseCloudFragment() {
    private var mAdapter: TextbookAdapter?=null
    private var textbooks= mutableListOf<TextbookBean>()
    private var position=0
    private var textBook=""

    override fun getLayoutId(): Int {
        return R.layout.fragment_cloud_list_tab
    }

    override fun initView() {
        pageSize=12
        initTab()
        initRecyclerView()
    }

    override fun lazyLoad() {
        fetchData()
    }

    private fun initTab(){
        itemTabTypes=DataBeanManager.textBookTypes
        textBook=itemTabTypes[0].title
        mTabTypeAdapter?.setNewData(itemTabTypes)
    }

    override fun onTabClickListener(view: View, position: Int) {
        textBook=itemTabTypes[position].title
        pageIndex=1
        fetchData()
    }


    private fun initRecyclerView(){
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(activity,30f),
            DP2PX.dip2px(activity,20f),
            DP2PX.dip2px(activity,30f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams
        rv_list.layoutManager = GridLayoutManager(activity,3)//创建布局管理
        mAdapter = TextbookAdapter(R.layout.item_bookstore, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setOnItemClickListener { adapter, view, position ->
                this@CloudTextbookFragment.position=position
                CommonDialog(requireActivity()).setContent("确定下载？").builder()
                    .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun ok() {
                            downloadItem()
                        }
                    })
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this@CloudTextbookFragment.position=position
                CommonDialog(requireActivity()).setContent("确定删除？").builder()
                    .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun ok() {
                            deleteItem()
                        }
                    })
                true
            }
        }
        rv_list.addItemDecoration(SpaceGridItemDeco(3,50))
    }

    private fun downloadItem(){
        val book=textbooks[position]
        val localBook = TextbookGreenDaoManager.getInstance().queryTextBookByBookId(book.category,book.bookId)
        if (localBook == null) {
            showLoading()
            downloadBook(book)
        } else {
            showToast("已下载")
        }
    }

    private fun deleteItem(){
        val ids= mutableListOf<Int>()
        ids.add(textbooks[position].cloudId)
        mCloudPresenter.deleteCloud(ids)
    }

    /**
     * 下载书籍
     */
    private fun downloadBook(book: TextbookBean) {
        val zipPath = FileAddress().getPathZip(FileUtils.getUrlName(book.downloadUrl))
        val zipDrawPath=FileAddress().getPathZip(FileUtils.getUrlName(book.drawUrl))
        val urls= mutableListOf<String>()
        val paths= mutableListOf<String>()
        urls.add(book.downloadUrl)
        paths.add(zipPath)
        if (!book.drawUrl.isNullOrEmpty()){
            urls.add(book.drawUrl)
            paths.add(zipDrawPath)
        }
        mDownloadManager?.startBatch(urls,paths, object : DownloadManager.BatchCallback {
            override fun onBatchCompleted() {
                ZipUtils.unzip(zipPath, book.bookPath, object : IZipCallback {
                    override fun onFinish() {
                        FileUtils.deleteFile(File(zipPath))
                        if (!book.drawUrl.isNullOrEmpty()){
                            ZipUtils.unzip(zipDrawPath, book.bookDrawPath, object : IZipCallback {
                                override fun onFinish() {
                                    FileUtils.deleteFile(File(zipDrawPath))
                                    downloadComplete(1,book)
                                }
                                override fun onProgress(percentDone: Int) {
                                }
                                override fun onError(msg: String) {
                                    downloadComplete(0,book)
                                }
                                override fun onStart() {
                                }
                            })
                        }
                        else{
                            downloadComplete(1,book)
                        }
                    }
                    override fun onProgress(percentDone: Int) {
                    }
                    override fun onError(msg: String?) {
                        downloadComplete(0,book)
                    }
                    override fun onStart() {
                    }
                })

            }
            override fun onBatchFailed(error: String) {
                hideLoading()
                downloadComplete(0,book)
            }
        })
    }

    /**
     * 下载结果
     */
    private fun downloadComplete(type:Int,book: TextbookBean){
        hideLoading()
        if (type==1){
            book.id=null
            TextbookGreenDaoManager.getInstance().insertOrReplaceBook(book)
            deleteItem()
            showToast(book.bookName+getString(R.string.download_success))
            EventBus.getDefault().post(TEXT_BOOK_EVENT)
        }
        else{
            if (FileUtils.isExistContent(book.bookDrawPath)){
                FileUtils.deleteFile(File(book.bookDrawPath))
            }
            if (FileUtils.isExistContent(book.bookPath)){
                FileUtils.deleteFile(File(book.bookPath))
            }
            showToast(book.bookName+getString(R.string.download_fail))
        }
    }


    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"]=pageIndex
        map["size"] = pageSize
        map["type"] = 2
        map["subTypeStr"] = textBook
        mCloudPresenter.getList(map)
    }

    override fun onCloudList(cloudList: CloudList) {
        setPageNumber(cloudList.total)
        textbooks.clear()
        for (item in cloudList.list){
            if (item.listJson.isNotEmpty()){
                val bookBean= Gson().fromJson(item.listJson, TextbookBean::class.java)
                bookBean.id=null
                bookBean.cloudId=item.id
                bookBean.drawUrl=item.downloadUrl
                textbooks.add(bookBean)
            }
        }
        mAdapter?.setNewData(textbooks)
    }

    override fun onCloudDelete() {
        mAdapter?.remove(position)
        onRefreshList(textbooks)
    }

}