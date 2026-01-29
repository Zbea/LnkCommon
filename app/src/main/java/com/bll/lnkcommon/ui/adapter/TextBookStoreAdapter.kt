package com.bll.lnkcommon.ui.adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.book.TextbookBean
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class TextBookStoreAdapter(layoutResId: Int, data: List<TextbookBean>?) : BaseQuickAdapter<TextbookBean, BaseViewHolder>(layoutResId, data) {
    var type=0

    override fun convert(helper: BaseViewHolder, item: TextbookBean) {
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

            val tvPrice=getView<TextView>(R.id.tv_price)
            when(type){
                0->{
                    toggleDrawableLeft(tvPrice,false)
                    setText(R.id.tv_price, DataBeanManager.getBookVersionStr(item.version))
                }
                else->{
                    toggleDrawableLeft(tvPrice,true)
                    setText(R.id.tv_price,if (item.price==0) " 免费" else " ${item.price}")
                }
            }

            addOnClickListener(R.id.tv_buy)
        }
    }

    // 快速切换显示/隐藏（复用缓存的 Drawable）
    private fun toggleDrawableLeft(view: TextView, show: Boolean) {
        if (show) {
            val leftDrawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_wallet_smoney)
            val drawableSize = DP2PX.dip2px(mContext, 20f)
            leftDrawable?.setBounds(0, 0, drawableSize, drawableSize)
            view.setCompoundDrawables(leftDrawable, null, null, null)
        } else {
            view.setCompoundDrawables(null, null, null, null)
        }
    }

    fun setChangeType(type:Int){
        this.type=type
        notifyDataSetChanged()
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
