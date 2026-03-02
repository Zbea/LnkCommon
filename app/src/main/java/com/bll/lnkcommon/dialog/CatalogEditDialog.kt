package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.utils.KeyboardUtils
import com.bll.lnkcommon.utils.SToast
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder


/**
 * 目录编辑弹窗
 * @param context 上下文
 * @param countSize 页码上限
 */
class CatalogEditDialog(context: Context, private val countSize: Int) : BaseDialog(context) {

    private var pages = mutableListOf<Int>()
    private var type = 1


    private var onDialogClickListener: OnDialogClickListener? = null

    override fun getLayoutResId(): Int = R.layout.dialog_catalog_edit

    override fun initView(contentView: View) {

        val etName = contentView.findViewById<EditText>(R.id.et_name)
        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        val etPageStart = contentView.findViewById<EditText>(R.id.et_page_start)
        val etPageEnd = contentView.findViewById<EditText>(R.id.et_page_end)
        val llBatch = contentView.findViewById<LinearLayout>(R.id.ll_batch)
        val rgGroup = contentView.findViewById<RadioGroup>(R.id.rg_group)

        val list = mutableListOf<ItemList>()
        val pageItem1 = ItemList().apply { isAdd = false }
        list.add(pageItem1)
        val pageItem2 = ItemList().apply { isAdd = true }
        list.add(pageItem2)

        rgGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_single) {
                type = 1
                llBatch?.visibility = View.GONE
                rvList?.visibility = View.VISIBLE
            } else {
                type = 2
                rvList?.visibility = View.GONE
                llBatch?.visibility = View.VISIBLE
            }
        }

        val mAdapter = MyAdapter(R.layout.item_drawing_commit_page, list)
        rvList?.layoutManager = GridLayoutManager(context, 6)
        mAdapter.bindToRecyclerView(rvList)
        rvList?.addItemDecoration(SpaceGridItemDeco(6, 20))

        mAdapter.setOnItemClickListener { _, _, position ->
            if (list[position].isAdd) {
                list[position].page = 0
                val newItem = ItemList().apply { isAdd = false }
                list.add(list.size - 1, newItem)
                mAdapter.setNewData(list)
            }
        }

        btnCancel?.setOnClickListener { dismiss() }

        btnOk?.setOnClickListener {
            val contentStr = etName?.text.toString().trim()
            if (contentStr.isEmpty()) {
                showToast("请输入目录标题")
                return@setOnClickListener
            }
            // 收集页码
            pages.clear()
            if (type == 1) {
                list.filter { it.page > 0 }
                    .map { it.page }
                    .distinct()
                    .forEach { pages.add(it) }
            } else {
                val startStr = etPageStart?.text.toString()
                val endStr = etPageEnd?.text.toString()
                if (startStr.isNotEmpty() && endStr.isNotEmpty()) {
                    val pageStart = startStr.toInt()
                    val pageEnd = endStr.toInt()
                    if (pageEnd > pageStart) {
                        (pageStart..pageEnd).distinct().forEach { pages.add(it) }
                    }
                }
            }

            // 页码校验
            pages.sort()
            when {
                pages.isEmpty() -> showToast("请输入页码")
                pages.last() > countSize -> showToast("输入的页码超出")
                else -> {
                    // 转换为真实页码（-1）
                    val realPages = pages.map { it - 1 }.toMutableList()
                    onDialogClickListener?.onClick(contentStr, realPages)
                    dismiss()
                }
            }
        }

        dialog?.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }
    }

    private fun showToast(str: String) {
        SToast.showText(str)
    }

    fun interface OnDialogClickListener {
        fun onClick(contentStr: String, pages: List<Int>)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener): CatalogEditDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): CatalogEditDialog {
        super.builder()
        return this
    }

    class MyAdapter(layoutResId: Int, list: List<ItemList>) : BaseQuickAdapter<ItemList, BaseViewHolder>(layoutResId, list) {
        override fun convert(helper: BaseViewHolder, item: ItemList) {
            helper.setVisible(R.id.et_name, !item.isAdd)
            helper.setVisible(R.id.iv_add, item.isAdd)
            val etName = helper.getView<EditText>(R.id.et_name)
            etName.setText(if (item.page != 0) item.page.toString() else "")

            etName.doAfterTextChanged {
                val str = it.toString()
                if (str.isNotEmpty()) {
                    data[helper.adapterPosition].page = str.toInt()
                }
            }

            if (helper.adapterPosition == data.size - 2) {
                etName.requestFocus()
                etName.isFocusableInTouchMode = true
            }
        }
    }
}