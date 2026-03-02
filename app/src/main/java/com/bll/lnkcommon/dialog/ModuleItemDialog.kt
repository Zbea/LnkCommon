package com.bll.lnkcommon.dialog


import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.mvp.model.ModuleBean
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class ModuleItemDialog(context: Context, private val title: String, private val list: MutableList<ModuleBean>) :BaseDialog(context) {

    // 自定义宽度
    override val defaultWidth: Int
        get() = if (list.size > 4) DP2PX.dip2px(context,700f) else  DP2PX.dip2px(context,500f)

    override fun getLayoutResId(): Int = R.layout.dialog_module_select

    override fun initView(contentView: View) {
        // 标题
        val tvName = contentView.findViewById<TextView>(R.id.tv_name)
        tvName?.text = title

        // 关闭按钮
        val ivCancel = contentView.findViewById<ImageView>(R.id.iv_close)
        ivCancel?.setOnClickListener { dismiss() }

        // 列表列数
        val count = if (list.size > 4) 3 else 2

        // 列表初始化
        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        rvList?.layoutManager = GridLayoutManager(context, count)
        val mAdapter = MAdapter(R.layout.item_module, list)
        mAdapter.bindToRecyclerView(rvList)
        rvList?.addItemDecoration(SpaceGridItemDeco(count, 40))

        // 列表点击事件
        mAdapter.setOnItemClickListener { _, _, position ->
            listener?.onClick(list[position])
            dismiss()
        }
    }


    private var listener: OnDialogClickListener? = null
    fun interface OnDialogClickListener {
        fun onClick(item: ModuleBean)
    }
    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    override fun builder(): ModuleItemDialog {
        super.builder()
        return this
    }

    private class MAdapter(layoutResId: Int, data: List<ModuleBean>?) : BaseQuickAdapter<ModuleBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: ModuleBean) {
            helper.setText(R.id.tv_name, item.name)
            helper.getView<ImageView>(R.id.iv_image).setImageResource(item.resId)
        }
    }
}