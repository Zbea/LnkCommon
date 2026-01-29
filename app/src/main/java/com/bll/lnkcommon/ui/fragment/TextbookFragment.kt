package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.ItemSelectorDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.TextbookGreenDaoManager
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.book.TextbookBean
import com.bll.lnkcommon.mvp.presenter.MyHomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IMyHomeworkView
import com.bll.lnkcommon.ui.activity.book.TextBookDetailsActivity
import com.bll.lnkcommon.ui.activity.book.TextBookStoreActivity
import com.bll.lnkcommon.ui.adapter.TextBookAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.cloudManager.TextBookCloudUploadManager
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.common_fragment_title.tv_btn
import kotlinx.android.synthetic.main.fragment_list_tab.rv_list

class TextbookFragment : BaseFragment(), IMyHomeworkView {

    private val presenter = MyHomeworkPresenter(this)
    private var mAdapter: TextBookAdapter? = null
    private var textbooks = mutableListOf<TextbookBean>()
    private var tabId = 0
    private var position = 0
    private var textTypes= mutableListOf<ItemTypeBean>()

    private val uploadManager by lazy {
        TextBookCloudUploadManager(this)
    }

    override fun onCreateSuccess() {
        showToast("设置作业本成功")
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_tab
    }

    override fun initView() {
        pageSize = 12
        setTitle(DataBeanManager.mainListTitle[5])
        showView(tv_btn)

        tv_btn.apply {
            text="教材列表"
            setOnClickListener {
                customStartActivity(Intent(requireActivity(), TextBookStoreActivity::class.java))
            }
        }

        initTab()
        initRecyclerView()
    }

    override fun lazyLoad() {
        fetchData()
    }

    private fun initTab() {
        textTypes=DataBeanManager.textBookTypes
        mTabTypeAdapter?.setNewData(textTypes)
    }

    override fun onTabClickListener(view: View, position: Int) {
        tabId = position
        pageIndex = 1
        fetchData()
    }

    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(requireActivity(),15f),
            DP2PX.dip2px(requireActivity(),50f),
            DP2PX.dip2px(requireActivity(),15f),0)
        layoutParams.weight=1f
        rv_list?.layoutParams= layoutParams

        rv_list?.layoutManager = GridLayoutManager(activity,4)//创建布局管理
        rv_list?.addItemDecoration(SpaceGridItemDeco(4,DP2PX.dip2px(requireActivity(),30f)))
        mAdapter = TextBookAdapter(R.layout.item_bookstore, null).apply {
            bindToRecyclerView(rv_list)
            setOnItemClickListener { adapter, view, position ->
                val book = textbooks[position]
                MethodManager.gotoTextBookDetails(activity,book)
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this@TextbookFragment.position=position
                onLongClick(textbooks[position])
                true
            }
        }
    }

    //长按显示课本管理
    private fun onLongClick(book: TextbookBean) {
        val beans = mutableListOf<ItemList>()
        beans.add(ItemList().apply {
            name = "删除"
            resId = R.mipmap.icon_setting_delete
        })
//        if (tabId >1&&DataBeanManager.students.size>0) {
//            beans.add(ItemList().apply {
//                name = "设置作业"
//                resId = R.mipmap.icon_setting_set
//            })
//        }

        LongClickManageDialog(requireActivity(), book.bookName, beans).builder()
            .setOnDialogClickListener {
                if (it == 0) {
                    MethodManager.deleteTextbook(book)
                } else {
                    val students=DataBeanManager.students
                    if (students.size==1){
                        val map = HashMap<String, Any>()
                        map["name"] = book.bookName
                        map["type"] = 2
                        map["childId"] = students[0].accountId
                        map["bookId"] = book.bookId
                        map["imageUrl"] = book.imageUrl
                        map["subject"] = book.subject
                        presenter.createHomeworkType(map)
                    }
                    else{
                        val lists= mutableListOf<ItemList>()
                        for (item in students){
                            lists.add(ItemList(item.accountId,item.nickname))
                        }
                        ItemSelectorDialog(requireActivity(),"选择学生",lists).builder().setOnDialogClickListener{pos->
                            val map = HashMap<String, Any>()
                            map["name"] = book.bookName
                            map["type"] = 2
                            map["childId"] = students[pos].accountId
                            map["bookId"] = book.bookId
                            map["imageUrl"] = book.imageUrl
                            map["subject"] = book.subject
                            presenter.createHomeworkType(map)
                        }
                    }
                }
            }
    }

    override fun fetchData() {
        textbooks = TextbookGreenDaoManager.getInstance().queryAllTextBook(tabId, pageIndex, pageSize)
        val total = TextbookGreenDaoManager.getInstance().queryAllTextBook(tabId)
        setPageNumber(total.size)
        mAdapter?.setNewData(textbooks)
    }

    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            Constants.TEXT_BOOK_EVENT->{
                fetchData()
            }
        }
    }

    /**
     * 上传两个月未使用书籍
     */
    fun upload(token: String){
        uploadManager.upload(token)
    }
}