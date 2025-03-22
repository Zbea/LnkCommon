package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.book.Book
import com.bll.lnkcommon.utils.GlideUtils


class DownloadBookDialog(private val context: Context, private val book: Book) {

    private var btn_ok:Button?=null
    private var dialog: Dialog?=null

    fun builder(): Dialog? {
        dialog= Dialog(context).apply {
            setContentView(R.layout.dialog_book_detail)
            setCanceledOnTouchOutside(false)
            show()
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            btn_ok = findViewById(R.id.btn_ok)
            val iv_cancel = findViewById<ImageView>(R.id.iv_cancel)
            val iv_book = findViewById<ImageView>(R.id.iv_book)
            val tv_price =findViewById<TextView>(R.id.tv_price)
            val tv_course = findViewById<TextView>(R.id.tv_course)
            val tv_version = findViewById<TextView>(R.id.tv_version)
            val tv_info = findViewById<TextView>(R.id.tv_info)
            val tv_book_name = findViewById<TextView>(R.id.tv_book_name)
            tv_course.visibility=View.GONE

            GlideUtils.setImageRoundUrl(context,book.imageUrl,iv_book,5)

            tv_book_name?.text = book.bookName
            tv_price?.text =context.getString(R.string.price)+"： "  + if (book.price==0) context.getString(R.string.free) else book.price
            tv_version?.text = context.getString(R.string.publish)+"： " + book.version
            tv_info?.text = context.getString(R.string.introduction)+"： " + book.bookDesc

            if (book.buyStatus == 1) {
                btn_ok?.text = context.getString(R.string.click_download)
            } else {
                btn_ok?.text = context.getString(R.string.click_buy)
            }

            if (book.loadSate==2){
                btn_ok?.visibility= View.GONE
            }

            iv_cancel?.setOnClickListener { dismiss() }
            btn_ok?.setOnClickListener { listener?.onClick() }
        }
        return dialog
    }


    fun setChangeStatus() {
        book.buyStatus=1
        btn_ok?.isClickable = true
        btn_ok?.isEnabled = true
        btn_ok?.text = context.getString(R.string.click_download)
    }

    fun setUnClickBtn(string: String){
        if (btn_ok!=null){
            btn_ok?.text = string
            btn_ok?.isClickable = false
            btn_ok?.isEnabled = false//不能再按了
        }
    }

    fun setDissBtn(){
        btn_ok?.visibility = View.GONE
    }


    fun dismiss(){
        dialog?.dismiss()
    }


    private var listener: OnClickListener? = null

    fun interface OnClickListener {
        fun onClick()
    }

    fun setOnClickListener(listener: OnClickListener?) {
        this.listener = listener
    }

}