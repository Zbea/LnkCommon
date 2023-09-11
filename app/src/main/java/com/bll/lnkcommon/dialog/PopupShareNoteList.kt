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
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.ShareNoteList
import com.bll.lnkcommon.utils.DP2PX
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlin.math.ceil

class PopupShareNoteList(var context: Context, var view: View, val total:Int) {

    private var lists= mutableListOf<ShareNoteList.ShareNoteBean>()
    private var mPopupWindow: PopupWindow? = null
    private var mAdapter: MyAdapter?=null
    private var pageSize=6
    private var pageIndex=1
    private var pageCount=1
    private var ll_page_number:LinearLayout?=null
    private var tv_page_current:TextView?=null
    private var tv_page_total:TextView?=null

    fun builder(): PopupShareNoteList {
        val popView = LayoutInflater.from(context).inflate(R.layout.popup_record_list, null, false)
        mPopupWindow = PopupWindow(context).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // 设置PopupWindow的内容view
            contentView = popView
            isFocusable = true // 设置PopupWindow可获得焦点
            isTouchable = true // 设置PopupWindow可触摸
            isOutsideTouchable = true // 设置非PopupWindow区域可触摸
            width=DP2PX.dip2px(context,280f)
        }

        ll_page_number = popView.findViewById(R.id.ll_page_number)
        tv_page_current = popView.findViewById(R.id.tv_page_current)
        tv_page_total = popView.findViewById(R.id.tv_page_total)

        val btn_page_up = popView.findViewById<TextView>(R.id.btn_page_up)
        val btn_page_down = popView.findViewById<TextView>(R.id.btn_page_down)

        btn_page_up.setOnClickListener {
            if(pageIndex>1){
                pageIndex-=1
                onClickListener?.onPage(pageIndex)
            }
        }

        btn_page_down.setOnClickListener {
            if(pageIndex<pageCount){
                pageIndex+=1
                onClickListener?.onPage(pageIndex)
            }
        }

        val rvList = popView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)//创建布局管理
        mAdapter = MyAdapter(R.layout.item_freenote_share_list, null)
        rvList.adapter=mAdapter
        mAdapter?.bindToRecyclerView(rvList)
        mAdapter?.setEmptyView(R.layout.common_empty)
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            val item=lists[position]
            ImageDialog(context,item.paths.split(","),item.bgRes.split(",")).builder()
        }
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            if (view.id==R.id.iv_load){
                onClickListener?.onDownload(position)
            }
            if (view.id==R.id.iv_delete){
                onClickListener?.onDelete(position)
            }
        }

        pageCount = ceil(total.toDouble() / pageSize).toInt()
        if (total == 0) {
            ll_page_number?.visibility=View.GONE
        } else {
            tv_page_current?.text = pageIndex.toString()
            tv_page_total?.text = pageCount.toString()
            ll_page_number?.visibility=View.VISIBLE
        }

        show()
        return this
    }

    fun setData(beans:MutableList<ShareNoteList.ShareNoteBean>){
        lists=beans
        mAdapter?.setNewData(lists)
    }

    fun deleteData(position: Int){
        mAdapter?.remove(position)
    }

    fun dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow?.dismiss()
        }
    }

    fun show() {
        if (mPopupWindow != null) {
            mPopupWindow?.showAsDropDown(view, 0, 10, Gravity.RIGHT)
        }
    }

    private var onClickListener: OnClickListener?=null

    fun setOnClickListener(onClickListener: OnClickListener)
    {
        this.onClickListener=onClickListener
    }

    interface OnClickListener{
        fun onPage(pageIndex: Int)
        fun onDelete(position: Int)
        fun onDownload(position: Int)
    }



    class MyAdapter(layoutResId: Int, data: MutableList<ShareNoteList.ShareNoteBean>?) : BaseQuickAdapter<ShareNoteList.ShareNoteBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: ShareNoteList.ShareNoteBean) {
            helper.apply {
                setText(R.id.tv_title,item.title)
                setText(R.id.tv_name,item.nickname)
                addOnClickListener(R.id.iv_load,R.id.iv_delete)
            }
        }
    }


}