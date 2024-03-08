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
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder


class MessageSendDialog(private val context: Context) {

    private var students= mutableListOf<StudentBean>()

    private var dialog: Dialog?=null

    fun builder(): MessageSendDialog? {
        dialog= Dialog(context)
        dialog?.setContentView(R.layout.dialog_message_send)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()

        val tvOK = dialog?.findViewById<TextView>(R.id.tv_ok)
        val tvCancel = dialog?.findViewById<TextView>(R.id.tv_cancel)
        val et_content = dialog?.findViewById<EditText>(R.id.et_content)
        val rvList=dialog?.findViewById<RecyclerView>(R.id.rv_list)

        students=DataBeanManager.students

        val mAdapter=MyAdapter(R.layout.item_message_student,students)
        rvList?.layoutManager=LinearLayoutManager(context)
        rvList?.adapter=mAdapter
        mAdapter.bindToRecyclerView(rvList)
        mAdapter.setOnItemClickListener { adapter, view, position ->
            students[position].isCheck=!students[position].isCheck
            mAdapter.notifyItemChanged(position)
        }

        tvCancel?.setOnClickListener { dismiss() }
        tvOK?.setOnClickListener {
            val contentStr=et_content?.text.toString()
            if (contentStr.isNotEmpty()&& getCheckIds().isNotEmpty())
            {
                dismiss()
                listener?.onSend(contentStr,getCheckIds())
            }
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
        fun onSend(contentStr:String,ids: List<Int>)
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