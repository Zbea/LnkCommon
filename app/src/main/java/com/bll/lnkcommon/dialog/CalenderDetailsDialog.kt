package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.CalenderItemBean
import com.bll.lnkcommon.utils.GlideUtils


class CalenderDetailsDialog(private val context: Context, private val item: CalenderItemBean) {

    private var btn_ok:Button?=null
    private var dialog: Dialog?=null

    fun builder(): Dialog? {
        dialog= Dialog(context).apply {
            setContentView(R.layout.dialog_calender_detail)
            show()
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            btn_ok = findViewById(R.id.btn_ok)
            val iv_cancel = findViewById<ImageView>(R.id.iv_cancel)
            val iv_image = findViewById<ImageView>(R.id.iv_image)
            val tv_price =findViewById<TextView>(R.id.tv_price)
            val tv_info = findViewById<TextView>(R.id.tv_info)
            val tv_title = findViewById<TextView>(R.id.tv_title)

            GlideUtils.setImageRoundUrl(context,item.imageUrl,iv_image,10)

            tv_title?.text = item.title
            tv_price?.text = "价格： " + if (item.price==0) "免费" else item.price
            tv_info?.text = "简介： " + item.introduction


            if (item.buyStatus == 1) {
                btn_ok?.text = "点击下载"
            } else {
                btn_ok?.text = "点击购买"
            }

            if (item.loadSate==2){
                btn_ok?.visibility= View.GONE
            }

            iv_cancel?.setOnClickListener { dismiss() }
            btn_ok?.setOnClickListener { listener?.onClick() }
        }
        return dialog
    }


    fun setChangeStatus() {
        item.buyStatus=1
        btn_ok?.text = "点击下载"
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