package com.bll.lnkcommon.ui.fragment.cloud

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants.BOOK_EVENT
import com.bll.lnkcommon.Constants.BOOK_TYPE_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseCloudFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.CloudList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.book.Book
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DownloadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.NetworkUtil
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_list_content_tab.rv_list
import org.greenrobot.eventbus.EventBus
import java.io.File

class CloudBookcaseFragment:BaseCloudFragment() {
    private var mAdapter: BookAdapter?=null
    private var bookTypeStr=""
    private val books= mutableListOf<Book>()
    private var position=0

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_content_tab
    }

    override fun initView() {
        pageSize=12
        initTab()
        initRecyclerView()
    }

    override fun lazyLoad() {
        if (NetworkUtil.isNetworkConnected()){
            fetchData()
        }
    }

    private fun initTab(){
        val types= DataBeanManager.bookStoreTypes
        types.forEach {
            itemTabTypes.add(ItemTypeBean().apply {
                title=it.desc
                typeId=it.type
            })
        }
        itemTabTypes[0].isCheck=true
        bookTypeStr=itemTabTypes[0].title
        mTabTypeAdapter?.setNewData(itemTabTypes)
    }

    override fun onTabClickListener(view: View, position: Int) {
        bookTypeStr=itemTabTypes[position].title
        pageIndex=1
        fetchData()
    }

    private fun initRecyclerView(){
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(activity,15f),
            DP2PX.dip2px(activity,25f),
            DP2PX.dip2px(activity,15f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(activity,4)//创建布局管理
        rv_list.addItemDecoration(SpaceGridItemDeco(4,DP2PX.dip2px(activity,15f)))
        mAdapter = BookAdapter(R.layout.item_bookstore, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setOnItemClickListener { adapter, view, position ->
                this@CloudBookcaseFragment.position=position
                CommonDialog(requireActivity()).setContent("确定下载？").builder()
                    .setOnDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun ok() {
                            downloadItem()
                        }
                    })
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this@CloudBookcaseFragment.position=position
                CommonDialog(requireActivity()).setContent(R.string.tips_is_delete).builder()
                    .setOnDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun ok() {
                            deleteItem()
                        }
                    })
                true
            }
        }

    }

    private fun downloadItem(){
        val book=books[position]
        val localBook = BookDaoManager.getInstance().queryByBookID(book.bookId)
        if (localBook == null) {
            showLoading()
            downloadBook(book)
        } else {
            showToast(R.string.downloaded)
        }
    }

    private fun deleteItem(){
        val ids= mutableListOf<Int>()
        ids.add(books[position].cloudId)
        mCloudPresenter.deleteCloud(ids)
    }

    /**
     * 下载书籍
     */
    private fun downloadBook(book: Book) {
        val urls= mutableListOf<String>()
        val paths= mutableListOf<String>()
        urls.add(book.downloadUrl)
        paths.add(book.bookPath)
        val zipPath=FileAddress().getPathZip(FileUtils.getUrlName(book.drawUrl))
        if (!book.drawUrl.isNullOrEmpty()){
            urls.add(book.drawUrl)
            paths.add(zipPath)
        }
        mDownloadManager?.startBatch(urls,paths, object : DownloadManager.BatchCallback {
            override fun onBatchCompleted() {
                if (!book.drawUrl.isNullOrEmpty()){
                    ZipUtils.unzip(zipPath, book.bookDrawPath, object : IZipCallback {
                        override fun onFinish() {
                            FileUtils.deleteFile(File(zipPath))
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
            override fun onBatchFailed(error: String) {
                downloadComplete(0,book)
            }
        })
    }

    /**
     * 下载结果
     */
    private fun downloadComplete(type:Int,book:Book){
        hideLoading()
        if (type==1){
            book.id=null
            book.time=System.currentTimeMillis()
            book.isLook=false
            BookDaoManager.getInstance().insertOrReplaceBook(book)
            //修改书库分类状态
            ItemTypeDaoManager.getInstance().saveBookBean(book.subtypeStr,true)

            deleteItem()
            showToast(book.bookName+getString(R.string.download_success))
            EventBus.getDefault().post(BOOK_EVENT)
            EventBus.getDefault().post(BOOK_TYPE_EVENT)
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
        map["type"] = 1
        map["subTypeStr"] = bookTypeStr
        mCloudPresenter.getList(map)
    }

    override fun onCloudList(cloudBean: CloudList) {
        books.clear()
        for (item in cloudBean.list){
            val bookBean= Gson().fromJson(item.listJson, Book::class.java)
            bookBean.id=null
            bookBean.cloudId=item.id
            bookBean.drawUrl=item.downloadUrl
            books.add(bookBean)
        }
        mAdapter?.setNewData(books)
        setPageNumber(cloudBean.total)
    }

    override fun onCloudDelete() {
        mAdapter?.remove(position)
        onRefreshList(books)
    }

}