package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.RadioButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.mvp.model.AccountQdBean
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class WalletBuyDialog(
    context: Context,
    private val list: List<AccountQdBean>
) : BaseDialog(context) {

    private var id = 0

    override fun getLayoutResId(): Int = R.layout.dialog_account_xd

    override fun initView(contentView: View) {
        // 初始化控件
        val recyclerView = contentView.findViewById<RecyclerView>(R.id.rv_list)
        val rbWx = contentView.findViewById<RadioButton>(R.id.rb_wx)

        // 初始化列表
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        val mAdapter = AccountXdAdapter(R.layout.item_account_smoney, list)
        recyclerView.adapter = mAdapter
        recyclerView.addItemDecoration(SpaceGridItemDeco(4, 40))

        // 列表项点击事件
        mAdapter.setOnItemClickListener { _, _, position ->
            mAdapter.setItemView(position)
            id = list[position].id
        }

        // 取消按钮
        btnCancel?.setOnClickListener { dismiss() }

        // 确认按钮
        btnOk?.setOnClickListener {
            dismiss()
            val payType = if (rbWx.isChecked) 2 else 1
            listener?.onClick(payType, id.toString())
        }

        // 默认选中第一个项
        if (list.isNotEmpty()) {
            id = list[0].id
        }
    }

    // 点击监听
    private var listener: OnDialogClickListener? = null
    fun interface OnDialogClickListener {
        fun onClick(payType: Int, id: String)
    }
    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    override fun builder(): WalletBuyDialog {
        super.builder()
        return this
    }

    // 账户选择适配器
    class AccountXdAdapter(layoutResId: Int, data: List<AccountQdBean>?) : BaseQuickAdapter<AccountQdBean, BaseViewHolder>(layoutResId, data) {

        var mPosition = 0

        override fun convert(helper: BaseViewHolder, item: AccountQdBean) {
            helper.setText(R.id.tv_name, item.amount.toString())

            // 选中状态样式
            if (helper.adapterPosition == mPosition) {
                helper.setBackgroundRes(R.id.tv_name, R.drawable.bg_black_solid_5dp_corner)
                helper.setTextColor(R.id.tv_name, mContext.resources.getColor(R.color.white))
            } else {
                helper.setBackgroundRes(R.id.tv_name, R.drawable.bg_gray_stroke_5dp_corner)
                helper.setTextColor(R.id.tv_name, mContext.resources.getColor(R.color.black))
            }
        }

        fun setItemView(position: Int) {
            mPosition = position
            notifyDataSetChanged()
        }
    }
}