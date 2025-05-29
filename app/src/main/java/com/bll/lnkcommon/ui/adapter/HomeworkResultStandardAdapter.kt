package com.bll.lnkcommon.ui.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.ResultStandardItem
import com.bll.lnkcommon.mvp.model.ResultStandardItem.ResultChildItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class HomeworkResultStandardAdapter(layoutResId: Int, data: List<ResultStandardItem>?) : BaseQuickAdapter<ResultStandardItem, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: ResultStandardItem) {
        helper.setText(R.id.tv_title,item.title)

        val recyclerView=helper.getView<RecyclerView>(R.id.rv_list)
        recyclerView?.layoutManager = GridLayoutManager(mContext,3)
        val mAdapter = ChildAdapter(R.layout.item_homework_result_standard_child,item.list)
        recyclerView?.adapter = mAdapter
        mAdapter.bindToRecyclerView(recyclerView)
        mAdapter.setOnItemClickListener { adapter, view, position ->
            listener?.onClick(helper.adapterPosition,position)
        }
    }

    class ChildAdapter(layoutResId: Int,  data: List<ResultChildItem>?) : BaseQuickAdapter<ResultChildItem, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: ResultChildItem) {
            helper.setText(R.id.tv_score,item.sortStr)
            helper.setImageResource(R.id.iv_result,if (item.isCheck) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)
        }
    }

    private var listener: OnItemChildClickListener? = null

    fun interface OnItemChildClickListener {
        fun onClick(position:Int,childPos: Int)
    }

    fun setCustomItemChildClickListener(listener: OnItemChildClickListener?) {
        this.listener = listener
    }

}
