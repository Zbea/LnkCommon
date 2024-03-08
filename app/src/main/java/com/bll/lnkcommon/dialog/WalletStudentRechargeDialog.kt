package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.KeyboardUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder


class WalletStudentRechargeDialog(private val context: Context,private val money:Int) {

    private var students= mutableListOf<StudentBean>()

    private var dialog: Dialog?=null

    fun builder(): WalletStudentRechargeDialog? {
        dialog= Dialog(context)
        dialog?.setContentView(R.layout.dialog_wallet_student_recharge)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()

        val tvOK = dialog?.findViewById<TextView>(R.id.tv_ok)
        val tvCancel = dialog?.findViewById<TextView>(R.id.tv_cancel)
        val et_content = dialog?.findViewById<EditText>(R.id.et_num)
        val rvList=dialog?.findViewById<RecyclerView>(R.id.rv_list)
        et_content?.hint = "最大$money"

        students=DataBeanManager.students

        val mAdapter=MyAdapter(R.layout.item_message_student,students)
        rvList?.layoutManager=LinearLayoutManager(context)
        rvList?.adapter=mAdapter
        mAdapter.bindToRecyclerView(rvList)
        mAdapter.setOnItemClickListener { adapter, view, position ->
            for (item in students){
                item.isCheck=false
            }
            students[position].isCheck=true
            mAdapter.notifyDataSetChanged()
        }

        tvCancel?.setOnClickListener { dismiss() }
        tvOK?.setOnClickListener {
            val contentStr=et_content?.text.toString()
            if (contentStr.isNotEmpty()&& getCheckIds().isNotEmpty())
            {
                dismiss()
                listener?.onSend(contentStr.toInt(),getCheckIds())
            }
        }

        dialog?.setOnDismissListener {
            KeyboardUtils.hideSoftKeyboard(context)
        }

        return this
    }

    private fun getCheckIds():List<Int>{
        val ids= mutableListOf<Int>()
        for (item in students){
            if (item.isCheck)
                ids.add(item.childId)
        }
        return ids
    }

    fun show(){
        dialog?.show()
    }

    fun dismiss(){
        dialog?.dismiss()
    }

    private var listener: OnClickListener? = null

    fun interface OnClickListener {
        fun onSend(money:Int,ids: List<Int>)
    }

    fun setOnClickListener(listener: OnClickListener?) {
        this.listener = listener
    }

     class MyAdapter(layoutResId:Int,classs:MutableList<StudentBean>):BaseQuickAdapter<StudentBean,BaseViewHolder>(layoutResId,classs){
         override fun convert(helper: BaseViewHolder, item: StudentBean?) {
             helper.setText(R.id.tv_class_name,item?.nickname )
             helper.setChecked(R.id.cb_check,item?.isCheck!!)
         }
     }

}