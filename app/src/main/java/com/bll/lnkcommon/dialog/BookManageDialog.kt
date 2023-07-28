package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.Book

/**
 * type==1 删除
 * type==2 设置 删除
 */
class BookManageDialog(val context: Context, val book:Book,val type:Int){

    fun builder(): BookManageDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_book_manage)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tv_name=dialog.findViewById<TextView>(R.id.tv_name)
        val iv_close=dialog.findViewById<ImageView>(R.id.iv_close)
        val ll_delete=dialog.findViewById<LinearLayout>(R.id.ll_delete)
        val ll_set=dialog.findViewById<LinearLayout>(R.id.ll_set)

        tv_name.text=book.bookName

        if (type==1){
            ll_set.visibility= View.INVISIBLE
        }
        else{
            ll_set.visibility= View.VISIBLE
        }

        iv_close.setOnClickListener {
            dialog.dismiss()
        }

        ll_delete.setOnClickListener {
            onClickListener?.onDelete()
            dialog.dismiss()
        }

        ll_set.setOnClickListener {
            onClickListener?.onSet()
            dialog.dismiss()
        }

        return this
    }

    private var onClickListener: OnDialogClickListener? = null

    interface OnDialogClickListener {
        fun onDelete()
        fun onSet()
    }

    fun setOnDialogClickListener(onClickListener: OnDialogClickListener?) {
        this.onClickListener = onClickListener
    }

}