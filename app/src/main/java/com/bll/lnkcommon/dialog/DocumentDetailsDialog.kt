package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
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

class DocumentDetailsDialog(val context: Context) {

    fun builder(): DocumentDetailsDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_bookcase_list)
        val window= dialog.window!!
        window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        var total=0
        val items= mutableListOf<ItemDetailsBean>()

        val path = FileAddress().getPathDocument("默认")
        val documentTypeNames=FileUtils.getDirectorys(File(path).parent)

        for (name in documentTypeNames){
            val files= FileUtils.getDescFiles(FileAddress().getPathDocument(name))
            if (files.isNotEmpty()){
                items.add(ItemDetailsBean().apply {
                    typeStr=name
                    num=files.size
                    this.files=files
                })
                total+=files.size
            }
        }

        val tv_title=dialog.findViewById<TextView>(R.id.tv_title)
        tv_title.setText("文档明细")

        val tv_total=dialog.findViewById<TextView>(R.id.tv_total)
        tv_total.text="总计：${total}"

        val rv_list=dialog.findViewById<MaxRecyclerView>(R.id.rv_list)
        rv_list?.layoutManager = LinearLayoutManager(context)
        val mAdapter = ScreenshotDetailsAdapter(R.layout.item_details_list, items)
        rv_list?.adapter = mAdapter
        mAdapter.bindToRecyclerView(rv_list)
        rv_list?.addItemDecoration(SpaceItemDeco(30))
        mAdapter.setOnChildClickListener{ parentPos,pos->
            dialog.dismiss()
            MethodManager.gotoDocument(context, items[parentPos].files[pos])
        }

        return this
    }


    class ScreenshotDetailsAdapter(layoutResId: Int, data: List<ItemDetailsBean>?) : BaseQuickAdapter<ItemDetailsBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: ItemDetailsBean) {
            helper.setText(R.id.tv_book_type,item.typeStr)
            helper.setText(R.id.tv_book_num,"( ${item.num}) ")

            val recyclerView = helper.getView<RecyclerView>(R.id.rv_list)
            recyclerView?.layoutManager = FlowLayoutManager()
            val mAdapter = ChildAdapter(R.layout.item_details_list_name,item.files)
            recyclerView?.adapter = mAdapter
            mAdapter.setOnItemClickListener { adapter, view, position ->
                listener?.onClick(helper.adapterPosition,position)
            }
        }

        class ChildAdapter(layoutResId: Int,  data: List<File>?) : BaseQuickAdapter<File, BaseViewHolder>(layoutResId, data) {
            override fun convert(helper: BaseViewHolder, item: File) {
                helper.apply {
                    helper.setText(R.id.tv_name, FileUtils.getFileName(item.name))
                }
            }
        }

        private var listener: OnChildClickListener? = null

        fun interface OnChildClickListener {
            fun onClick(parentPos:Int,pos: Int)
        }
        fun setOnChildClickListener(listener: OnChildClickListener?) {
            this.listener = listener
        }
    }

}