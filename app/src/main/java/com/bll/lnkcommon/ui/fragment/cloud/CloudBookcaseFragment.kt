package com.bll.lnkcommon.ui.fragment.cloud

import android.os.Handler
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseCloudFragment
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.mvp.model.CloudList
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileDownManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.common_radiogroup.*
import kotlinx.android.synthetic.main.fragment_cloud_list_type.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class CloudBookcaseFragment:BaseCloudFragment() {

    private var bookTypeStr=""
    private var mAdapter:BookAdapter?=null
    private var types= mutableListOf<String>()
    private var books= mutableListOf<Book>()
    private var position=0

    override fun getLayoutId(): Int {
        return R.layout.fragment_cloud_list_type
    }

    override fun initView() {
        pageSize=12
        initRecyclerView()
    }

    override fun lazyLoad() {
        mCloudPresenter.getType()
    }

    private fun initTab(){
        for (i in types.indices) {
            rg_group.addView(getRadioButton(i ,types[i],types.size-1))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, id ->
            bookTypeStr=types[id]
            pageIndex=1
            fetchData()
        }
        fetchData()
    }

    private fun initRecyclerView(){
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(activity,28f),
            DP2PX.dip2px(activity,20f),
            DP2PX.dip2px(activity,28f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams
        rv_list.layoutManager = GridLayoutManager(activity,4)//创建布局管理
        mAdapter = BookAdapter(R.layout.item_bookstore, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            rv_list.addItemDecoration(SpaceGridItemDeco1(4, DP2PX.dip2px(activity,22f),50))
            setOnItemClickListener { adapter, view, position ->
                val book=books[position]
                val localBook = BookDaoManager.getInstance().queryByBookID(1,book.bookPlusId)
                if (localBook == null) {
                    showLoading()
                    //判断书籍是否有手写内容，没有手写内容直接下载书籍zip
                    if (!book.drawUrl.isNullOrEmpty()){
                        downloadBookDrawing(book)
                    }else{
                        downloadBook(book)
                    }
                } else {
                    showToast("已下载")
                }
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this@CloudBookcaseFragment.position=position
                CommonDialog(requireActivity()).setContent("确定删除？").builder()
                    .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun cancel() {
                        }
                        override fun ok() {
                            val ids= mutableListOf<Int>()
                            ids.add(books[position].cloudId)
                            mCloudPresenter.deleteCloud(ids)
                        }
                    })
                true
            }
        }
    }

    /**
     * 下载书籍手写内容
     */
    private fun downloadBookDrawing(book: Book){
        val fileName = MD5Utils.digest(book.bookId.toString())//文件名
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
                            downloadBook(book)
                        }
                        override fun onProgress(percentDone: Int) {
                        }
                        override fun onError(msg: String?) {
                        }
                        override fun onStart() {
                        }
                    })
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                }
            })
    }

    /**
     * 下载书籍
     */
    private fun downloadBook(book: Book) {
        val formatStr=book.bodyUrl.substring(book.bodyUrl.lastIndexOf("."))
        val fileName = MD5Utils.digest(book.bookId.toString())//文件名
        val targetFileStr = FileAddress().getPathBook(fileName+formatStr)
        FileDownManager.with(activity).create(book.bodyUrl).setPath(targetFileStr)
            .startSingleTaskDownLoad(object : FileDownManager.SingleTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    book.id=null
                    BookDaoManager.getInstance().insertOrReplaceBook(book)
                    Handler().postDelayed({
                        hideLoading()
                        showToast(book.bookName+"下载成功")
                    },500)
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    //删除缓存 poolmap
                    hideLoading()
                    //下载失败删掉已下载手写内容
                    FileUtils.deleteFile(File(book.bookDrawPath))
                    showToast(book.bookName+"下载失败")
                }
            })
    }

    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"]=pageIndex
        map["size"] = pageSize
        map["type"] = 1
        map["subTypeStr"] = bookTypeStr
        mCloudPresenter.getList(map)
    }

    override fun onCloudType(types: MutableList<String>) {
        for (str in types){
            if (!this.types.contains(str))
            {
                this.types.add(str)
            }
        }
        if (types.size>0){
            bookTypeStr=types[0]
            initTab()
        }
    }

    override fun onCloudList(item: CloudList) {
        setPageNumber(item.total)
        books.clear()
        for (bookCloud in item.list){
            if (bookCloud.listJson.isNotEmpty()){
                val bookBean= Gson().fromJson(bookCloud.listJson, Book::class.java)
                bookBean.id=null
                bookBean.cloudId=bookCloud.id
                bookBean.drawUrl=bookCloud.downloadUrl
                books.add(bookBean)
            }
        }
        mAdapter?.setNewData(books)
    }

    override fun onCloudDelete() {
        mAdapter?.remove(position)
    }
}