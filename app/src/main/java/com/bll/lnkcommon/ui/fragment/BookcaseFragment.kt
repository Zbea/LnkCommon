package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.ui.activity.AccountLoginActivity
import com.bll.lnkcommon.ui.activity.book.BookcaseTypeListActivity
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.ui.activity.CloudStorageActivity
import com.bll.lnkcommon.utils.FileUploadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.google.gson.Gson
import kotlinx.android.synthetic.main.common_fragment_title.*
import kotlinx.android.synthetic.main.fragment_bookcase.*
import java.io.File

class BookcaseFragment:BaseFragment() {

    private var mAdapter: BookAdapter?=null
    private var books= mutableListOf<Book>()//所有数据
    private var bookTopBean:Book?=null

    override fun getLayoutId(): Int {
        return R.layout.fragment_bookcase
    }
    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[1])

        initRecyclerView()
        findBook()

        tv_type.setOnClickListener {
            if (isLoginState()){
                customStartActivity(Intent(activity, BookcaseTypeListActivity::class.java))
            }
            else{
                customStartActivity(Intent(activity, AccountLoginActivity::class.java))
            }
        }

        ll_book_top.setOnClickListener {
            if (bookTopBean!=null)
                MethodManager.gotoBookDetails(requireActivity(),bookTopBean)
        }
    }
    override fun lazyLoad() {
        if (DataBeanManager.courses.isEmpty())
            mCommonPresenter.getCommon()
    }

    private fun initRecyclerView(){
        mAdapter = BookAdapter(R.layout.item_bookcase, null).apply {
            rv_list.layoutManager = GridLayoutManager(activity,4)//创建布局管理
            rv_list.adapter = mAdapter
            bindToRecyclerView(rv_list)
            rv_list.addItemDecoration(SpaceGridItemDeco1(4, DP2PX.dip2px(activity,23f),28))
            setOnItemClickListener { adapter, view, position ->
                val bookBean=books[position]
                MethodManager.gotoBookDetails(requireActivity(), bookBean)
            }
        }
    }

    /**
     * 查找本地书籍
     */
    private fun findBook(){
        if (isLoginState()){
            books= BookDaoManager.getInstance().queryAllBook(true)
            if (books.size==0){
                bookTopBean=null
            }
            else{
                bookTopBean=books[0]
                books.removeFirst()
            }
        }
        else{
            books.clear()
            bookTopBean=null
        }
        mAdapter?.setNewData(books)
        onChangeTopView()
    }


    //设置头部view显示 (当前页的第一个)
    private fun onChangeTopView(){
        if (bookTopBean!=null){
            setImageUrl(bookTopBean?.imageUrl!!,iv_content_up)
            setImageUrl(bookTopBean?.imageUrl!!,iv_content_down)
            tv_name.text=bookTopBean?.bookName
        }
        else{
            iv_content_up.setImageBitmap(null)
            iv_content_down.setImageBitmap(null)
            tv_name.text=""
        }
    }

    private fun setImageUrl(url: String,image: ImageView){
        GlideUtils.setImageRoundUrl(activity,url,image,5)
    }

    /**
     * 每天上传书籍
     */
    fun upload(tokenStr:String){
        showToast("上传书籍")
        cloudList.clear()
        val maxBooks= mutableListOf<Book>()
        val books= BookDaoManager.getInstance().queryAllBook()
        for (book in books){
            if (System.currentTimeMillis()>=book.time+Constants.halfYear){
                maxBooks.add(book)
                //判读是否存在手写内容
                if (File(book.bookDrawPath).exists()){
                    FileUploadManager(tokenStr).apply {
                        startUpload(book.bookDrawPath,book.bookId.toString())
                        setCallBack{
                            cloudList.add(CloudListBean().apply {
                                type=1
                                zipUrl=book.bodyUrl
                                downloadUrl=it
                                subType=-1
                                subTypeStr=book.subtypeStr
                                date=System.currentTimeMillis()
                                listJson= Gson().toJson(book)
                                bookId=book.bookId
                            })
                            if (cloudList.size==maxBooks.size)
                                mCloudUploadPresenter.upload(cloudList)
                        }
                    }
                }
                else{
                    cloudList.add(CloudListBean().apply {
                        type=1
                        zipUrl=book.bodyUrl
                        subType=-1
                        subTypeStr=book.subtypeStr
                        date=System.currentTimeMillis()
                        listJson= Gson().toJson(book)
                        bookId=book.bookId
                    })
                    if (cloudList.size==maxBooks.size)
                        mCloudUploadPresenter.upload(cloudList)
                }
            }
        }
    }

    //上传完成后删除书籍
    override fun uploadSuccess(cloudIds: MutableList<Int>?) {
        super.uploadSuccess(cloudIds)
        for (item in cloudList){
            val bookBean=BookDaoManager.getInstance().queryByBookID(1,item.bookId)
            //删除书籍
            FileUtils.deleteFile(File(bookBean.bookPath))
            FileUtils.deleteFile(File(bookBean.bookDrawPath))
            BookDaoManager.getInstance().deleteBook(bookBean)
        }
        findBook()
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag==Constants.USER_EVENT){
            findBook()
        }
        if (msgFlag == Constants.BOOK_EVENT) {
            findBook()
        }
    }

    override fun onRefreshData() {
        super.onRefreshData()
        lazyLoad()
        findBook()
    }
}