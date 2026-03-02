package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.manager.DiaryDaoManager
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.SpaceItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.io.File
import com.bll.lnkcommon.base.BaseDialog

/**
 * 日记上传列表弹窗
 * @param context 上下文
 */
class DiaryUploadListDialog(context: Context) : BaseDialog(context) {

    private var onDialogClickListener: OnDialogClickListener? = null

    override fun getLayoutResId(): Int = R.layout.dialog_diary_upload_list

    override fun initView(contentView: View) {

        val diaryTypes = ItemTypeDaoManager.getInstance().queryAllOrderDesc(4)

        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)
        val mAdapter = MyAdapter(R.layout.item_diary_upload, diaryTypes)
        mAdapter.bindToRecyclerView(rvList)
        mAdapter.setEmptyView(R.layout.common_empty)
        rvList.addItemDecoration(SpaceItemDeco(DP2PX.dip2px(context, 10f)))
        mAdapter.setOnItemClickListener { _, _, position ->
            onDialogClickListener?.onClick(diaryTypes[position].typeId)
            dismiss()
        }

        mAdapter.setOnItemChildClickListener { _, view, position ->
            if (view.id == R.id.iv_delete) {
                CommonDialog(context).setContent("确定删除？").builder()
                    .setOnDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun ok() {
                            val item = diaryTypes[position]
                            // 删除日记文件和数据库记录
                            val diaryBeans = DiaryDaoManager.getInstance().queryList(item.typeId)
                            diaryBeans.forEach { diaryBean ->
                                val path = FileAddress().getPathDiary(DateUtils.longToStringCalender(diaryBean.date))
                                FileUtils.deleteFile(File(path))
                                DiaryDaoManager.getInstance().delete(diaryBean)
                            }
                            // 删除类型记录并更新列表
                            ItemTypeDaoManager.getInstance().deleteBean(item)
                            mAdapter.remove(position)
                        }
                    })
            }
        }
    }

    fun interface OnDialogClickListener {
        fun onClick(typeId: Int)
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener): DiaryUploadListDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): DiaryUploadListDialog {
        super.builder()
        return this
    }

    class MyAdapter(layoutResId: Int, data: List<ItemTypeBean>?) : BaseQuickAdapter<ItemTypeBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: ItemTypeBean) {
            helper.setText(R.id.tv_name, item.title)
            helper.addOnClickListener(R.id.iv_delete)
        }
    }
}