package com.bll.lnkcommon.dialog


import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.utils.KeyboardUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class WalletStudentRechargeDialog(
    context: Context,
    private val money: Int
) : BaseDialog(context) {

    private var students = mutableListOf<StudentBean>()

    // 布局ID
    override fun getLayoutResId(): Int = R.layout.dialog_wallet_student_recharge

    // 初始化视图和业务逻辑
    override fun initView(contentView: View) {
        // 初始化控件
        val tvOK = contentView.findViewById<TextView>(R.id.tv_ok)
        val tvCancel = contentView.findViewById<TextView>(R.id.tv_cancel)
        val etContent = contentView.findViewById<EditText>(R.id.et_num)
        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)

        // 设置输入框提示
        etContent.hint = "最大$money"

        // 获取学生列表
        students = DataBeanManager.students

        // 初始化列表
        val mAdapter = MyAdapter(R.layout.item_message_student, students)
        rvList?.layoutManager = LinearLayoutManager(context)
        mAdapter.bindToRecyclerView(rvList)

        // 列表项点击事件
        mAdapter.setOnItemClickListener { _, _, position ->
            // 重置所有选中状态
            students.forEach { it.isCheck = false }
            // 设置当前项选中
            students[position].isCheck = true
            mAdapter.notifyDataSetChanged()
        }

        // 取消按钮
        tvCancel?.setOnClickListener { dismiss() }

        // 确认按钮
        tvOK?.setOnClickListener {
            val contentStr = etContent?.text.toString()
            if (contentStr.isNotEmpty() && getCheckIds() != 0) {
                dismiss()
                listener?.onSend(contentStr.toInt(), getCheckIds())
            }
        }

        // 弹窗关闭时隐藏软键盘
        dialog?.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }
    }

    // 获取选中的学生ID
    private fun getCheckIds(): Int {
        var id = 0
        students.forEach {
            if (it.isCheck) id = it.accountId
        }
        return id
    }

    // 点击监听
    private var listener: OnClickListener? = null
    fun interface OnClickListener {
        fun onSend(money: Int, id: Int)
    }
    fun setOnClickListener(listener: OnClickListener?) {
        this.listener = listener
    }

    override fun builder(): WalletStudentRechargeDialog {
        super.builder()
        return this
    }

    // 学生列表适配器
    class MyAdapter(layoutResId: Int, private val items: MutableList<StudentBean>) : BaseQuickAdapter<StudentBean, BaseViewHolder>(layoutResId, items) {
        override fun convert(helper: BaseViewHolder, item: StudentBean?) {
            helper.setText(R.id.tv_class_name, item?.nickname)
            helper.setChecked(R.id.cb_check, item?.isCheck ?: false)
        }
    }
}