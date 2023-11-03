package com.bll.lnkcommon.ui.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.mvp.model.JournalList
import com.bll.lnkcommon.mvp.presenter.JournalPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IJournalView
import com.bll.lnkcommon.ui.adapter.JournalAdapter
import com.bll.lnkcommon.widget.SpaceItemDeco
import kotlinx.android.synthetic.main.common_radiogroup.*
import kotlinx.android.synthetic.main.fragment_journal.*

class JournalFragment:BaseFragment(),IJournalView {

    private val presenter=JournalPresenter(this)
    private var items= mutableListOf<JournalList.JournalBean>()
    private var mAdapter:JournalAdapter?=null

    override fun onList(list: JournalList) {
        setPageNumber(list.total)
        items=list.list
        mAdapter?.setNewData(items)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_journal
    }

    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[2])

        initTabView()
        initRecyclerView()
    }

    override fun lazyLoad() {
        pageSize=2
        fetchData()
    }

    private fun initTabView(){
        rg_group.removeAllViews()
        val tabs=DataBeanManager.journalType
        for (i in tabs.indices) {
            rg_group.addView(getRadioButton(i, tabs[i], tabs.size - 1))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, id ->
        }
    }

    private fun initRecyclerView(){
        rv_list.layoutManager = LinearLayoutManager(requireActivity())//创建布局管理
        mAdapter = JournalAdapter(R.layout.item_journal, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
        }
        rv_list.addItemDecoration(SpaceItemDeco(0,0,0,100))
    }

    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        presenter.getList(map)
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag== Constants.AUTO_REFRESH_EVENT){
            pageIndex=1
            fetchData()
        }
    }

}