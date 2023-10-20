package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.Book
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.ui.activity.AccountLoginActivity
import com.bll.lnkcommon.ui.activity.book.BookcaseTypeListActivity
import com.bll.lnkcommon.ui.adapter.BookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.ui.activity.ScreenshotManagerActivity
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.common_fragment_title.*
import kotlinx.android.synthetic.main.fragment_bookcase.*

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