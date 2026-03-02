package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.ItemDetailsBean
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.FlowLayoutManager
import com.bll.lnkcommon.widget.MaxRecyclerView
import com.bll.lnkcommon.widget.SpaceItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.io.File
import com.bll.lnkcommon.base.BaseDialog

/**
 * 文档明细弹窗：继承BaseDialog
 * @param context 上下文
 */
class DocumentDetailsDialog(context: Context) : BaseDialog(context) {

    override fun getLayoutResId(): Int = R.layout.dialog_bookcase_list

    override fun initView(contentView: View) {

        val tvTitle = contentView.findViewById<TextView>(R.id.tv_title)
        val tvTotal = contentView.findViewById<TextView>(R.id.tv_total)
        val rvList = contentView.findViewById<MaxRecyclerView>(R.id.rv_list)

        tvTitle.text = "文档明细"

        var total = 0
        val items = mutableListOf<ItemDetailsBean>()
        val path = FileAddress().getPathDocument("默认")
        val documentTypeNames = FileUtils.getDirectorys(File(path).parent)

        documentTypeNames.forEach { name ->
            val files = FileUtils.getDescFiles(FileAddress().getPathDocument(name))
            if (files.isNotEmpty()) {
                items.add(ItemDetailsBean().apply {
                    typeStr = name
                    num = files.size
                    this.files = files
                })
                total += files.size
            }
        }

        tvTotal.text = "总计：${total}"

        rvList.layoutManager = LinearLayoutManager(context)
        val mAdapter = ScreenshotDetailsAdapter(R.layout.item_details_list, items)
        rvList.adapter = mAdapter
        mAdapter.bindToRecyclerView(rvList)
        rvList.addItemDecoration(SpaceItemDeco(30))

        mAdapter.setOnChildClickListener { parentPos, pos ->
            dismiss()
            MethodManager.gotoDocument(context, items[parentPos].files[pos])
        }
    }

    override fun builder(): DocumentDetailsDialog {
        super.builder()
        return this
    }

    class ScreenshotDetailsAdapter(layoutResId: Int, data: List<ItemDetailsBean>?) :
        BaseQuickAdapter<ItemDetailsBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: ItemDetailsBean) {
            helper.setText(R.id.tv_book_type, item.typeStr)
            helper.setText(R.id.tv_book_num, "( ${item.num}) ")

            val recyclerView = helper.getView<RecyclerView>(R.id.rv_list)
            recyclerView.layoutManager = FlowLayoutManager()
            val mAdapter = ChildAdapter(R.layout.item_details_list_name, item.files)
            recyclerView.adapter = mAdapter

            // 子列表条目点击
            mAdapter.setOnItemClickListener { _, _, position ->
                listener?.onClick(helper.adapterPosition, position)
            }
        }

        // 子Adapter
        class ChildAdapter(layoutResId: Int, data: List<File>?) :
            BaseQuickAdapter<File, BaseViewHolder>(layoutResId, data) {
            override fun convert(helper: BaseViewHolder, item: File) {
                helper.setText(R.id.tv_name, FileUtils.getFileName(item.name))
            }
        }

        // 子条目点击回调
        private var listener: OnChildClickListener? = null

        fun interface OnChildClickListener {
            fun onClick(parentPos: Int, pos: Int)
        }

        fun setOnChildClickListener(listener: OnChildClickListener?): ScreenshotDetailsAdapter {
            this.listener = listener
            return this
        }
    }
}