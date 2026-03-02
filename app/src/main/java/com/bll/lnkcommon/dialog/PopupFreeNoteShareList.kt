package com.bll.lnkcommon.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.mvp.model.ShareNoteList
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BasePopupWindow
import com.bll.lnkcommon.dialog.PopupFreeNoteReceiveList.MyAdapter
import com.bll.lnkcommon.manager.FreeNoteDaoManager
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.SToast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlin.math.ceil

class PopupFreeNoteShareList(context: Context, anchorView: View, val total:Int)
    : BasePopupWindow(
    context = context,
    anchorView = anchorView,
    layoutWidth = DP2PX.dip2px(context, 280f),
    xOffset =-DP2PX.dip2px(context, 280f)+anchorView.width,
    yOffset = 0)
{

    private var lists= mutableListOf<ShareNoteList.ShareNoteBean>()
    private var mAdapter: MyAdapter?=null
    private var pageSize=6
    private var pageIndex=1
    private var pageCount=1

    private var ll_page_number:LinearLayout?=null
    private var tv_page_current:TextView?=null
    private var tv_page_total:TextView?=null

    override fun getLayoutResId(): Int = R.layout.popup_freenote_list

    override fun initView() {
        initPageViews()
        initRecyclerView()
        calculatePageCount()
    }

    /**
     * 初始化分页控件
     */
    private fun initPageViews() {
        ll_page_number = contentView.findViewById(R.id.ll_page_number)
        tv_page_current = contentView.findViewById(R.id.tv_page_current)
        tv_page_total = contentView.findViewById(R.id.tv_page_total)

        // 上一页按钮点击
        contentView.findViewById<TextView>(R.id.btn_page_up).setOnClickListener {
            if (pageIndex > 1) {
                pageIndex -= 1
                onClickListener?.onPage(pageIndex)
                tv_page_current?.text = pageIndex.toString() // 更新当前页显示
            }
        }

        // 下一页按钮点击
        contentView.findViewById<TextView>(R.id.btn_page_down).setOnClickListener {
            if (pageIndex < pageCount) {
                pageIndex += 1
                onClickListener?.onPage(pageIndex)
                tv_page_current?.text = pageIndex.toString() // 更新当前页显示
            }
        }
    }

    /**
     * 初始化RecyclerView和Adapter
     */
    private fun initRecyclerView() {
        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)//创建布局管理
        mAdapter = MyAdapter(R.layout.item_freenote_share_list, null)
        mAdapter?.bindToRecyclerView(rvList)
        mAdapter?.setEmptyView(R.layout.common_empty)
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            val item=lists[position]
            ImageDialog(context,item.paths.split(","),item.bgRes.split(",")).builder()
        }
    }

    /**
     * 计算分页总数并更新UI
     */
    private fun calculatePageCount() {
        pageCount = ceil(total.toDouble() / pageSize).toInt()
        if (total == 0) {
            ll_page_number?.visibility = View.GONE
        } else {
            tv_page_current?.text = pageIndex.toString()
            tv_page_total?.text = pageCount.toString()
            ll_page_number?.visibility = View.VISIBLE
        }
    }

    fun setData(beans:MutableList<ShareNoteList.ShareNoteBean>){
        lists=beans
        mAdapter?.setNewData(lists)
    }

    private var onClickListener: OnClickListener?=null

    fun setOnClickListener(onClickListener: OnClickListener)
    {
        this.onClickListener=onClickListener
    }

    interface OnClickListener{
        fun onPage(pageIndex: Int)
    }

    class MyAdapter(layoutResId: Int, data: MutableList<ShareNoteList.ShareNoteBean>?) : BaseQuickAdapter<ShareNoteList.ShareNoteBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: ShareNoteList.ShareNoteBean) {
            helper.apply {
                setText(R.id.tv_title,item.title)
                setText(R.id.tv_name,item.nickname)
                setText(R.id.tv_time,DateUtils.longToStringWeek(DateUtils.dateStrToLong(item.createTime)))
            }
        }
    }


}