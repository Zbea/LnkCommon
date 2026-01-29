package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.ui.activity.book.BookStoreTypeActivity
import com.bll.lnkcommon.ui.fragment.book.BookCaseFragment
import com.bll.lnkcommon.ui.fragment.book.BookReadingFragment
import com.bll.lnkcommon.utils.cloudManager.BookCloudUploadManager
import com.bll.lnkcommon.widget.TabRadioGroup
import kotlinx.android.synthetic.main.common_fragment_title.tv_btn
import kotlinx.android.synthetic.main.fragment_bookcase_manage.tabRadioGroup

class BookcaseManageFragment: BaseFragment() {

    private var bookReadingFragment: BookReadingFragment?=null
    private var bookCaseFragment: BookCaseFragment?=null

    private val uploadManager by lazy {
        BookCloudUploadManager(this)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_bookcase_manage
    }

    override fun initView() {
       val tabTitles = arrayListOf("阅读", "书架")

        tv_btn?.apply {
            showView(tv_btn)
            text="书城列表"
            setOnClickListener {
                customStartActivity(Intent(requireActivity(), BookStoreTypeActivity::class.java))
            }
        }

        bookCaseFragment= BookCaseFragment()
        bookReadingFragment= BookReadingFragment()

        switchFragment(3,lastFragment,bookReadingFragment)

        tabRadioGroup.setTabTitles(tabTitles)
        tabRadioGroup.setOnTabSelectedListener(object : TabRadioGroup.OnTabSelectedListener {
            override fun onTabSelected(position: Int, title: String) {
                when(position){
                    0->{
                        switchFragment(2,lastFragment,bookReadingFragment)
                    }
                    1->{
                        switchFragment(2,lastFragment,bookCaseFragment)
                    }
                }
            }
        })
    }

    override fun lazyLoad() {
    }

    /**
     * 上传两个月未使用书籍
     */
    fun upload(token: String){
        uploadManager.upload(token)
    }

}