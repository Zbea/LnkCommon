package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.ItemDetailsBean
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.FlowLayoutManager
import com.bll.lnkcommon.widget.MaxRecyclerView
import com.bll.lnkcommon.widget.SpaceItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.io.File

class ScreenshotDetailsDialog(context: Context) : BaseDialog(context) {

    // 布局ID
    override fun getLayoutResId(): Int = R.layout.dialog_bookcase_list

    // 初始化视图和业务逻辑
    override fun initView(contentView: View) {
        var total = 0
        val items = mutableListOf<ItemDetailsBean>()

        // 查询截图分类并添加"未分类"项
        val screenTypes = ItemTypeDaoManager.getInstance().queryAll(3)
        screenTypes.add(0, ItemTypeBean().apply {
            path = FileAddress().getPathScreen("未分类")
            title = "未分类"
        })

        // 遍历分类，统计文件数量
        for (item in screenTypes) {
            val files = FileUtils.getDescTimeFiles(item.path)
            if (files.isNotEmpty()) {
                items.add(ItemDetailsBean().apply {
                    typeStr = item.title
                    num = files.size
                    this.files = files
                })
                total += files.size
            }
        }

        // 设置标题和总计
        val tvTitle = contentView.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = "截图明细"

        val tvTotal = contentView.findViewById<TextView>(R.id.tv_total)
        tvTotal.text = "总计：${total}"

        // 初始化列表
        val rvList = contentView.findViewById<MaxRecyclerView>(R.id.rv_list)
        rvList?.layoutManager = LinearLayoutManager(context)
        val mAdapter = ScreenshotDetailsAdapter(R.layout.item_details_list, items)
        rvList?.adapter = mAdapter
        mAdapter.bindToRecyclerView(rvList)
        rvList?.addItemDecoration(SpaceItemDeco(30))
    }

    // 截图明细适配器
    class ScreenshotDetailsAdapter(layoutResId: Int, data: List<ItemDetailsBean>?) : BaseQuickAdapter<ItemDetailsBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: ItemDetailsBean) {
            helper.setText(R.id.tv_book_type, item.typeStr)
            helper.setText(R.id.tv_book_num, "( ${item.num} )")

            // 子列表初始化
            val recyclerView = helper.getView<RecyclerView>(R.id.rv_list)
            recyclerView?.layoutManager = FlowLayoutManager()
            val mAdapter = ChildAdapter(R.layout.item_details_list_name, item.files)
            recyclerView?.adapter = mAdapter

            // 子项点击事件
            mAdapter.setOnItemClickListener { _, _, position ->
                listener?.onClick(helper.adapterPosition, position)
            }
        }

        // 子适配器
        class ChildAdapter(layoutResId: Int, data: List<File>?) : BaseQuickAdapter<File, BaseViewHolder>(layoutResId, data) {
            override fun convert(helper: BaseViewHolder, item: File) {
                helper.setText(R.id.tv_name, item.name.replace(".png", ""))
            }
        }

        // 子项点击监听
        private var listener: OnChildClickListener? = null
        fun interface OnChildClickListener {
            fun onClick(parentPos: Int, pos: Int)
        }
        fun setOnChildClickListener(listener: OnChildClickListener?) {
            this.listener = listener
        }
    }
}