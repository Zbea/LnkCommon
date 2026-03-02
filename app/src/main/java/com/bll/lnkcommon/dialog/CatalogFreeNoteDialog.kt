package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.manager.FreeNoteDaoManager
import com.bll.lnkcommon.mvp.model.FreeNoteBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlin.math.ceil

/**
 * 自由笔记目录弹窗
 * @param context 上下文
 * @param date 选中日期（用于标记当前笔记）
 */
class CatalogFreeNoteDialog(context: Context, private val date: Long) : BaseDialog(context) {

    private var list = mutableListOf<FreeNoteBean>()
    private var mAdapter: MyAdapter? = null
    private var pageIndex = 1
    private val pageSize = 13
    private var pageCount = 0

    private var onSelectListener: OnDialogClickListener? = null

    override val defaultGravity: Int = Gravity.BOTTOM or Gravity.START
    override val defaultXOffset: Float = 42f
    override val defaultYOffset: Float = 5f

    override fun getLayoutResId(): Int = R.layout.dialog_freenote_list

    override fun initView(contentView: View) {

        val llPageNumber = contentView.findViewById<LinearLayout>(R.id.ll_page_number)
        val tvPageCurrent = contentView.findViewById<TextView>(R.id.tv_page_current)
        val tvPageTotal = contentView.findViewById<TextView>(R.id.tv_page_total)
        val btnPageUp = contentView.findViewById<TextView>(R.id.btn_page_up)
        val btnPageDown = contentView.findViewById<TextView>(R.id.btn_page_down)
        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)

        val total = FreeNoteDaoManager.getInstance().queryListByType(0).size
        pageCount = ceil(total.toDouble() / pageSize).toInt()
        if (total == 0) {
            llPageNumber?.visibility = View.INVISIBLE
        } else {
            tvPageCurrent?.text = pageIndex.toString()
            tvPageTotal?.text = pageCount.toString()
            llPageNumber?.visibility = View.VISIBLE
        }

        btnPageUp?.setOnClickListener {
            if (pageIndex > 1) {
                pageIndex -= 1
                findFreeNotes()
                tvPageCurrent?.text = pageIndex.toString()
            }
        }

        btnPageDown?.setOnClickListener {
            if (pageIndex < pageCount) {
                pageIndex += 1
                findFreeNotes()
                tvPageCurrent?.text = pageIndex.toString()
            }
        }

        rvList.layoutManager = LinearLayoutManager(context)
        mAdapter = MyAdapter(R.layout.item_free_note, list, date)
        mAdapter?.bindToRecyclerView(rvList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            onSelectListener?.onClick(list[position])
            dismiss()
        }

        findFreeNotes()
    }

    private fun findFreeNotes() {
        list = FreeNoteDaoManager.getInstance().queryListByType(pageIndex, pageSize)
        mAdapter?.setNewData(list)
    }

    fun interface OnDialogClickListener {
        fun onClick(item: FreeNoteBean)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener): CatalogFreeNoteDialog {
        this.onSelectListener = listener
        return this
    }

    override fun builder(): CatalogFreeNoteDialog {
        super.builder()
        return this
    }

    private class MyAdapter(layoutResId: Int, data: List<FreeNoteBean>?, private val date: Long) : BaseQuickAdapter<FreeNoteBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: FreeNoteBean) {
            helper.setText(R.id.tv_title, item.title)
            helper.setVisible(R.id.iv_now, date == item.date)
        }
    }
}