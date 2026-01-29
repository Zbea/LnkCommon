package com.bll.lnkcommon.ui.adapter

import android.widget.ImageView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.book.Book
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class BookStoreAdapter(layoutResId: Int, data: List<Book>?) : BaseQuickAdapter<Book, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: Book) {
        helper.apply {
            setText(R.id.tv_name, item.bookName)
            val image = getView<ImageView>(R.id.iv_image)
            GlideUtils.setImageRoundUrl(mContext, item.imageUrl, image, 8)
            when(item.loadSate){
                2->{
                    setText(R.id.tv_buy,"打开")
                }
                1->{
                    setText(R.id.tv_buy,item.loadString)
                }
                0->{
                    setText(R.id.tv_buy,if (item.buyStatus==1) "下载" else "购买")
                }
            }

            setText(R.id.tv_price,if (item.price==0) " 免费" else " ${item.price}")

            addOnClickListener(R.id.tv_buy)
        }
    }

    fun setChangeText(s:String,pos:Int){
        data[pos].loadString=s
        data[pos].loadSate=1
        notifyItemChanged(pos)
    }

    fun setInitText(pos: Int){
        data[pos].loadString=""
        data[pos].loadSate=0
        notifyItemChanged(pos)
    }

}
