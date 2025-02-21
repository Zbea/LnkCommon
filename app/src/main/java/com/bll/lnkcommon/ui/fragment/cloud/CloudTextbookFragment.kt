package com.bll.lnkcommon.ui.fragment.cloud

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseCloudFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.manager.TextbookGreenDaoManager
import com.bll.lnkcommon.mvp.book.Book
import com.bll.lnkcommon.mvp.book.TextbookBean
import com.bll.lnkcommon.mvp.model.CloudList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.ui.adapter.TextbookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileDownManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.fragment_cloud_list_tab.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.concurrent.CountDownLatch

class CloudTextbookFragment: BaseCloudFragment() {
    private var countDownTasks: CountDownLatch?=null //异步完成后操作
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
                        override fun cancel() {
                        }
                        override fun ok() {
                            downloadItem()
                        }
                    })
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this@CloudTextbookFragment.position=position
                CommonDialog(requireActivity()).setContent("确定删除？").builder()
                    .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun cancel() {
                        }
                        override fun ok() {
                            deleteItem(textbooks[position])
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
            //判断书籍是否有手写内容，没有手写内容直接下载书籍zip
            if (!book.drawUrl.isNullOrEmpty()){
                countDownTasks= CountDownLatch(2)
                downloadBook(book)
                downloadBookDrawing(book)
            }
            else{
                countDownTasks=CountDownLatch(1)
                downloadBook(book)
            }
            downloadSuccess(book)
        } else {
            showToast("已下载")
        }
    }

    private fun deleteItem(book: TextbookBean){
        val ids= mutableListOf<Int>()
        ids.add(book.cloudId)
        mCloudPresenter.deleteCloud(ids)
    }

    /**
     * 下载完成
     */
    private fun downloadSuccess(book: TextbookBean){
        //等待两个请求完成后刷新列表
        Thread{
            countDownTasks?.await()
            requireActivity().runOnUiThread {
                hideLoading()
                val localBook = TextbookGreenDaoManager.getInstance().queryTextBookByBookId(book.category,book.bookId)
                if (localBook!=null){
                    showToast(book.bookName+"下载成功")
                    deleteItem(book)
                    EventBus.getDefault().post(Constants.TEXT_BOOK_EVENT)
                }
                else{
                    if (FileUtils.isExistContent(book.bookDrawPath)){
                        FileUtils.deleteFile(File(book.bookDrawPath))
                    }
                    if (FileUtils.isExistContent(book.bookPath)){
                        FileUtils.deleteFile(File(book.bookPath))
                    }
                    showToast(book.bookName+"下载失败")
                }
            }
            countDownTasks=null
        }.start()
    }

    /**
     * 下载书籍
     */
    private fun downloadBook(book: TextbookBean) {
        val fileName = book.bookId.toString()//文件名
        val zipPath = FileAddress().getPathZip(fileName)
        FileDownManager.with(activity).create(book.downloadUrl).setPath(zipPath)
            .startSingleTaskDownLoad(object : FileDownManager.SingleTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    ZipUtils.unzip(zipPath, book.bookPath, object : IZipCallback {
                        override fun onFinish() {
                            book.id=null
                            TextbookGreenDaoManager.getInstance().insertOrReplaceBook(book)
                            //删除教材的zip文件
                            FileUtils.deleteFile(File(zipPath))
                            countDownTasks?.countDown()
                        }
                        override fun onProgress(percentDone: Int) {
                        }
                        override fun onError(msg: String?) {
                            countDownTasks?.countDown()
                        }
                        override fun onStart() {
                        }
                    })
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    countDownTasks?.countDown()
                }
            })
    }

    /**
     * 下载书籍手写内容
     */
    private fun downloadBookDrawing(book: TextbookBean){
        val fileName = book.bookId.toString()+"draw"//文件名
        val zipPath = FileAddress().getPathZip(fileName)
        FileDownManager.with(activity).create(book.drawUrl).setPath(zipPath)
            .startSingleTaskDownLoad(object : FileDownManager.SingleTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    ZipUtils.unzip(zipPath, book.bookDrawPath, object : IZipCallback {
                        override fun onFinish() {
                            //删除教材的zip文件
                            FileUtils.deleteFile(File(zipPath))
                            countDownTasks?.countDown()
                        }
                        override fun onProgress(percentDone: Int) {
                        }
                        override fun onError(msg: String?) {
                            countDownTasks?.countDown()
                        }
                        override fun onStart() {
                        }
                    })
                    countDownTasks?.countDown()
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    countDownTasks?.countDown()
                }
            })
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
    }

}