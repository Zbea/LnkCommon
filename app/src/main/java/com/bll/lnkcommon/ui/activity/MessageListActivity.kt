package com.bll.lnkcommon.ui.activity

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.MessageSendDialog
import com.bll.lnkcommon.mvp.model.MessageList
import com.bll.lnkcommon.mvp.presenter.MessagePresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.MessageAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.NetworkUtil
import kotlinx.android.synthetic.main.ac_list.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus

class MessageListActivity:BaseActivity(),IContractView.IMessageView {

    private var mMessagePresenter= MessagePresenter(this)
    private var messages= mutableListOf<MessageList.MessageBean>()
    private var mAdapter:MessageAdapter?=null

    override fun onList(message: MessageList) {
        setPageNumber(message.total)
        messages=message.list
        mAdapter?.setNewData(messages)
    }

    override fun onCommitSuccess() {
        pageIndex=1
        fetchData()
    }


    override fun layoutId(): Int {
        return R.layout.ac_list
    }

    override fun initData() {
        pageSize=10
        fetchData()
    }

    override fun initView() {
        setPageTitle("消息中心")
        setImageBtn(R.mipmap.icon_save)

        iv_manager.setOnClickListener {
            MessageSendDialog(this).builder()?.setOnClickListener{
                str,ids->

            }
        }

        rv_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mAdapter = MessageAdapter(R.layout.item_message, null).apply {
            val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.setMargins(
                DP2PX.dip2px(this@MessageListActivity,50f),
                DP2PX.dip2px(this@MessageListActivity,20f),
                DP2PX.dip2px(this@MessageListActivity,50f),20)
            layoutParams.weight=1f
            rv_list.layoutParams= layoutParams
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
        }
    }

    override fun fetchData() {
        val map= HashMap<String,Any>()
        map["page"]=pageIndex
        map["size"]=pageSize
        map["type"]=2
        mMessagePresenter.getList(map)
    }

}